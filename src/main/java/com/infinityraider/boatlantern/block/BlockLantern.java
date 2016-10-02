package com.infinityraider.boatlantern.block;

import com.google.common.collect.ImmutableList;
import com.infinityraider.boatlantern.block.tile.TileEntityLantern;
import com.infinityraider.boatlantern.handler.GuiHandler;
import com.infinityraider.boatlantern.lantern.ItemHandlerLantern;
import com.infinityraider.boatlantern.handler.ConfigurationHandler;
import com.infinityraider.boatlantern.lantern.LanternItemCache;
import com.infinityraider.infinitylib.block.BlockBaseTile;
import com.infinityraider.infinitylib.block.blockstate.InfinityProperty;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class BlockLantern extends BlockBaseTile<TileEntityLantern> {
    public static final InfinityProperty[] PROPERTIES = new InfinityProperty[] {
            Properties.LIT,
            Properties.FACING_X,
            Properties.HANGING
    };

    public BlockLantern() {
        super("lantern", Material.CIRCUITS);
    }

    @Override
    public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        IBlockState state = this.getDefaultState();
        Properties.HANGING.applyToBlockState(state, hitY > 0.5);
        Properties.FACING_X.applyToBlockState(state, placer.getHorizontalFacing().getAxis() == EnumFacing.Axis.X);
        ItemHandlerLantern lantern = LanternItemCache.getInstance().getLantern(placer, placer.getHeldItemMainhand());
        Properties.LIT.applyToBlockState(state, lantern != null && lantern.isLit());
        return state;
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase entity, ItemStack stack) {
        super.onBlockPlacedBy(world, pos, state, entity, stack);
        ItemHandlerLantern lantern = LanternItemCache.getInstance().getLantern(stack);
        TileEntity te = world.getTileEntity(pos);
        if(te instanceof TileEntityLantern && lantern != null) {
            ((TileEntityLantern) te).copyFrom(lantern);
        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, ItemStack stack, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            TileEntity te = world.getTileEntity(pos);
            if(te instanceof TileEntityLantern) {
                TileEntityLantern lantern = (TileEntityLantern) te;
                if (player.isSneaking()) {
                    GuiHandler.getInstance().openGui(player, lantern);
                } else {
                    boolean lit = Properties.LIT.getValue(state);
                    if(lit || lantern.getRemainingBurnTicks() > 0) {
                        world.setBlockState(pos, Properties.LIT.applyToBlockState(state, !lit));
                    }
                }
            }
        }
        return true;
    }

    @Override
    public void onBlockClicked(World world, BlockPos pos, EntityPlayer player) {
        if(!world.isRemote) {
            this.dropBlockAsItem(world, pos, world.getBlockState(pos), 0);
            world.removeTileEntity(pos);
            world.setBlockToAir(pos);
        }
    }

    @Override
    public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        Item item = this.getItemDropped(state, new Random(),fortune);
        if(item != null) {
            ItemStack stack = new ItemStack(item, 1, 0);
            ItemHandlerLantern lantern = LanternItemCache.getInstance().getLantern(stack);
            TileEntity te = world.getTileEntity(pos);
            if(lantern != null && te instanceof TileEntityLantern) {
                lantern.copyFrom((TileEntityLantern) te);
            }
            return ImmutableList.of(stack);
        }
        return Collections.emptyList();
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block) {
        TileEntity te = world.getTileEntity(pos);
        if(te instanceof TileEntityLantern) {
            ((TileEntityLantern) te).resetState();
        }
    }

    @Override
    public TileEntityLantern createNewTileEntity(World worldIn, int meta) {
        return new TileEntityLantern();
    }

    @Override
    protected InfinityProperty[] getPropertyArray() {
        return PROPERTIES;
    }

    @Override
    @SuppressWarnings("deprecation")
    public IBlockState getStateFromMeta(int meta) {
        IBlockState state = this.getDefaultState();
        Properties.LIT.applyToBlockState(state, (meta & 1) == 1);
        Properties.FACING_X.applyToBlockState(state, ( (meta >> 1) & 1) == 1);
        Properties.FACING_X.applyToBlockState(state, ( (meta >> 2) & 1) == 1);
        return state;
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int meta = 0;
        if(Properties.LIT.getValue(state)) {
            meta += 1;
        }
        if(Properties.FACING_X.getValue(state)) {
            meta += 2;
        }
        if(Properties.FACING_X.getValue(state)) {
            meta += 4;
        }
        return meta;
    }

    @Override
    public Class<? extends ItemBlock> getItemBlockClass() {
        return BlockItem.class;
    }

    @Override
    @SuppressWarnings("deprecation")
    public int getLightValue(IBlockState state) {
        return Properties.LIT.getValue(state) ? ConfigurationHandler.getInstance().lanternLightLevel : 0;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.TRANSLUCENT;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    public static class BlockItem extends ItemBlock {
        public BlockItem(Block lantern) {
            super(lantern);
            this.setMaxStackSize(1);
            this.setCreativeTab(CreativeTabs.MISC);
        }

        public ItemHandlerLantern getLantern(Entity entity, ItemStack stack) {
            return LanternItemCache.getInstance().getLantern(entity, stack);
        }

        @Override
        public void onUpdate(ItemStack stack, World worldIn, Entity entity, int itemSlot, boolean isSelected) {
            if(shouldLightTheWorld(stack, entity, itemSlot, isSelected)) {
                ItemHandlerLantern lantern = this.getLantern(entity, stack);
                if(lantern != null) {
                    lantern.updateTick();
                }
            }
        }

        @Override
        public int getEntityLifespan(ItemStack stack, World world) {
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean onEntityItemUpdate(EntityItem entity) {
            if(!entity.getEntityWorld().isRemote) {
                ItemStack stack = entity.getEntityItem();
                ItemHandlerLantern lantern = this.getLantern(entity, stack);
                if (lantern != null) {
                    lantern.updateTick();
                    entity.setEntityItemStack(stack);
                }
            }
            return false;
        }

        public boolean isLit(Entity entity, ItemStack stack) {
            ItemHandlerLantern lantern = this.getLantern(entity, stack);
            return lantern != null && lantern.isLit();
        }

        protected boolean shouldLightTheWorld(ItemStack stack, Entity entity, int slot, boolean isSelected) {
            if(this.isLit(entity, stack)) {
                if (ConfigurationHandler.getInstance().onlyLightWorldWhenHeld) {
                    if(entity instanceof EntityPlayer) {
                        EntityPlayer player = (EntityPlayer) entity;
                        return isSelected || slot == player.inventory.currentItem || slot == 40;
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
        public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
            if(oldStack == null) {
                return newStack != null;
            } else {
                return newStack == null || oldStack.getItem() != newStack.getItem();
            }
        }

        @Override
        public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound tag) {
            return new ICapabilityProvider() {
                @Override
                public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
                    return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;
                }

                @Override
                public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
                    if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
                        return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(new ItemHandlerLantern(stack));
                    } else {
                        return null;
                    }
                }
            };
        }
    }

    public static class Properties {
        public static final InfinityProperty<Boolean> LIT = new InfinityProperty<>(PropertyBool.create("lit"), false);
        public static final InfinityProperty<Boolean> FACING_X = new InfinityProperty<>(PropertyBool.create("facing_x"), true);
        public static final InfinityProperty<Boolean> HANGING = new InfinityProperty<>(PropertyBool.create("hanging"), false);
    }

}
