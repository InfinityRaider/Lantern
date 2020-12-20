package com.infinityraider.lantern.handler;

import com.infinityraider.lantern.Lantern;
import com.infinityraider.lantern.registry.BlockRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityLeaveWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.IdentityHashMap;
import java.util.Map;

public class LightingHandler {
    private static final LightingHandler INSTANCE = new LightingHandler();

    public static LightingHandler getInstance() {
        return INSTANCE;
    }

    private final Map<Entity, BlockPos> lights;
    private final Map<Entity, Boolean> placed;

    private LightingHandler() {
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
            lights.put(entity, pos);
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

    public void removeLastLight(Entity entity) {
        if(lights.containsKey(entity)) {
            BlockPos pos = lights.get(entity);
            this.removeLight(entity, pos);
            lights.remove(entity);
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
            this.placed.put(entity, true);
        }
    }

    @SubscribeEvent
    @SuppressWarnings("unused")
    public void onEntityRemoved(EntityLeaveWorldEvent event) {
        removeLastLight(event.getEntity());
        this.placed.remove(event.getEntity());
    }

    @SubscribeEvent
    @SuppressWarnings("unused")
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if(!event.player.getEntityWorld().isRemote) {
            if(this.placed.containsKey(event.player)) {
                if(!this.placed.get(event.player)) {
                    this.removeLastLight(event.player);
                }
                this.placed.put(event.player, false);
            }
        }
    }
}
