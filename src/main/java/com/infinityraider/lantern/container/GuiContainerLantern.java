package com.infinityraider.lantern.container;

import com.infinityraider.lantern.reference.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiContainerLantern extends GuiContainer {
    public static final ResourceLocation TEXTURE = new ResourceLocation(Reference.MOD_ID.toLowerCase(), "textures/gui/lantern.png");

    public GuiContainerLantern(ContainerLantern container) {
        super(container);
    }

    public ContainerLantern getContainer() {
        return (ContainerLantern) this.inventorySlots;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().renderEngine.bindTexture(TEXTURE);
        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(x, y, 0, 0, this.xSize, this.ySize);
        if (this.isLit()) {
            this.drawTexturedModalRect(x + 81, y + 28, 176, 0, 14, 14);
        }
    }

    protected boolean isLit() {
        return this.getContainer().isLit();
    }
}
