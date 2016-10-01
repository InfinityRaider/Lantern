package com.infinityraider.boatlantern.block;

import com.infinityraider.boatlantern.block.tile.TileEntityLanternLight;
import com.infinityraider.infinitylib.block.BlockBaseTile;
import com.infinityraider.infinitylib.block.blockstate.InfinityProperty;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemBlock;
import net.minecraft.world.World;

import java.util.List;

public class BlockLanternLight extends BlockBaseTile {
    public BlockLanternLight() {
        super("lantern", Material.CIRCUITS);
    }

    @Override
    public TileEntityLanternLight createNewTileEntity(World worldIn, int meta) {
        return new TileEntityLanternLight();
    }

    @Override
    public List<String> getOreTags() {
        return null;
    }

    @Override
    protected InfinityProperty[] getPropertyArray() {
        return new InfinityProperty[0];
    }

    @Override
    public Class<? extends ItemBlock> getItemBlockClass() {
        return null;
    }
}
