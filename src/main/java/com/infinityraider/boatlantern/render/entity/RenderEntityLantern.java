package com.infinityraider.boatlantern.render.entity;

import com.infinityraider.boatlantern.block.BlockLantern;
import com.infinityraider.boatlantern.entity.EntityLantern;
import com.infinityraider.boatlantern.registry.BlockRegistry;
import com.infinityraider.boatlantern.render.block.RenderBlockLantern;
import com.infinityraider.infinitylib.render.tessellation.ITessellator;
import com.infinityraider.infinitylib.render.tessellation.TessellatorVertexBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderEntityLantern extends Render<EntityLantern> {
    private final RenderBlockLantern lanternRenderer;

    public RenderEntityLantern(RenderManager renderManager) {
        super(renderManager);
        this.lanternRenderer = ((BlockLantern) BlockRegistry.getInstance().blockLantern).getRenderer();
    }

    @Override
    public void doRender(EntityLantern lantern, double x, double y, double z, float entityYaw, float partialTicks) {
        GlStateManager.pushMatrix();
        GlStateManager.pushAttrib();


        float yaw = lantern.prevRotationYaw + (lantern.rotationYaw - lantern.prevRotationYaw) * partialTicks;
        GlStateManager.translate(x, y, z);
        GlStateManager.rotate(-yaw, 0, 1, 0);
        GlStateManager.translate(-lantern.width, 0, -lantern.width);
        Minecraft.getMinecraft().renderEngine.bindTexture(this.getEntityTexture(lantern));
        ITessellator tessellator = TessellatorVertexBuffer.getInstance(Tessellator.getInstance());

        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        //GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        tessellator.startDrawingQuads(DefaultVertexFormats.BLOCK);
        this.lanternRenderer.drawLantern(tessellator, lantern.isLit());
        tessellator.draw();

        GlStateManager.disableBlend();
        tessellator.startDrawingQuads(DefaultVertexFormats.BLOCK);
        this.lanternRenderer.drawLanternFrame(tessellator);
        tessellator.draw();

        /*
        IBlockState state = BlockLantern.Properties.LIT.applyToBlockState(BlockRegistry.getInstance().blockLantern.getDefaultState(), entity.isLit());
        RenderUtilBase.drawBlockModel(TessellatorVertexBuffer.getInstance(Tessellator.getInstance()), state);
        */

        GlStateManager.popAttrib();
        GlStateManager.popMatrix();
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityLantern entity) {
        return TextureMap.LOCATION_BLOCKS_TEXTURE;
    }
}
