package com.infinityraider.lantern.config;

import com.infinityraider.infinitylib.config.ConfigurationHandler;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;

public abstract class Config implements ConfigurationHandler.SidedModConfig {
    private Config(ForgeConfigSpec.Builder builder) {}

    public abstract int getLightLevel();

    public abstract boolean consumeFuelWhenNotHeld();

    public abstract boolean onlyLightWhenHeld();

    public abstract boolean enableFuelConsumption();

    public abstract boolean disableWorldLighting();

    public abstract int burnTimeMultiplier();

    public static final class Common extends Config {
        private final ForgeConfigSpec.IntValue lanternLightLevel;
        private final ForgeConfigSpec.BooleanValue consumesFuelWhenNotHeld;
        private final ForgeConfigSpec.BooleanValue onlyLightWorldWhenHeld;
        private final ForgeConfigSpec.BooleanValue enableFuelConsumption;
        private final ForgeConfigSpec.BooleanValue disableWorldLighting;
        private final ForgeConfigSpec.IntValue burnTimeMultiplier;

        public Common(ForgeConfigSpec.Builder builder) {
            super(builder);
            this.lanternLightLevel = builder
                    .comment("The light level emitted by the lantern")
                    .defineInRange("Lantern brightness",12, 1, 15);
            this.consumesFuelWhenNotHeld = builder
                    .comment("set to false to make the lantern not consume fuel when not held in hand")
                    .define("Lamp consumes fuel when not held", true);
            this.onlyLightWorldWhenHeld = builder
                    .comment("set to false to have the lantern illuminate the world as long as its in the inventory, and not just held in hand")
                    .define("Only illuminate when held", true);
            this.enableFuelConsumption = builder
                    .comment("set to false to make the lanterns not require fuel")
                    .define("Enable fuel consumption", true);
            this.disableWorldLighting = builder
                    .comment("set to true to disable lighting the world from lanterns in item or boat form")
                    .define("Disable world lighting",false);
            this.burnTimeMultiplier = builder
                    .comment("Defines how long burnable items can burn in the lamp, the duration is the furnace burn time is multiplied by this number")
                    .defineInRange("Burn time multiplier", 5, 1, 50);
        }

        @Override
        public int getLightLevel() {
            return this.lanternLightLevel.get();
        }

        @Override
        public boolean consumeFuelWhenNotHeld() {
            return this.consumesFuelWhenNotHeld.get();
        }

        @Override
        public boolean onlyLightWhenHeld() {
            return this.onlyLightWorldWhenHeld.get();
        }

        @Override
        public boolean enableFuelConsumption() {
            return this.enableFuelConsumption.get();
        }

        @Override
        public boolean disableWorldLighting() {
            return disableWorldLighting.get();
        }

        @Override
        public int burnTimeMultiplier() {
            return this.burnTimeMultiplier.get();
        }

        @Override
        public ModConfig.Type getSide() {
            return ModConfig.Type.SERVER;
        }
    }
}
