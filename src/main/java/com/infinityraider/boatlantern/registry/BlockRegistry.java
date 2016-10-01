package com.infinityraider.boatlantern.registry;

import com.infinityraider.boatlantern.block.BlockLantern;
import com.infinityraider.boatlantern.block.BlockLanternLight;
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
