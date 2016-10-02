package com.infinityraider.boatlantern.handler;

import com.infinityraider.infinitylib.utility.LogHelper;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ConfigurationHandler {
    private static final ConfigurationHandler INSTANCE = new ConfigurationHandler();

    public static ConfigurationHandler getInstance() {
        return INSTANCE;
    }

    private Configuration config;

    public int lanternLightLevel;
    public boolean consumesFuelWhenNotHeld;
    public boolean onlyLightWorldWhenHeld;
    public boolean enableFuelConsumption;
    public boolean disableWorldLighting;
    public int burnTimeMultiplier;

    public void init(FMLPreInitializationEvent event) {
        if (config == null) {
            config = new Configuration(event.getSuggestedConfigurationFile());
        }
        loadConfiguration();
        if (config.hasChanged()) {
            config.save();
        }
        LogHelper.debug("Configuration Loaded");
    }

    private void loadConfiguration() {
        lanternLightLevel = config.getInt("Lantern brightness", Categories.GENERAL.getName(), 12, 1, 15,
                "The light level emitted by the lantern");

        consumesFuelWhenNotHeld = config.getBoolean("Lamp consumes fuel when not held", Categories.GENERAL.getName(), true,
                "set to false to make the lantern not consume fuel when not held in hand");

        onlyLightWorldWhenHeld = config.getBoolean("Only illuminate when held", Categories.GENERAL.getName(), true,
                "set to false to have the lantern illuminate the world as long as its in the inventory, and not just held in hand");

        enableFuelConsumption = config.getBoolean("Enable fuel consumption", Categories.GENERAL.getName(), true,
                "set to false to make the lanterns not require fuel");

        disableWorldLighting = config.getBoolean("Disable world lighting", Categories.GENERAL.getName(), false,
                "set to true to disable lighting hte world from lanters in item or boat form");

        burnTimeMultiplier = config.getInt("Burn time multiplier", Categories.GENERAL.getName(), 5, 1, 50,
                "Defines how long burnable items can burn in the lamp, the duration is the furnace burn time is multiplied by this number");
    }

    public enum Categories {
        GENERAL("general");

        private final String name;

        Categories(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

}
