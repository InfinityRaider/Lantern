package com.infinityraider.boatlantern.entity;

import com.infinityraider.boatlantern.handler.GuiHandler;
import com.infinityraider.boatlantern.lantern.*;
import com.infinityraider.boatlantern.handler.LightingHandler;
import com.infinityraider.boatlantern.reference.Names;
import com.infinityraider.boatlantern.registry.BlockRegistry;
import com.infinityraider.boatlantern.render.RenderEntityBoatLantern;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nullable;

public class EntityBoatLantern extends EntityBoat implements ILantern, IInventoryLantern {
    public static final DataParameter<Boolean> DATA_LIT = EntityDataManager.createKey(EntityBoatLantern.class, DataSerializers.BOOLEAN);
    public static final DataParameter<Integer> DATA_BURN_TICKS = EntityDataManager.createKey(EntityBoatLantern.class, DataSerializers.VARINT);

    private final LanternLogic lanternLogic = new LanternLogic(this);
    private ItemStack fuelStack;

    /** Constructor which is used to instantiate the entity client side or server side after a world reload using reflection */
    @SuppressWarnings("unused")
    public EntityBoatLantern(World world) {
        super(world);
    }

    public EntityBoatLantern(World world, double x, double y, double z) {
        super(world, x, y, z);
    }

    public EntityBoatLantern(EntityBoat boat, ItemStack stack) {
        this(boat.getEntityWorld(), boat.prevPosX, boat.prevPosY, boat.prevPosZ);
        this.posX = boat.posX;
        this.posY = boat.posY;
        this.posZ = boat.posZ;
        this.motionX = boat.motionX;
        this.motionY = boat.motionY;
        this.motionZ = boat.motionZ;
        this.rotationPitch = boat.rotationPitch;
        this.rotationYaw = boat.rotationYaw;
        this.setBoatType(boat.getBoatType());
        ItemHandlerLantern lantern = LanternItemCache.getInstance().getLantern(stack);
        if(lantern != null) {
            this.copyFrom(lantern);
        }
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.getDataManager().register(DATA_LIT, false);
        this.getDataManager().register(DATA_BURN_TICKS, 0);
    }

    @Override
    public IInventoryLantern getInventory() {
        return this;
    }

    @Override
    public void setLit(boolean status) {
        boolean lit = this.isLit();
        if(lit && !status) {
            LightingHandler.getInstance().removeLastLight(this);
        }
        this.getDataManager().set(DATA_LIT, status);
    }

    @Override
    public boolean isLit() {
        return this.getDataManager().get(DATA_LIT);
    }

    @Override
    public ILantern getLantern() {
        return this;
    }

    @Override
    public ItemStack getFuelStack() {
        return this.fuelStack;
    }

    @Override
    public IInventoryLantern setFuelStack(ItemStack stack) {
        this.fuelStack = stack;
        return this;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        this.lanternLogic.burnUpdate();
    }

    @Override
    public int getRemainingBurnTicks() {
        return this.getDataManager().get(DATA_BURN_TICKS);
    }

    public EntityBoatLantern setBurnTicks(int ticks) {
        ticks = ticks < 0 ? 0 : ticks;
        this.getDataManager().set(DATA_BURN_TICKS, ticks);
        return this;
    }

    @Override
    public EntityBoatLantern addBurnTicks(int ticks) {
        return this.setBurnTicks(this.getRemainingBurnTicks() + ticks);
    }

    @Override
    public void spreadLight() {
        LightingHandler.getInstance().spreadLight(this);
    }

    @Override
    public boolean processInitialInteract(EntityPlayer player, @Nullable ItemStack stack, EnumHand hand) {
        if (player.isSneaking()) {
            if (!this.worldObj.isRemote) {
                GuiHandler.getInstance().openGui(player, this);
            }
        } else {
            if (player.isRidingOrBeingRiddenBy(this)) {
                boolean lit = this.isLit();
                if (lit || this.getRemainingBurnTicks() > 0 || this.consumeFuel()) {
                    this.setLit(!lit);
                }
            } else {
                return super.processInitialInteract(player, stack, hand);
            }
        }
        return true;
    }

    /**
     * Overridden to drop the lantern too
     */
    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (this.isEntityInvulnerable(source)) {
            return false;
        } else if (!this.worldObj.isRemote && !this.isDead) {
            if (source instanceof EntityDamageSourceIndirect && source.getEntity() != null && this.isPassenger(source.getEntity())) {
                return false;
            } else {
                this.setForwardDirection(-this.getForwardDirection());
                this.setTimeSinceHit(10);
                this.setDamageTaken(this.getDamageTaken() + amount * 10.0F);
                this.setBeenAttacked();
                boolean flag = source.getEntity() instanceof EntityPlayer && ((EntityPlayer)source.getEntity()).capabilities.isCreativeMode;
                if (flag || this.getDamageTaken() > 40.0F) {
                    if (!flag && this.worldObj.getGameRules().getBoolean("doEntityDrops")) {
                        this.dropItems();
                    }
                    this.setDead();
                }
                return true;
            }
        } else {
            return true;
        }
    }

    public void dropItems() {
        this.dropItemWithOffset(this.getItemBoat(), 1, 0.0F);
        ItemStack stack = new ItemStack(BlockRegistry.getInstance().blockLantern, 1, 0);
        ItemHandlerLantern lantern = LanternItemCache.getInstance().getLantern(stack);
        if(lantern != null) {
            lantern.copyFrom(this);
        }
        this.entityDropItem(stack, 0.0F);
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound tag) {
        super.writeEntityToNBT(tag);
        this.writeInventoryToNBT(tag);
        this.setLit(tag.getBoolean(Names.NBT.LIT));
        this.setBurnTicks(tag.getInteger(Names.NBT.BURN_TICKS));
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound tag) {
        super.readEntityFromNBT(tag);
        this.readInventoryFromNBT(tag);
        tag.setBoolean(Names.NBT.LIT, this.isLit());
        tag.setInteger(Names.NBT.BURN_TICKS, this.getRemainingBurnTicks());
    }

    @Override
    public void markDirty() {}

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(this);
        } else {
            return super.getCapability(capability, facing);
        }
    }

    public static class RenderFactory implements IRenderFactory<EntityBoatLantern> {
        public static final RenderFactory FACTORY = new RenderFactory();

        private RenderFactory() {}

        @Override
        @SideOnly(Side.CLIENT)
        public Render<? super EntityBoatLantern> createRenderFor(RenderManager manager) {
            return new RenderEntityBoatLantern(manager);
        }
    }
}
