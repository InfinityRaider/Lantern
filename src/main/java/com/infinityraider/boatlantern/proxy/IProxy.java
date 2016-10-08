package com.infinityraider.boatlantern.proxy;

import com.infinityraider.boatlantern.BoatLantern;
import com.infinityraider.boatlantern.handler.*;
import com.infinityraider.boatlantern.lantern.LanternItemCache;
import com.infinityraider.infinitylib.proxy.base.IProxyBase;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.network.NetworkRegistry;

public interface IProxy extends IProxyBase {
    @Override
    default void initStart(FMLInitializationEvent event) {
        NetworkRegistry.INSTANCE.registerGuiHandler(BoatLantern.instance, GuiHandler.getInstance());
    }

    @Override
    default void initConfiguration(FMLPreInitializationEvent event) {
        ConfigurationHandler.getInstance().init(event);
    }

    @Override
    default void activateRequiredModules() {}

    @Override
    default void registerEventHandlers() {
        this.registerEventHandler(InteractionHandler.getInstance());
        this.registerEventHandler(LightingHandler.getInstance());
        this.registerEventHandler(LanternItemCache.getInstance());
    }
}