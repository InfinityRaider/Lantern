package com.infinityraider.lantern.registry;

import com.infinityraider.infinitylib.container.InfinityContainerType;
import com.infinityraider.lantern.container.ContainerLantern;
import com.infinityraider.lantern.reference.Names;
import net.minecraft.inventory.container.ContainerType;

public class ContainerRegistry {
    private static final ContainerRegistry INSTANCE = new ContainerRegistry();

    public static ContainerRegistry getInstance() {
        return INSTANCE;
    }

    public final ContainerType<ContainerLantern> lantern;

    private ContainerRegistry() {
        this.lantern = InfinityContainerType.builder(Names.Blocks.LANTERN, ContainerLantern.Factory.getInstance())
                .setGuiFactory(ContainerLantern.GuiFactory.getInstance())
                .build();
    }
}
