package com.infinityraider.lantern.proxy;

import com.infinityraider.lantern.config.Config;
import com.infinityraider.lantern.handler.*;
import com.infinityraider.lantern.lantern.LanternItemCache;
import com.infinityraider.infinitylib.proxy.base.IProxyBase;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.function.Function;

public interface IProxy extends IProxyBase<Config> {
    @Override
    default Function<ForgeConfigSpec.Builder, Config> getConfigConstructor() {
        return Config.Common::new;
    }

    @Override
    default void activateRequiredModules() {}

    @Override
    default void registerCapabilities() {}

    @Override
    default void registerEventHandlers() {
        this.registerEventHandler(InteractionHandler.getInstance());
        this.registerEventHandler(LightingHandler.getInstance());
        this.registerEventHandler(LanternItemCache.getInstance());
    }
}