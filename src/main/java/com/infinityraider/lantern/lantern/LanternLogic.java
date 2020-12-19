package com.infinityraider.lantern.lantern;

import com.infinityraider.lantern.Lantern;

public class LanternLogic {
    private final ILantern lantern;

    public LanternLogic(ILantern lantern) {
        this.lantern = lantern;
    }

    public void burnUpdate() {
        if(this.isLit()) {
            if(Lantern.instance.getConfig().enableFuelConsumption()) {
                if(this.getRemainingBurnTicks() > 0) {
                    this.spreadLight();
                    this.addBurnTicks(-1);
                } else if(!this.consumeFuel()) {
                    this.setLit(false);
                }
            } else {
                this.spreadLight();
            }
        }
    }

    protected boolean consumeFuel() {
        return this.lantern.consumeFuel();
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

    protected int getRemainingBurnTicks() {
        return this.lantern.getRemainingBurnTicks();
    }

    protected ILantern addBurnTicks(int ticks) {
        return lantern.addBurnTicks(ticks);
    }
}
