package com.infinityraider.boatlantern.lantern;

import com.infinityraider.boatlantern.handler.ConfigurationHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;

public interface ILantern {
    IInventoryLantern getInventory();

    void setLit(boolean status);

    boolean isLit();

    int getRemainingBurnTicks();

    default boolean consumeFuel() {
        if(!ConfigurationHandler.getInstance().consumesFuelWhenNotHeld) {
            return true;
        }
        ItemStack stack = this.getInventory().getFuelStack();
        if(stack == null) {
            return false;
        } else {
            int ticks = TileEntityFurnace.getItemBurnTime(stack);
            if(ticks > 0) {
                this.addBurnTicks(ticks * ConfigurationHandler.getInstance().burnTimeMultiplier);
                if(stack.getItem().hasContainerItem(stack)) {
                    this.getInventory().setInventorySlotContents(0, stack.getItem().getContainerItem(stack));
                } else {
                    this.getInventory().decrStackSize(0, 1);
                }
                return true;
            } else {
                return false;
            }
        }
    }

    ILantern addBurnTicks(int ticks);

    void spreadLight();

    default void copyFrom(ILantern lantern) {
        this.setLit(lantern.isLit());
        this.addBurnTicks(lantern.getRemainingBurnTicks() - this.getRemainingBurnTicks());
        this.getInventory().setFuelStack(lantern.getInventory().getFuelStack());
    }
}
