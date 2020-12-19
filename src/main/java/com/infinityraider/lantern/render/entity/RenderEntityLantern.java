package com.infinityraider.lantern.render.entity;

import com.infinityraider.infinitylib.render.IRenderUtilities;
import com.infinityraider.lantern.block.BlockLantern;
import com.infinityraider.lantern.entity.EntityLantern;
import com.infinityraider.lantern.registry.BlockRegistry;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderEntityLantern extends EntityRenderer<EntityLantern> implements IRenderUtilities {

    public RenderEntityLantern(EntityRendererManager renderManager) {
        super(renderManager);
    }

    @Override
    public void render(EntityLantern lantern, float entityYaw, float partialTicks, MatrixStack transforms, IRenderTypeBuffer buffer, int packedLightIn) {
        transforms.push();

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

        double newX = entity.getPosX() + dx * cosY - dz * sinY;
        double newY = entity.getPosY() + dy;
        double newZ = entity.getPosZ() + dx * sinY + dz * cosY;

        double prevX = entity.prevPosX + dx * cosY - dz * sinY;
        double prevY = entity.prevPosY + dy;
        double prevZ = entity.prevPosZ + dx * sinY + dz * cosY;

        double lx = (lantern.lastTickPosX + (lantern.getPosX() - lantern.lastTickPosX) * (double) partialTicks);
        double ly = (lantern.lastTickPosY + (lantern.getPosY() - lantern.lastTickPosY) * (double) partialTicks);
        double lz = (lantern.lastTickPosZ + (lantern.getPosZ() - lantern.lastTickPosZ) * (double) partialTicks);

        double rx = prevX + (newX - prevX) * partialTicks;
        double ry = prevY + (newY - prevY) * partialTicks;
        double rz = prevZ + (newZ - prevZ) * partialTicks;

        transforms.translate(rx + lx, ry + ly, rz + lz);
        transforms.rotate(Vector3f.YP.rotationDegrees(-yaw));
        transforms.translate(-lantern.getWidth(), 0, -lantern.getWidth());

        BlockState state = BlockLantern.LIT.apply(BlockRegistry.getInstance().blockLantern.getDefaultState(), lantern.isLit());

        //TODO: render block model

        transforms.pop();
    }

    @Override
    public ResourceLocation getEntityTexture(EntityLantern entity) {
        return this.getTextureAtlasLocation();
    }
}
