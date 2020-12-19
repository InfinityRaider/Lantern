package com.infinityraider.lantern.lantern;

import com.infinityraider.lantern.Lantern;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeHooks;

public interface ILantern {
    IInventoryLantern getInventory();

    void setLit(boolean status);

    boolean isLit();

    int getRemainingBurnTicks();

    default boolean consumeFuel() {
        if(!Lantern.instance.getConfig().consumeFuelWhenNotHeld()) {
            return true;
        }
        ItemStack stack = this.getInventory().getFuelStack();
        if(stack == null) {
            return false;
        } else {
            int ticks = ForgeHooks.getBurnTime(stack);
            if(ticks > 0) {
                this.addBurnTicks(ticks * Lantern.instance.getConfig().burnTimeMultiplier());
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
