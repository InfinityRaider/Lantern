package com.infinityraider.lantern.render.entity;

import com.infinityraider.infinitylib.block.IInfinityBlock;
import com.infinityraider.infinitylib.render.entity.RenderEntityAsBlock;
import com.infinityraider.lantern.block.BlockLantern;
import com.infinityraider.lantern.entity.EntityLantern;
import com.infinityraider.lantern.registry.BlockRegistry;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderEntityLantern extends RenderEntityAsBlock<EntityLantern> {
    private BlockState lit;
    private BlockState unlit;

    public RenderEntityLantern(EntityRendererManager renderManager) {
        super(renderManager);
    }

    @Override
    protected void applyTransformations(EntityLantern lantern, float yaw, float partialTicks, MatrixStack transforms) {
        Entity mount = lantern.getRidingEntity();
        if(mount == null) {
            mount = lantern;
        }
        float actualYaw = mount.prevRotationYaw + (mount.rotationYaw - mount.prevRotationYaw)*partialTicks;
        transforms.rotate(Vector3f.YP.rotationDegrees(-actualYaw));
        transforms.translate(-lantern.getWidth(), 0, -lantern.getWidth());
    }

    @Override
    protected RenderType getRenderType() {
        return ((IInfinityBlock) BlockRegistry.getInstance().blockLantern).getRenderType();
    }

    @Override
    protected BlockState getBlockState(EntityLantern lantern) {
        return lantern.isLit() ? this.getLitState() : this.getUnlitState();
    }

    private BlockState getLitState() {
        if(this.lit == null) {
            this.lit =  BlockLantern.LIT.apply(BlockRegistry.getInstance().blockLantern.getDefaultState(), true);
        }
        return this.lit;
    }

    private BlockState getUnlitState() {
        if(this.unlit == null) {
            this.unlit =  BlockLantern.LIT.apply(BlockRegistry.getInstance().blockLantern.getDefaultState(), false);
        }
        return this.unlit;
    }
}
