package com.infinityraider.boatlantern.proxy;

import com.infinityraider.infinitylib.proxy.base.IClientProxyBase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
@SuppressWarnings("unused")
public class ClientProxy implements IProxy, IClientProxyBase {
    @Override
    public void registerEventHandlers() {
        IProxy.super.registerEventHandlers();
    }
}