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
import net.minecraft.entity.Entity;
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

        Entity entity = lantern.getRidingEntity();
        if(entity == null) {
            return;
        }

        //Render relative to the boat, because there are issues with the syncing of position between server and client

        double dx = -0.145;
        double dy = 0.25;
        double dz = -0.6;

        float yaw = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks;
        double cosY = Math.cos(Math.toRadians(yaw));
        double sinY = Math.sin(Math.toRadians(yaw));

        double newX = entity.posX + dx * cosY - dz * sinY;
        double newY = entity.posY + dy;
        double newZ = entity.posZ + dx * sinY + dz * cosY;

        double prevX = entity.prevPosX + dx * cosY - dz * sinY;
        double prevY = entity.prevPosY + dy;
        double prevZ = entity.prevPosZ + dx * sinY + dz * cosY;

        double lx = x - (lantern.lastTickPosX + (lantern.posX - lantern.lastTickPosX) * (double) partialTicks);
        double ly = y - (lantern.lastTickPosY + (lantern.posY - lantern.lastTickPosY) * (double) partialTicks);
        double lz = z - (lantern.lastTickPosZ + (lantern.posZ - lantern.lastTickPosZ) * (double) partialTicks);

        double rx = prevX + (newX - prevX) * partialTicks;
        double ry = prevY + (newY - prevY) * partialTicks;
        double rz = prevZ + (newZ - prevZ) * partialTicks;

        GlStateManager.translate(rx + lx, ry + ly, rz + lz);
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
