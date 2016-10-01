package com.infinityraider.boatlantern.proxy;

import com.infinityraider.boatlantern.handler.ConfigurationHandler;
import com.infinityraider.infinitylib.proxy.base.IProxyBase;
import net.minecraftforge.fml.common.event.*;

public interface IProxy extends IProxyBase {
    @Override
    default void initConfiguration(FMLPreInitializationEvent event) {
        ConfigurationHandler.getInstance().init(event);
    }

    @Override
    default void activateRequiredModules() {

    }

    @Override
    default void registerEventHandlers() {}
}