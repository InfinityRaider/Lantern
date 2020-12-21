package com.infinityraider.lantern.item;

import com.google.common.collect.ImmutableSet;
import com.infinityraider.infinitylib.block.IInfinityBlock;
import com.infinityraider.infinitylib.item.BlockItemBase;
import com.infinityraider.infinitylib.item.InfinityItemProperty;
import com.infinityraider.lantern.Lantern;
import com.infinityraider.lantern.block.BlockLantern;
import com.infinityraider.lantern.container.ContainerLantern;
import com.infinityraider.lantern.entity.EntityLantern;
import com.infinityraider.lantern.handler.LightingHandler;
import com.infinityraider.lantern.lantern.ILantern;
import com.infinityraider.lantern.lantern.ItemHandlerLantern;
import com.infinityraider.lantern.lantern.LanternItemCache;
import com.infinityraider.lantern.reference.Reference;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Set;

public class ItemLantern extends BlockItemBase {
    private final BlockLantern block;

    public ItemLantern(IInfinityBlock block) {
        super(block, new Properties()
                .group(ItemGroup.MISC)
                .maxStackSize(1));
        this.block = (BlockLantern) block;
    }

    public ItemHandlerLantern getLantern(Entity entity, ItemStack stack) {
        return LanternItemCache.getInstance().getLantern(entity, stack);
    }

    @Override
    public void onUse(World worldIn, LivingEntity livingEntityIn, ItemStack stack, int count) {
        super.onUse(worldIn, livingEntityIn, stack, count);
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getPos();
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        PlayerEntity player = context.getPlayer();
        Direction face = context.getFace();
        ItemStack stack = context.getItem();
        BlockItemUseContext blockContext = new BlockItemUseContext(context);
        if (!block.isReplaceable(state, blockContext)) {
            pos = pos.offset(context.getFace());
        }
        if (stack.getCount() != 0 && player.canPlayerEdit(pos, face, stack) && blockContext.canPlace()) {
            BlockState newState = this.block.getStateForPlacement(blockContext);
            if (newState != null && newState.isValidPosition(world, pos) && placeBlockAt(stack, player, world, pos, newState)) {
                SoundType soundtype = this.block.getSoundType(state, world, pos, player);
                world.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                stack.setCount(stack.getCount() - 1);
            }
            return ActionResultType.SUCCESS;
        } else {
            return ActionResultType.FAIL;
        }
    }

    public boolean placeBlockAt(ItemStack stack, PlayerEntity player, World world, BlockPos pos, BlockState newState) {
        if (!world.setBlockState(pos, newState, 3)) {
            return false;
        }
        BlockState state = world.getBlockState(pos);
        if (state.getBlock() == this.block) {
            this.block.onBlockPlacedBy(world, pos, state, player, stack);
        }
        return true;
    }


    @Override
    @ParametersAreNonnullByDefault
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if(!world.isRemote) {
            if(player.isSneaking()) {
                ContainerLantern.open(player, hand, stack);
            } else {
                ILantern lantern = this.getLantern(player, stack);
                if(lantern != null) {
                    boolean lit = lantern.isLit();
                    if(lit || lantern.getRemainingBurnTicks() > 0 || lantern.consumeFuel()) {
                        lantern.setLit(!lit);
                    }
                }
            }
        }
        return new ActionResult<>(ActionResultType.PASS, stack);
    }

    public void mountLanternOnBoat(PlayerEntity player, ItemStack stack, BoatEntity boat) {
        if(!player.getEntityWorld().isRemote) {
            EntityLantern entity = new EntityLantern(player);
            ItemHandlerLantern lantern = this.getLantern(player, stack);
            if (lantern != null) {
                entity.copyFrom(lantern);
            }
            player.getEntityWorld().addEntity(entity);
            entity.mountOnBoat(boat);
            if(!player.isCreative()) {
                player.inventory.setInventorySlotContents(player.inventory.currentItem, ItemStack.EMPTY);
            }
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {
        if(shouldLightTheWorld(stack, entity, isSelected)) {
            ItemHandlerLantern lantern = this.getLantern(entity, stack);
            if(lantern != null && !world.isRemote) {
                lantern.updateTick();
                LightingHandler.getInstance().playerLightTick(entity);
            }
        }
    }

    @Override
    public int getEntityLifespan(ItemStack stack, World world) {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        if(!entity.getEntityWorld().isRemote) {
            ItemHandlerLantern lantern = this.getLantern(entity, stack);
            if (lantern != null && !entity.getEntityWorld().isRemote) {
                lantern.updateTick();
                entity.setItem(stack);
            }
        }
        return false;
    }

    public boolean isLit(Entity entity, ItemStack stack) {
        ItemHandlerLantern lantern = this.getLantern(entity, stack);
        return lantern != null && lantern.isLit();
    }

    protected boolean shouldLightTheWorld(ItemStack stack, Entity entity, boolean isSelected) {
        if(this.isLit(entity, stack)) {
            if (Lantern.instance.getConfig().onlyLightWhenHeld()) {
                if(entity instanceof LivingEntity) {
                    LivingEntity living = (LivingEntity) entity;
                    ItemStack main = living.getHeldItem(Hand.MAIN_HAND);
                    ItemStack off = living.getHeldItem(Hand.OFF_HAND);
                    return isSelected || stack == main || stack == off;
                } else {
                    return isSelected;
                }
            } else {
                return true;
            }
        }
        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    @SuppressWarnings("deprecation")
    public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag advanced) {
        tooltip.add(new TranslationTextComponent(Reference.MOD_ID.toLowerCase() + ".tooltip.place_lantern"));
        tooltip.add(new TranslationTextComponent(Reference.MOD_ID.toLowerCase() + ".tooltip.open_gui"));
        tooltip.add(new TranslationTextComponent(Reference.MOD_ID.toLowerCase() + ".tooltip.toggle_lantern"));
        tooltip.add(new TranslationTextComponent(Reference.MOD_ID.toLowerCase() + ".tooltip.create_lantern_boat"));
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        if(oldStack == null) {
            return newStack != null;
        } else {
            return newStack == null || oldStack.getItem() != newStack.getItem();
        }
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, CompoundNBT tag) {
        return new ICapabilityProvider() {
            @Override
            public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing) {
                if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
                    LazyOptional<IItemHandler> cap = LazyOptional.of(() -> new ItemHandlerLantern(stack));
                    return cap.cast();
                } else {
                    return LazyOptional.empty();
                }
            }
        };
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public Set<InfinityItemProperty> getModelProperties() {
        return ImmutableSet.of(new InfinityItemProperty(new ResourceLocation(Lantern.instance.getModId(), BlockLantern.LIT.getName())) {
            @Override
            public float getValue(ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity entity) {
                return (!stack.isEmpty()
                        && stack.getItem() instanceof ItemLantern
                        && ((ItemLantern) stack.getItem()).isLit(entity, stack)) ? 1 : 0;
            }
        });
    }
}
