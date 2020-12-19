package com.infinityraider.lantern.container;

import com.infinityraider.lantern.lantern.IInventoryLantern;
import com.infinityraider.lantern.lantern.ILantern;
import com.infinityraider.infinitylib.container.ContainerBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;

import javax.annotation.Nullable;

public class ContainerLantern extends ContainerBase {
    private final IInventoryLantern lantern;

    public ContainerLantern(InventoryPlayer inventory, IInventoryLantern lantern) {
        super(inventory, 8, 44);
        this.lantern = lantern;
        this.addSlotToContainer(new FuelSlot(this.getLanternInventory(), 0, 80, 9));
    }

    public IInventoryLantern getLanternInventory() {
        return this.lantern;
    }

    public boolean isLit() {
        ILantern lantern = this.getLanternInventory().getLantern();
        return this.getLanternInventory().getLantern().isLit();
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int clickedSlot) {
        ItemStack itemstack = null;
        Slot slot = this.inventorySlots.get(clickedSlot);
        if (slot != null && slot.getHasStack()) {
            ItemStack itemStack1 = slot.getStack();
            itemstack = itemStack1.copy();
            //try to move item from the lantern into the player's inventory
            if (clickedSlot >= PLAYER_INVENTORY_SIZE) {
                if (!this.mergeItemStack(itemStack1, 0, inventorySlots.size() - 2, false)) {
                    return null;
                }
            }
            else {
                //try to move item from the player's inventory into the lantern
                if(itemStack1.getItem() != null) {
                    if(this.getLanternInventory().isItemValidForSlot(0, itemStack1)) {
                        if (!this.mergeItemStack(itemStack1, PLAYER_INVENTORY_SIZE, PLAYER_INVENTORY_SIZE + 1, false)) {
                            return null;
                        }
                    }
                }
            }
            if (itemStack1.stackSize == 0) {
                slot.putStack(null);
            }
            else {
                slot.onSlotChanged();
            }
            if (itemStack1.stackSize == itemstack.stackSize) {
                return null;
            }
            slot.onPickupFromSlot(player, itemStack1);
        }
        return itemstack;
    }

    public static class FuelSlot extends Slot {
        public FuelSlot(IInventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean isItemValid(@Nullable ItemStack stack) {
            return stack == null || TileEntityFurnace.getItemBurnTime(stack) > 0;
        }

    }
}
