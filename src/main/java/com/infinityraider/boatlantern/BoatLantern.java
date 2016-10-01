package com.infinityraider.boatlantern;

import com.infinityraider.boatlantern.proxy.IProxy;
import com.infinityraider.boatlantern.reference.Reference;
import com.infinityraider.boatlantern.registry.BlockRegistry;
import com.infinityraider.boatlantern.registry.EntityRegistry;
import com.infinityraider.infinitylib.InfinityMod;
import com.infinityraider.infinitylib.network.INetworkWrapper;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;

@Mod(
        modid = Reference.MOD_ID,
        name = Reference.MOD_NAME,
        version = Reference.MOD_VERSION,
        dependencies = "required-after:infinitylib"
)
public class BoatLantern extends InfinityMod {
    @Mod.Instance(Reference.MOD_ID)
    @SuppressWarnings("unused")
    public static BoatLantern instance;

    @SidedProxy(clientSide = Reference.CLIENT_PROXY_CLASS, serverSide = Reference.SERVER_PROXY_CLASS)
    public static IProxy proxy;

    @Override
    public IProxy proxy() {
        return proxy;
    }

    @Override
    public String getModId() {
        return Reference.MOD_ID;
    }

    @Override
    public Object getModBlockRegistry() {
        return BlockRegistry.getInstance();
    }

    @Override
    public Object getModItemRegistry() {
        return this;
    }

    @Override
    public Object getModEntityRegistry() {
        return EntityRegistry.getInstance();
    }

    @Override
    public void registerMessages(INetworkWrapper wrapper) {}

    @Mod.EventHandler
    @SuppressWarnings("unused")
    public void preInitMod(FMLPreInitializationEvent event) {
        super.preInit(event);
    }

    @Mod.EventHandler
    @SuppressWarnings("unused")
    public void initMod(FMLInitializationEvent event) {
        super.init(event);
    }

    @Mod.EventHandler
    @SuppressWarnings("unused")
    public void postInitMod(FMLPostInitializationEvent event) {
        super.postInit(event);
    }

    @Mod.EventHandler
    @SuppressWarnings("unused")
    public void serverAboutToStart(FMLServerAboutToStartEvent event) {
        super.onServerAboutToStart(event);
    }
}
