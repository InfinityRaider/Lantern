package com.infinityraider.lantern.block;

import com.infinityraider.infinitylib.block.property.InfProperty;
import com.infinityraider.infinitylib.block.property.InfPropertyConfiguration;
import com.infinityraider.lantern.Lantern;
import com.infinityraider.lantern.block.tile.TileEntityLantern;
import com.infinityraider.lantern.handler.GuiHandler;
import com.infinityraider.lantern.item.ItemLantern;
import com.infinityraider.lantern.lantern.ItemHandlerLantern;
import com.infinityraider.lantern.lantern.LanternItemCache;
import com.infinityraider.lantern.reference.Names;
import com.infinityraider.infinitylib.block.BlockBaseTile;
import com.infinityraider.lantern.registry.ItemRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

public class BlockLantern extends BlockBaseTile<TileEntityLantern> {
    public static final InfProperty<Boolean> LIT = InfProperty.Creators.create("lit", false);
    public static final InfProperty<Direction.Axis> AXIS = InfProperty.Creators.createHorizontals("axis", Direction.Axis.X);
    public static final InfProperty<Boolean> HANGING = InfProperty.Creators.create("hanging", false);
    public static final InfProperty<Boolean> WATERLOGGED = InfProperty.Defaults.waterlogged();

    private static final InfPropertyConfiguration PROPERTIES = InfPropertyConfiguration.builder()
            .add(LIT)
            .add(AXIS)
            .add(HANGING)
            .waterloggable()
            .build();

    private static final BiFunction<BlockState, IBlockReader, TileEntityLantern> TILE_FACTORY = (s, w) -> new TileEntityLantern();

    private final VoxelShape hitBoxStanding;
    private final VoxelShape hitBoxHanging;

    public BlockLantern() {
        super(Names.Blocks.LANTERN, Properties
                .create(Material.MISCELLANEOUS)
                .setLightLevel((state) -> LIT.fetch(state) ? Lantern.instance.getConfig().getLightLevel() : 0)
                .setOpaque((a1, a2, a3) -> false)
        );
        this.hitBoxStanding = Block.makeCuboidShape(5, 0, 5, 11, 11, 11);
        this.hitBoxHanging = Block.makeCuboidShape(5, 5, 5, 11, 16, 11);
    }

    @Override
    protected InfPropertyConfiguration getPropertyConfiguration() {
        return PROPERTIES;
    }

    @Override
    public ItemLantern asItem() {
        return (ItemLantern) ItemRegistry.getInstance().itemLantern;
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        BlockState state = this.getDefaultState();
        if(!state.isValidPosition(context.getWorld(), context.getPos())) {
            return null;
        }
        ItemHandlerLantern lantern = LanternItemCache.getInstance().getLantern(context.getPlayer(), context.getItem());
        Direction direction = context.getFace();
        Direction horizontal = context.getPlacementHorizontalFacing();
        Direction.Axis axis = horizontal.getAxis();
        state = HANGING.apply(state, direction == Direction.DOWN);
        state = AXIS.apply(state, axis);
        state = LIT.apply(state, lantern != null && lantern.isLit());
        state = WATERLOGGED.apply(state, context.getWorld().getFluidState(context.getPos()).getFluid() == Fluids.WATER);
        return state;
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity entity, ItemStack stack) {
        super.onBlockPlacedBy(world, pos, state, entity, stack);
        ItemHandlerLantern lantern = LanternItemCache.getInstance().getLantern(stack);
        TileEntity te = world.getTileEntity(pos);
        if(te instanceof TileEntityLantern && lantern != null) {
            ((TileEntityLantern) te).copyFrom(lantern);
        }
    }

    @Override
    @Deprecated
    public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        if (!world.isRemote) {
            TileEntity te = world.getTileEntity(hit.getPos());
            if(te instanceof TileEntityLantern) {
                TileEntityLantern lantern = (TileEntityLantern) te;
                if (player.isSneaking()) {
                    GuiHandler.getInstance().openGui(player, lantern);
                } else {
                    boolean lit = LIT.fetch(state);
                    if(lit || lantern.getRemainingBurnTicks() > 0 || lantern.consumeFuel()) {
                        world.setBlockState(pos, LIT.apply(state, !lit));
                    }
                }
            }
        }
        return ActionResultType.CONSUME;
    }

    @Override
    @Deprecated
    public void onBlockClicked(BlockState state, World world, BlockPos pos, PlayerEntity player) {
        if(!world.isRemote) {
            spawnDrops(state, world, pos);
            world.removeTileEntity(pos);
            world.setBlockState(pos, net.minecraft.block.Blocks.AIR.getDefaultState());
        }
    }

    @Override
    @Deprecated
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder context) {
        return Collections.emptyList();
    }

    @Override
    public void spawnAdditionalDrops(BlockState state, ServerWorld world, BlockPos pos, ItemStack stack) {
        ItemLantern item = this.asItem();
        if(item != null) {
            ItemStack drop = new ItemStack(item, 1);
            ItemHandlerLantern lantern = LanternItemCache.getInstance().getLantern(drop);
            TileEntity te = world.getWorld().getTileEntity(pos);
            if(lantern != null && te instanceof TileEntityLantern) {
                lantern.copyFrom((TileEntityLantern) te);
            }
            spawnAsEntity(world, pos, drop);
        }
    }

    @Override
    @ParametersAreNonnullByDefault
    public int getLightValue(BlockState state, IBlockReader world, BlockPos pos) {
        return LIT.fetch(state) ? Lantern.instance.getConfig().getLightLevel() : 0;
    }

    @Override
    @Deprecated
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        return HANGING.fetch(state) ? this.hitBoxHanging : this.hitBoxStanding;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public RenderType getRenderType() {
        return RenderType.getTranslucent();
    }

    @Override
    @Deprecated
    public boolean isTransparent(BlockState state) {
        return true;
    }

    @Override
    public BiFunction<BlockState, IBlockReader, TileEntityLantern> getTileEntityFactory() {
        return TILE_FACTORY;
    }
}
