package com.infinityraider.boatlantern.block;

import com.infinityraider.boatlantern.block.tile.TileEntityLanternLight;
import com.infinityraider.infinitylib.block.BlockBaseTile;
import com.infinityraider.infinitylib.block.ICustomRenderedBlock;
import com.infinityraider.infinitylib.block.blockstate.InfinityProperty;
import com.infinityraider.infinitylib.render.block.IBlockRenderingHandler;
import com.infinityraider.infinitylib.render.block.RenderBlockEmpty;
import net.minecraft.block.material.Material;
import net.minecraft.world.World;

public class BlockLanternLight extends BlockBaseTile implements ICustomRenderedBlock {
    public BlockLanternLight() {
        super("light", Material.AIR);
    }

    @Override
    public TileEntityLanternLight createNewTileEntity(World worldIn, int meta) {
        return new TileEntityLanternLight();
    }

    @Override
    protected InfinityProperty[] getPropertyArray() {
        return new InfinityProperty[0];
    }

    @Override
    public IBlockRenderingHandler getRenderer() {
        return RenderBlockEmpty.createEmptyRender(this);
    }
}
