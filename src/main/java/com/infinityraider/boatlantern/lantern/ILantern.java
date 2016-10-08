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
            this.addBurnTicks(TileEntityFurnace.getItemBurnTime(stack) * ConfigurationHandler.getInstance().burnTimeMultiplier);
            this.getInventory().decrStackSize(0, 1);
            return true;
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
