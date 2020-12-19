package com.infinityraider.lantern.registry;

import com.infinityraider.infinitylib.block.IInfinityBlock;
import com.infinityraider.lantern.item.ItemLantern;
import net.minecraft.item.Item;

public class ItemRegistry {
    private static final ItemRegistry INSTANCE = new ItemRegistry();

    public static ItemRegistry getInstance() {
        return INSTANCE;
    }

    public final Item itemLantern;

    private ItemRegistry() {
        this.itemLantern = new ItemLantern((IInfinityBlock) BlockRegistry.getInstance().blockLantern);
    }
}
