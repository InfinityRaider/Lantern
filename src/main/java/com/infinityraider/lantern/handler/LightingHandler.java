package com.infinityraider.lantern.handler;

import com.google.common.collect.Maps;
import com.infinityraider.lantern.Lantern;
import com.infinityraider.lantern.registry.BlockRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityLeaveWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;

import javax.annotation.Nullable;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class LightingHandler {
    private static final Map<RegistryKey<World>, LightingHandler> HANDLERS = Maps.newIdentityHashMap();
    private static final EventHandler EVENT_HANDLER = new EventHandler();

    public static LightingHandler getInstance(Entity entity) {
        return getInstance(entity.getEntityWorld());
    }

    public static LightingHandler getInstance(World world) {
        return getInstance(world.getDimensionKey());
    }

    public static LightingHandler getInstance(RegistryKey<World> dimension) {
        if(HANDLERS.containsKey(dimension)) {
            return HANDLERS.get(dimension);
        } else {
            LightingHandler handler = new LightingHandler(dimension);
            HANDLERS.put(dimension, handler);
            return handler;
        }
    }

    public static EventHandler getEventHandler() {
        return EVENT_HANDLER;
    }

    private final RegistryKey<World> dimension;

    private final Map<UUID, BlockPos> lights;
    private final Map<UUID, Boolean> placed;

    private LightingHandler(RegistryKey<World> dimension) {
        this.dimension = dimension;
        this.lights = new IdentityHashMap<>();
        this.placed = new IdentityHashMap<>();
    }

    public void spreadLight(Entity entity) {
        if(Lantern.instance.getConfig().disableWorldLighting()) {
            this.removeLastLight(entity);
            return;
        }
        if(entity.getEntityWorld().isRemote) {
            return;
        }
        BlockPos pos = placeLight(entity);
        if(pos != null) {
            this.removeLastLight(entity);
            lights.put(entity.getUniqueID(), pos);
        }
    }

    @Nullable
    protected BlockPos placeLight(Entity entity) {
        World world = entity.getEntityWorld();
        BlockPos pos = entity.getPosition();
        BlockState state = world.getBlockState(pos);
        if(state.getBlock() == BlockRegistry.getInstance().blockLight) {
            return null;
        }
        if(state.getBlock().isAir(state, world, pos)) {
            world.setBlockState(pos, BlockRegistry.getInstance().blockLight.getDefaultState());
            return pos;
        }
        pos = pos.up();
        state = world.getBlockState(pos);
        if(state.getBlock() == BlockRegistry.getInstance().blockLight) {
            return null;
        }
        if(state.getBlock().isAir(state, world, pos)) {
            world.setBlockState(pos, BlockRegistry.getInstance().blockLight.getDefaultState());
            return pos;
        }
        return null;
    }

    protected void clearLights() {
        Iterator<Map.Entry<UUID, BlockPos>> it = this.lights.entrySet().iterator();
        World world = Lantern.instance.getWorldFromDimension(this.dimension);
        if(world != null) {
            this.lights.values().forEach(pos ->  world.setBlockState(pos, Blocks.AIR.getDefaultState()));
        }
        this.lights.clear();
        this.placed.clear();
    }

    public void removeLastLight(Entity entity) {
        if(lights.containsKey(entity.getUniqueID())) {
            BlockPos pos = lights.get(entity.getUniqueID());
            this.removeLight(entity, pos);
            lights.remove(entity.getUniqueID());
        }
    }

    protected void removeLight(Entity entity, BlockPos pos) {
        World world = entity.getEntityWorld();
        if(!world.isRemote) {
            BlockState state = world.getBlockState(pos);
            if(state.getBlock() == BlockRegistry.getInstance().blockLight) {
                world.setBlockState(pos, Blocks.AIR.getDefaultState());
            }
        }
    }

    public void playerLightTick(Entity entity) {
        if(!entity.getEntityWorld().isRemote) {
            this.placed.put(entity.getUniqueID(), true);
        }
    }

    public static final class EventHandler {
        private EventHandler() {}

        @SubscribeEvent
        @SuppressWarnings("unused")
        public void onEntityRemoved(EntityLeaveWorldEvent event) {
            LightingHandler handler = getInstance(event.getEntity());
            handler.removeLastLight(event.getEntity());
            handler.placed.remove(event.getEntity().getUniqueID());
        }

        @SubscribeEvent
        @SuppressWarnings("unused")
        public void onPlayerTick(TickEvent.PlayerTickEvent event) {
            if(!event.player.getEntityWorld().isRemote) {
                LightingHandler handler = getInstance(event.player);
                if(handler.placed.containsKey(event.player.getUniqueID())) {
                    if(!handler.placed.get(event.player.getUniqueID())) {
                        handler.removeLastLight(event.player);
                    }
                    handler.placed.put(event.player.getUniqueID(), false);
                }
            }
        }

        public void onServerStopping(FMLServerStoppingEvent event) {
            event.getServer().func_240770_D_().stream().map(LightingHandler::getInstance).forEach(LightingHandler::clearLights);
        }
    }
}
