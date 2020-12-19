package com.infinityraider.lantern.proxy;

import com.infinityraider.infinitylib.proxy.base.IClientProxyBase;
import com.infinityraider.lantern.config.Config;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@SuppressWarnings("unused")
@OnlyIn(Dist.CLIENT)
public class ClientProxy implements IProxy, IClientProxyBase<Config> {

}