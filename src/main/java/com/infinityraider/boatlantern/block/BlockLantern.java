package com.infinityraider.boatlantern.block;

import com.infinityraider.boatlantern.block.tile.TileEntityLantern;
import com.infinityraider.infinitylib.block.BlockBaseTile;
import com.infinityraider.infinitylib.block.blockstate.InfinityProperty;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collections;
import java.util.List;

public class BlockLantern extends BlockBaseTile<TileEntityLantern> {
    public static final InfinityProperty[] PROPERTIES = new InfinityProperty[] {
            Properties.LIT,
            Properties.FACING_X,
            Properties.HANGING
    };

    public BlockLantern() {
        super("lantern", Material.CIRCUITS);
    }

    @Override
    public TileEntityLantern createNewTileEntity(World worldIn, int meta) {
        return new TileEntityLantern();
    }

    @Override
    public List<String> getOreTags() {
        return Collections.emptyList();
    }

    @Override
    protected InfinityProperty[] getPropertyArray() {
        return PROPERTIES;
    }

    @Deprecated
    public IBlockState getStateFromMeta(int meta) {
        IBlockState state = this.getDefaultState();
        Properties.LIT.applyToBlockState(state, (meta & 1) == 1);
        Properties.FACING_X.applyToBlockState(state, ( (meta >> 1) & 1) == 1);
        Properties.FACING_X.applyToBlockState(state, ( (meta >> 2) & 1) == 1);
        return state;
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int meta = 0;
        if(Properties.LIT.getValue(state)) {
            meta += 1;
        }
        if(Properties.FACING_X.getValue(state)) {
            meta += 2;
        }
        if(Properties.FACING_X.getValue(state)) {
            meta += 4;
        }
        return meta;
    }

    @Override
    public Class<? extends ItemBlock> getItemBlockClass() {
        return null;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.TRANSLUCENT;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    public static class Properties {
        public static final InfinityProperty<Boolean> LIT = new InfinityProperty<>(PropertyBool.create("lit"), false);
        public static final InfinityProperty<Boolean> FACING_X = new InfinityProperty<>(PropertyBool.create("facing_x"), true);
        public static final InfinityProperty<Boolean> HANGING = new InfinityProperty<>(PropertyBool.create("hanging"), false);
    }

}
