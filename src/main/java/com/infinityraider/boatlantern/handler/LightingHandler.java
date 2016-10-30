package com.infinityraider.boatlantern.handler;

import com.infinityraider.boatlantern.registry.BlockRegistry;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import javax.annotation.Nullable;
import java.util.IdentityHashMap;
import java.util.Map;

public class LightingHandler implements IWorldEventListener {
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
        if(ConfigurationHandler.getInstance().disableWorldLighting) {
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
        IBlockState state = world.getBlockState(pos);
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
            IBlockState state = world.getBlockState(pos);
            if(state.getBlock() == BlockRegistry.getInstance().blockLight) {
                world.setBlockToAir(pos);
            }
        }
    }

    public void playerLightTick(Entity entity) {
        if(!entity.getEntityWorld().isRemote) {
            this.placed.put(entity, true);
        }
    }

    //useful IWorldEventListener callback to avoid memory leaks
    @Override
    public void onEntityRemoved(Entity entity) {
        removeLastLight(entity);
        if(this.placed.containsKey(entity)) {
            this.placed.remove(entity);
        }
    }

    @SubscribeEvent
    @SuppressWarnings("unused")
    public void onWorldLoad(WorldEvent.Load event) {
        if(!event.getWorld().isRemote) {
            event.getWorld().addEventListener(this);
        }
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


    //IWorldEventListener methods not useful here
    //-------------------------------------------

    @Override
    public void notifyBlockUpdate(World worldIn, BlockPos pos, IBlockState oldState, IBlockState newState, int flags) {}

    @Override
    public void notifyLightSet(BlockPos pos) {}

    @Override
    public void markBlockRangeForRenderUpdate(int x1, int y1, int z1, int x2, int y2, int z2) {}

    @Override
    public void playSoundToAllNearExcept(@Nullable EntityPlayer player, SoundEvent soundIn, SoundCategory category, double x, double y, double z, float volume, float pitch) {}

    @Override
    public void playRecord(SoundEvent soundIn, BlockPos pos) {}

    @Override
    public void spawnParticle(int particleID, boolean ignoreRange, double xCoord, double yCoord, double zCoord, double xSpeed, double ySpeed, double zSpeed, int... parameters) {

    }

    @Override
    public void onEntityAdded(Entity entity) {}

    @Override
    public void broadcastSound(int soundID, BlockPos pos, int data) {}

    @Override
    public void playEvent(EntityPlayer player, int type, BlockPos blockPosIn, int data) {}

    @Override
    public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress) {}
}
