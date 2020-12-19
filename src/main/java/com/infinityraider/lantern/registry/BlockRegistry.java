package com.infinityraider.lantern.registry;

import com.infinityraider.lantern.block.BlockLantern;
import com.infinityraider.lantern.block.BlockLanternLight;
import net.minecraft.block.Block;

public class BlockRegistry {
    private static final BlockRegistry INSTANCE = new BlockRegistry();

    public static BlockRegistry getInstance() {
        return INSTANCE;
    }

    public final Block blockLantern;
    public final Block blockLight;

    private BlockRegistry() {
        this.blockLantern = new BlockLantern();
        this.blockLight= new BlockLanternLight();
    }
}
