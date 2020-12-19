package com.infinityraider.lantern.lantern;

import com.infinityraider.infinitylib.utility.inventory.IInventorySerializableItemHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IInventoryLantern extends IInventorySerializableItemHandler {
    ILantern getLantern();

    @Override
    default int getSizeInventory() {
        return 1;
    }

    ItemStack getFuelStack();

    IInventoryLantern setFuelStack(ItemStack stack);

    @Nullable
    @Override
    default ItemStack getStackInSlot(int index) {
        return this.getFuelStack();
    }

    @Override
    default void setInventorySlotContents(int index, @Nullable ItemStack stack) {
        this.setFuelStack(stack);
    }

    @Nullable
    @Override
    default ItemStack decrStackSize(int index, int count) {
        ItemStack fuelStack = this.getStackInSlot(index);
        if(fuelStack == null) {
            return null;
        }
        count = Math.min(count, fuelStack.getCount());
        ItemStack stack = fuelStack.copy();
        stack.setCount(count);
        fuelStack.setCount(fuelStack.getCount() - count);
        if(fuelStack.getCount() <= 0) {
            this.setInventorySlotContents(index, null);
        } else {
            this.markDirty();
        }
        return stack.getCount() > 0 ? stack : null;
    }

    @Nullable
    @Override
    default ItemStack removeStackFromSlot(int index) {
        ItemStack fuelStack = this.getFuelStack();
        if(fuelStack == null) {
            return null;
        }
        ItemStack stack = fuelStack.copy();
        this.setInventorySlotContents(index, null);
        return stack;
    }

    @Override
    default int getInventoryStackLimit() {
        return 64;
    }

    @Override
    default boolean isUsableByPlayer(PlayerEntity player) {
        return true;
    }

    @Override
    default void openInventory(PlayerEntity player) {}

    @Override
    default void closeInventory(PlayerEntity player) {}

    @Override
    default boolean isItemValidForSlot(int index, ItemStack stack) {
        if(stack.isEmpty()) {
            return true;
        }
        ItemStack fuelStack = this.getStackInSlot(index);
        if(fuelStack != null) {
            return ItemStack.areItemsEqual(fuelStack, stack) && ItemStack.areItemStackTagsEqual(fuelStack, stack);
        } else {
            return ForgeHooks.getBurnTime(stack) > 0;
        }
    }

    @Override
    default boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return this.isItemValidForSlot(slot, stack);
    }

    @Override
    default void clear() {
        this.setInventorySlotContents(0, null);
    }
}
