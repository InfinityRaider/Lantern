package com.infinityraider.boatlantern.lantern;

import com.infinityraider.infinitylib.reference.Reference;
import com.infinityraider.infinitylib.utility.inventory.IInventorySerializableItemHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

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
        count = Math.min(count, fuelStack.stackSize);
        ItemStack stack = fuelStack.copy();
        stack.stackSize = count;
        if(fuelStack.stackSize <= count || fuelStack.stackSize <= 0) {
            this.setInventorySlotContents(index, fuelStack.getItem().getContainerItem(fuelStack));
        }
        return stack;
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
    default boolean isUseableByPlayer(EntityPlayer player) {
        return true;
    }

    @Override
    default void openInventory(EntityPlayer player) {}

    @Override
    default void closeInventory(EntityPlayer player) {}

    @Override
    default boolean isItemValidForSlot(int index, ItemStack stack) {
        if(stack == null) {
            return true;
        }
        ItemStack fuelStack = this.getStackInSlot(index);
        if(fuelStack != null) {
            return ItemStack.areItemsEqual(fuelStack, stack) && ItemStack.areItemStackTagsEqual(fuelStack, stack);
        } else {
            return TileEntityFurnace.getItemBurnTime(stack) > 0;
        }
    }

    @Override
    default int getField(int id) {
        return 0;
    }

    @Override
    default void setField(int id, int value) { }

    @Override
    default int getFieldCount() {
        return 0;
    }

    @Override
    default void clear() {
        this.setInventorySlotContents(0, null);
    }

    @Override
    default String getName() {
        return Reference.MOD_ID.toLowerCase() +  ".inventory.lantern";
    }

    @Override
    default boolean hasCustomName() {
        return false;
    }

    @Override
    default ITextComponent getDisplayName() {
        return new TextComponentTranslation(this.getName());
    }
}
