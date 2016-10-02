package com.infinityraider.boatlantern.lantern;

public interface ILantern {
    IInventoryLantern getInventory();

    void setLit(boolean status);

    boolean isLit();

    int getRemainingBurnTicks();

    ILantern addBurnTicks(int ticks);

    void spreadLight();

    default void copyFrom(ILantern lantern) {
        this.setLit(lantern.isLit());
        this.addBurnTicks(lantern.getRemainingBurnTicks() - this.getRemainingBurnTicks());
        this.getInventory().setFuelStack(lantern.getInventory().getFuelStack());
    }
}
