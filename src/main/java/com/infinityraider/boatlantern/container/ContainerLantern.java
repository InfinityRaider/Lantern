package com.infinityraider.boatlantern.container;

import com.infinityraider.boatlantern.lantern.IInventoryLantern;
import com.infinityraider.infinitylib.container.ContainerBase;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;

import javax.annotation.Nullable;

public class ContainerLantern extends ContainerBase {
    private final IInventoryLantern lantern;

    public ContainerLantern(InventoryPlayer inventory, IInventoryLantern lantern) {
        super(inventory, 0, 0);
        this.lantern = lantern;
    }

    public IInventoryLantern getLanternInventory() {
        return this.lantern;
    }

    public static class FuelSlot extends Slot {
        public FuelSlot(IInventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean isItemValid(@Nullable ItemStack stack) {
            return stack == null || GameRegistry.getFuelValue(stack) > 0;
        }

    }
}
