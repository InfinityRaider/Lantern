package com.infinityraider.lantern.block;

import com.infinityraider.infinitylib.block.BlockBase;
import com.infinityraider.infinitylib.block.property.InfPropertyConfiguration;
import com.infinityraider.lantern.Lantern;
import com.infinityraider.lantern.reference.Names;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

import javax.annotation.ParametersAreNonnullByDefault;

public class BlockLanternLight extends BlockBase {
    public BlockLanternLight() {
        super(Names.Blocks.LIGHT, Properties.create(Material.AIR)
                .doesNotBlockMovement().setAir().noDrops().notSolid()
                .setLightLevel((state) -> Lantern.instance.getConfig().getLightLevel())
        );
    }

    @Override
    @ParametersAreNonnullByDefault
    public int getLightValue(BlockState state, IBlockReader world, BlockPos pos) {
        return Lantern.instance.getConfig().getLightLevel();
    }

    @Override
    public boolean isTransparent(BlockState state) {
        return true;
    }

    @Deprecated
    public boolean isReplaceable(BlockState state, BlockItemUseContext useContext) {
        return true;
    }

    @Deprecated
    public boolean isReplaceable(BlockState state, Fluid fluid) {
        return true;
    }


    @Override
    @Deprecated
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.INVISIBLE;
    }

    @Override
    protected InfPropertyConfiguration getPropertyConfiguration() {
        return InfPropertyConfiguration.empty();
    }
}
