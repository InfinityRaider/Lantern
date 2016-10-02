package com.infinityraider.boatlantern.lantern;

import com.infinityraider.boatlantern.handler.ConfigurationHandler;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class LanternLogic {
    private final ILantern lantern;
    private final IInventoryLantern inventory;

    public LanternLogic(ILantern lantern) {
        this.lantern = lantern;
        this.inventory = lantern.getInventory();
    }

    public void burnUpdate() {
        if(this.isLit()) {
            if(ConfigurationHandler.getInstance().enableFuelConsumption) {
                if (this.getRemainingBurnTicks() > 0) {
                    this.spreadLight();
                    this.addBurnTicks(-1);
                } else if (this.getFuelStack() != null) {
                    this.addBurnTicks(GameRegistry.getFuelValue(this.getFuelStack()) * ConfigurationHandler.getInstance().burnTimeMultiplier);
                    this.inventory.decrStackSize(0, 1);
                } else {
                    this.setLit(false);
                }
            } else {
                this.spreadLight();
            }
        }
    }

    protected void spreadLight() {
        this.lantern.spreadLight();
    }

    protected void setLit(boolean status) {
        this.lantern.setLit(status);
    }

    protected boolean isLit() {
        return this.lantern.isLit();
    }

    protected ItemStack getFuelStack() {
        return this.inventory.getFuelStack();
    }

    protected int getRemainingBurnTicks() {
        return this.lantern.getRemainingBurnTicks();
    }

    protected ILantern addBurnTicks(int ticks) {
        return lantern.addBurnTicks(ticks);
    }
}
