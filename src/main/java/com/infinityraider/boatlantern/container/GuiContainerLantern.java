package com.infinityraider.boatlantern.container;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiContainerLantern extends GuiContainer {
    public GuiContainerLantern(ContainerLantern container) {
        super(container);
    }

    public ContainerLantern getContainer() {
        return (ContainerLantern) this.inventorySlots;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {

    }
}
