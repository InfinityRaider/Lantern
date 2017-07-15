package com.infinityraider.boatlantern.block;

import com.google.common.collect.ImmutableList;
import com.infinityraider.boatlantern.block.tile.TileEntityLantern;
import com.infinityraider.boatlantern.entity.EntityLantern;
import com.infinityraider.boatlantern.handler.GuiHandler;
import com.infinityraider.boatlantern.handler.LightingHandler;
import com.infinityraider.boatlantern.lantern.ILantern;
import com.infinityraider.boatlantern.lantern.ItemHandlerLantern;
import com.infinityraider.boatlantern.handler.ConfigurationHandler;
import com.infinityraider.boatlantern.lantern.LanternItemCache;
import com.infinityraider.boatlantern.reference.Reference;
import com.infinityraider.boatlantern.render.block.RenderBlockLantern;
import com.infinityraider.infinitylib.block.BlockBaseTile;
import com.infinityraider.infinitylib.block.ICustomRenderedBlock;
import com.infinityraider.infinitylib.block.blockstate.InfinityProperty;
import com.infinityraider.infinitylib.reference.Constants;
import com.infinityraider.infinitylib.utility.IRecipeRegister;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.oredict.ShapedOreRecipe;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class BlockLantern extends BlockBaseTile<TileEntityLantern> implements ICustomRenderedBlock, IRecipeRegister {
    public static final AxisAlignedBB BOUNDING_BOX = new AxisAlignedBB(Constants.UNIT * 5, 0, Constants.UNIT * 5, Constants.UNIT * 11, Constants.UNIT * 11, Constants.UNIT * 11);

    public static final InfinityProperty[] PROPERTIES = new InfinityProperty[] {
            Properties.LIT,
            Properties.FACING_X,
            Properties.HANGING
    };

    @SideOnly(Side.CLIENT)
    private RenderBlockLantern renderer;


    public BlockLantern() {
        super("lantern", Material.CIRCUITS);
        this.setCreativeTab(CreativeTabs.MISC);
    }

    @Override
    @SuppressWarnings("deprecation")
    public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        IBlockState state = this.getDefaultState();
        ItemHandlerLantern lantern = LanternItemCache.getInstance().getLantern(placer, placer.getHeldItemMainhand());
        state = Properties.HANGING.applyToBlockState(state, facing == EnumFacing.DOWN);
        state = Properties.FACING_X.applyToBlockState(state, placer.getHorizontalFacing().getAxis() == EnumFacing.Axis.X);
        state = Properties.LIT.applyToBlockState(state, lantern != null && lantern.isLit());
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
                    if(lit || lantern.getRemainingBurnTicks() > 0 || lantern.consumeFuel()) {
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

    @Override
    @SuppressWarnings("deprecation")
    @Deprecated
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return BOUNDING_BOX;
    }

    @Override
    @SuppressWarnings("deprecation")
    @Deprecated
    @Nullable
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, World worldIn, BlockPos pos) {
        return BOUNDING_BOX;
    }

    @Override
    public void registerRecipes() {
        this.getRecipes().forEach(GameRegistry::addRecipe);
    }

    public List<IRecipe> getRecipes() {
        return ImmutableList.of(new ShapedOreRecipe(this, " s ", "sbs", " p ", 's', "stickWood", 'b', Items.GLASS_BOTTLE, 'p', "slabWood"));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public RenderBlockLantern getRenderer() {
        if(this.renderer == null) {
            this.renderer = new RenderBlockLantern(this);
        }
        return this.renderer;
    }

    public static class BlockItem extends ItemBlock {
        public BlockItem(Block lantern) {
            super(lantern);
            this.setMaxStackSize(1);
        }

        public ItemHandlerLantern getLantern(Entity entity, ItemStack stack) {
            return LanternItemCache.getInstance().getLantern(entity, stack);
        }

        @Override
        public ActionResult<ItemStack> onItemRightClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand) {
            if(!world.isRemote) {
                if(player.isSneaking()) {
                    GuiHandler.getInstance().openGui(player, stack);
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
            return new ActionResult<>(EnumActionResult.PASS, stack);
        }

        public void mountLanternOnBoat(EntityPlayer player, ItemStack stack, EntityBoat boat) {
            if(!player.getEntityWorld().isRemote) {
                EntityLantern entity = new EntityLantern(player);
                ItemHandlerLantern lantern = this.getLantern(player, stack);
                if (lantern != null) {
                    entity.copyFrom(lantern);
                }
                player.getEntityWorld().spawnEntityInWorld(entity);
                player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
                entity.mountOnBoat(boat);
            }
        }

        @Override
        public void onUpdate(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {
            if(shouldLightTheWorld(stack, entity, itemSlot, isSelected)) {
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
        public boolean onEntityItemUpdate(EntityItem entity) {
            if(!entity.getEntityWorld().isRemote) {
                ItemStack stack = entity.getEntityItem();
                ItemHandlerLantern lantern = this.getLantern(entity, stack);
                if (lantern != null && !entity.getEntityWorld().isRemote) {
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
                        ItemStack main = player.getHeldItem(EnumHand.MAIN_HAND);
                        ItemStack off = player.getHeldItem(EnumHand.OFF_HAND);
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
        @SideOnly(Side.CLIENT)
        @SuppressWarnings("deprecation")
        public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
            tooltip.add(I18n.translateToLocal(Reference.MOD_ID.toLowerCase() + ".tooltip.place_lantern"));
            tooltip.add(I18n.translateToLocal(Reference.MOD_ID.toLowerCase() + ".tooltip.open_gui"));
            tooltip.add(I18n.translateToLocal(Reference.MOD_ID.toLowerCase() + ".tooltip.toggle_lantern"));
            tooltip.add(I18n.translateToLocal(Reference.MOD_ID.toLowerCase() + ".tooltip.create_lantern_boat"));
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
