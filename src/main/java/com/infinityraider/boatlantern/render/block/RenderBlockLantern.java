package com.infinityraider.boatlantern.render.block;

import com.google.common.collect.ImmutableList;
import com.infinityraider.boatlantern.block.BlockLantern;
import com.infinityraider.boatlantern.lantern.ItemHandlerLantern;
import com.infinityraider.boatlantern.lantern.LanternItemCache;
import com.infinityraider.boatlantern.reference.Reference;
import com.infinityraider.infinitylib.reference.Constants;
import com.infinityraider.infinitylib.render.block.RenderBlockBase;
import com.infinityraider.infinitylib.render.tessellation.ITessellator;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import java.util.List;

public class RenderBlockLantern extends RenderBlockBase<BlockLantern> {
    public static final ResourceLocation BRONZE = new ResourceLocation(Reference.MOD_ID.toLowerCase(), "blocks/bronze");
    public static final ResourceLocation GLASS_LIT = new ResourceLocation(Reference.MOD_ID.toLowerCase(), "blocks/lanternglass_lit");
    public static final ResourceLocation GLASS_UNLIT = new ResourceLocation(Reference.MOD_ID.toLowerCase(), "blocks/lanternglass_unlit");
    public static final ResourceLocation WHITE = new ResourceLocation(Reference.MOD_ID.toLowerCase(), "blocks/white");

    public RenderBlockLantern(BlockLantern block) {
        super(block, true);
    }

    @Override
    public List<ResourceLocation> getAllTextures() {
        return ImmutableList.of(BRONZE, GLASS_LIT, GLASS_UNLIT, WHITE);
    }

    @Override
    public void renderWorldBlockStatic(ITessellator tessellator, IBlockState state, BlockLantern block, EnumFacing side) {
        float u = Constants.UNIT;

        if(BlockLantern.Properties.FACING_X.getValue(state)) {
            tessellator.translate(0, 0, 1);
            tessellator.rotate(90, 0, 1, 0);
        }
        if(BlockLantern.Properties.HANGING.getValue(state)) {
            tessellator.translate(0, 5 * u, 0);
        }

        drawLanternFrame(tessellator);
        drawLantern(tessellator, BlockLantern.Properties.LIT.getValue(state));
    }

    @Override
    public void renderInventoryBlock(ITessellator tessellator, World world, IBlockState state, BlockLantern block, ItemStack stack, EntityLivingBase entity, ItemCameraTransforms.TransformType type) {
        ItemHandlerLantern lantern = LanternItemCache.getInstance().getLantern(entity, stack);
        applyTransforms(tessellator, type);
        drawLanternFrame(tessellator);
        drawLantern(tessellator, lantern != null && lantern.isLit());
    }

    protected void applyTransforms(ITessellator tessellator, ItemCameraTransforms.TransformType type) {
        switch(type) {
            case FIRST_PERSON_LEFT_HAND:
            case FIRST_PERSON_RIGHT_HAND:
                tessellator.rotate(90, 0, 1, 0);
                break;
        }
        tessellator.translate(-0.5F, 0, -0.5F);
        tessellator.scale(2, 2, 2);
    }

    //Credits for model to Wiresegal (aka yrsegal)
    public void drawLantern(ITessellator tessellator, boolean lit) {
        if(lit) {
            TextureAtlasSprite glass_lit = getIcon(GLASS_LIT);
            TextureAtlasSprite white = getIcon(WHITE);

            tessellator.drawScaledPrism(6.5F, 4, 6.5F, 9.5F, 7, 9.5F, glass_lit);
            tessellator.drawScaledPrism(7, 4.5F, 7, 9, 6.5F, 9, white);
        } else {
            TextureAtlasSprite glass_unlit = getIcon(GLASS_UNLIT);

            tessellator.drawScaledPrism(6.5F, 4, 6.5F, 9.5F, 7, 9.5F, glass_unlit);
        }
    }

    public void drawLanternFrame(ITessellator tessellator) {
        TextureAtlasSprite bronze = getIcon(BRONZE);

        tessellator.drawScaledPrism(5.5F, 0, 5.5F, 10.5F, 1, 10.5F, bronze);
        tessellator.drawScaledPrism(6, 1, 6, 10, 3, 10, bronze);
        tessellator.drawScaledPrism(7, 3, 7, 9, 4, 9, bronze);
        tessellator.drawScaledPrism(5, 2, 7.5F, 6, 8.5F, 8.5F, bronze);
        tessellator.drawScaledPrism(10, 2, 7.5F, 11, 8.5F, 8.5F, bronze);
        tessellator.drawScaledPrism(6, 8, 7, 10, 9, 9, bronze);
        tessellator.drawScaledPrism(7.5F, 7, 7.5F, 8.5F, 8, 8.5F, bronze);
        tessellator.drawScaledPrism(6.5F, 9, 7.5F, 7.5F, 11, 8.5F, bronze);
        tessellator.drawScaledPrism(8.5F, 9, 7.5F, 9.5F, 11, 8.5F, bronze);
        tessellator.drawScaledPrism(7.5F, 10, 7.5F, 8.5F, 11, 8.5F, bronze);
    }

    @Override
    public TextureAtlasSprite getIcon() {
        return getIcon(BRONZE);
    }

    @Override
    public boolean applyAmbientOcclusion() {
        return false;
    }
}
