package com.infinityraider.boatlantern.render;

import com.infinityraider.boatlantern.block.BlockLantern;
import com.infinityraider.boatlantern.entity.EntityBoatLantern;
import com.infinityraider.boatlantern.registry.BlockRegistry;
import com.infinityraider.infinitylib.render.RenderUtilBase;
import com.infinityraider.infinitylib.render.tessellation.TessellatorVertexBuffer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.model.ModelBoat;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderEntityBoatLantern extends Render<EntityBoatLantern> {
    // Because the one in RenderBoat is private >>
    public static final ResourceLocation[] BOAT_TEXTURES = new ResourceLocation[] {
            new ResourceLocation("textures/entity/boat/boat_oak.png"),
            new ResourceLocation("textures/entity/boat/boat_spruce.png"),
            new ResourceLocation("textures/entity/boat/boat_birch.png"),
            new ResourceLocation("textures/entity/boat/boat_jungle.png"),
            new ResourceLocation("textures/entity/boat/boat_acacia.png"),
            new ResourceLocation("textures/entity/boat/boat_darkoak.png")};

    protected final ModelBoat model;

    public RenderEntityBoatLantern(RenderManager renderManager) {
        super(renderManager);
        this.model = new ModelBoat();
        this.shadowSize = 0.5F;
    }

    @Override
    public void doRender(EntityBoatLantern entity, double x, double y, double z, float entityYaw, float partialTicks) {
        GlStateManager.pushMatrix();
        this.setupTranslation(x, y, z);
        this.setupRotation(entity, entityYaw, partialTicks);
        this.bindEntityTexture(entity);

        if (this.renderOutlines) {
            GlStateManager.enableColorMaterial();
            GlStateManager.enableOutlineMode(this.getTeamColor(entity));
        }

        this.model.render(entity, partialTicks, 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F);
        this.renderLantern(entity);

        if (this.renderOutlines) {
            GlStateManager.disableOutlineMode();
            GlStateManager.disableColorMaterial();
        }

        GlStateManager.popMatrix();
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    public void renderLantern(EntityBoatLantern entity) {
        IBlockState state = BlockLantern.Properties.LIT.applyToBlockState(BlockRegistry.getInstance().blockLantern.getDefaultState(), entity.isLit());

        GlStateManager.pushMatrix();
        GlStateManager.pushAttrib();

        GlStateManager.translate(-1.0, 0.125, 0.5);
        GlStateManager.rotate(180, 1, 0, 0);

        GlStateManager.enableAlpha();

        RenderUtilBase.drawBlockModel(TessellatorVertexBuffer.getInstance(Tessellator.getInstance()), state);

        GlStateManager.popAttrib();
        GlStateManager.popMatrix();
    }

    public void setupRotation(EntityBoat boat, float entityYaw, float partialTicks) {
        GlStateManager.rotate(180.0F - entityYaw, 0.0F, 1.0F, 0.0F);
        float time = (float) boat.getTimeSinceHit() - partialTicks;
        float dmg = boat.getDamageTaken() - partialTicks;

        if (dmg < 0.0F) {
            dmg = 0.0F;
        }

        if (time > 0.0F) {
            GlStateManager.rotate(MathHelper.sin(time) * time * dmg / 10.0F * (float)boat.getForwardDirection(), 1.0F, 0.0F, 0.0F);
        }

        GlStateManager.scale(-1.0F, -1.0F, 1.0F);
    }

    public void setupTranslation(double x, double y, double z) {
        GlStateManager.translate((float)x, (float)y + 0.375F, (float)z);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityBoatLantern entity) {
        return BOAT_TEXTURES[entity.getBoatType().ordinal()];
    }

    public boolean isMultipass()
    {
        return true;
    }

    public void renderMultipass(EntityBoatLantern boat, double x, double y, double z, float entityYaw, float partialTicks) {
        GlStateManager.pushMatrix();
        this.setupTranslation(x, y, z);
        this.setupRotation(boat, entityYaw, partialTicks);
        this.bindEntityTexture(boat);
        this.model.renderMultipass(boat, partialTicks, 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F);
        GlStateManager.popMatrix();
    }
}
