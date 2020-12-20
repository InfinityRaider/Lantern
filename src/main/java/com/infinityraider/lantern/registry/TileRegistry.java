package com.infinityraider.lantern.registry;

import com.infinityraider.infinitylib.block.tile.InfinityTileEntityType;
import com.infinityraider.lantern.Lantern;
import com.infinityraider.lantern.block.tile.TileEntityLantern;
import com.infinityraider.lantern.reference.Names;
import net.minecraft.tileentity.TileEntityType;

public class TileRegistry {
    private static final TileRegistry INSTANCE = new TileRegistry();

    public static TileRegistry getInstance() {
        return INSTANCE;
    }

    public final TileEntityType<TileEntityLantern> lantern;

    private TileRegistry() {
        this.lantern = InfinityTileEntityType.builder(Names.Blocks.LANTERN, TileEntityLantern::new)
                .addBlock(Lantern.instance.getModBlockRegistry().blockLantern)
                .build();
    }
}
