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

    public boolean consumesFuelWhenNotHeld;
    public boolean onlyLightWorldWhenHeld;
    public boolean enableFuelConsumption;

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
        consumesFuelWhenNotHeld = config.getBoolean("Lamp consumes fuel when not held", Categories.GENERAL.getName(), true,
                "set to false to make the lantern not consume fuel when not held in hand");

        onlyLightWorldWhenHeld = config.getBoolean("Only illuminate when held", Categories.GENERAL.getName(), true,
                "set to false to have the lantern illuminate the world as long as its in the inventory, and not just held in hand");

        enableFuelConsumption = config.getBoolean("Enable fuel consumption", Categories.GENERAL.getName(), true,
                "set to false to make the lanterns not require fuel");
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
