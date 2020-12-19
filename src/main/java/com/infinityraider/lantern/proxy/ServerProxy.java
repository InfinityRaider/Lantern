package com.infinityraider.lantern.proxy;

import com.infinityraider.infinitylib.proxy.base.IServerProxyBase;
import com.infinityraider.lantern.config.Config;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@SuppressWarnings("unused")
@OnlyIn(Dist.DEDICATED_SERVER)
public class ServerProxy implements IProxy, IServerProxyBase<Config> {

}