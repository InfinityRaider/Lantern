package com.infinityraider.boatlantern.block;

import com.infinityraider.boatlantern.block.tile.TileEntityLantern;
import com.infinityraider.infinitylib.block.BlockBaseTile;
import com.infinityraider.infinitylib.block.blockstate.InfinityProperty;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemBlock;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.List;

public class BlockLantern extends BlockBaseTile<TileEntityLantern> {
    public BlockLantern(String name, Material blockMaterial) {
        super(name, blockMaterial);
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
        return new InfinityProperty[0];
    }

    @Override
    public Class<? extends ItemBlock> getItemBlockClass() {
        return null;
    }
}
