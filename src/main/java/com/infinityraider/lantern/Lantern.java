package com.infinityraider.lantern;

import com.infinityraider.lantern.config.Config;
import com.infinityraider.lantern.network.MessageApplyLanternToBoat;
import com.infinityraider.lantern.proxy.ClientProxy;
import com.infinityraider.lantern.proxy.IProxy;
import com.infinityraider.lantern.proxy.ServerProxy;
import com.infinityraider.lantern.reference.Reference;
import com.infinityraider.lantern.registry.*;
import com.infinityraider.infinitylib.InfinityMod;
import com.infinityraider.infinitylib.network.INetworkWrapper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.Mod;

@Mod(Reference.MOD_ID)
public class Lantern extends InfinityMod<IProxy, Config> {
    public static Lantern instance;

    @Override
    public String getModId() {
        return Reference.MOD_ID;
    }

    @Override
    protected void onModConstructed() {
        instance = this;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    protected IProxy createClientProxy() {
        return new ClientProxy();
    }

    @Override
    @OnlyIn(Dist.DEDICATED_SERVER)
    protected IProxy createServerProxy() {
        return new ServerProxy();
    }

    @Override
    public BlockRegistry getModBlockRegistry() {
        return BlockRegistry.getInstance();
    }

    @Override
    public TileRegistry getModTileRegistry() {
        return TileRegistry.getInstance();
    }

    @Override
    public ItemRegistry getModItemRegistry() {
        return ItemRegistry.getInstance();
    }

    @Override
    public EntityRegistry getModEntityRegistry() {
        return EntityRegistry.getInstance();
    }

    @Override
    public ContainerRegistry getModContainerRegistry() {
        return ContainerRegistry.getInstance();
    }

    @Override
    public void registerMessages(INetworkWrapper wrapper) {
        wrapper.registerMessage(MessageApplyLanternToBoat.class);
    }
}
