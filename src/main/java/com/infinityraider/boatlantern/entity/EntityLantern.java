package com.infinityraider.boatlantern.entity;

import com.infinityraider.boatlantern.BoatLantern;
import com.infinityraider.boatlantern.handler.GuiHandler;
import com.infinityraider.boatlantern.handler.LightingHandler;
import com.infinityraider.boatlantern.lantern.*;
import com.infinityraider.boatlantern.network.MessageSyncServerPos;
import com.infinityraider.boatlantern.reference.Names;
import com.infinityraider.boatlantern.registry.BlockRegistry;
import com.infinityraider.boatlantern.render.entity.RenderEntityLantern;
import com.infinityraider.infinitylib.reference.Constants;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nullable;

public class EntityLantern extends Entity implements ILantern, IInventoryLantern {
    public static final DataParameter<Boolean> DATA_LIT = EntityDataManager.createKey(EntityLantern.class, DataSerializers.BOOLEAN);
    public static final DataParameter<Integer> DATA_BURN_TICKS = EntityDataManager.createKey(EntityLantern.class, DataSerializers.VARINT);

    private final LanternLogic lanternLogic = new LanternLogic(this);
    private ItemStack fuelStack;

    private double prevX;
    private double prevY;
    private double prevZ;

    public EntityLantern(World world) {
        super(world);
        this.setSize(6 * Constants.UNIT, 11 * Constants.UNIT);
    }

    public EntityLantern(Entity source) {
        this(source.getEntityWorld());
        this.copyLocationAndAnglesFrom(source);
    }

    @Override
    protected void entityInit() {
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
        if(!this.getEntityWorld().isRemote) {
            this.lanternLogic.burnUpdate();
        }
        if(this.getRidingEntity() == null) {
            this.dropItems();
            this.setDead();
        }
    }

    @Override
    public void updateRidden() {
        Entity entity = this.getRidingEntity();
        if(entity == null) {
            this.dismountRidingEntity();
            return;
        }
        if (this.isRiding() && entity.isDead) {
            this.dismountRidingEntity();
        } else {
            if(this.firstUpdate) {
                this.prevX = this.posX;
                this.prevY = this.posY;
                this.prevZ = this.posZ;
            }
            this.motionX = 0.0D;
            this.motionY = 0.0D;
            this.motionZ = 0.0D;
            this.onUpdate();
            double dx = -0.145;
            double dy = 0.25;
            double dz = -0.6;
            double yaw = entity.rotationYaw;
            double cosY = Math.cos(Math.toRadians(yaw));
            double sinY = Math.sin(Math.toRadians(yaw));
            double newX = entity.posX + dx * cosY - dz * sinY;
            double newY = entity.posY + dy;
            double newZ = entity.posZ + dx * sinY + dz * cosY;
            this.prevPosX = prevX;
            this.prevPosY = prevY;
            this.prevPosZ = prevZ;
            this.lastTickPosX = this.prevX;
            this.lastTickPosY = this.prevY;
            this.lastTickPosZ = this.prevZ;
            this.setPosition(newX, newY, newZ);
            this.prevX = this.posX;
            this.prevY = this.posY;
            this.prevZ = this.posZ;
            this.prevRotationYaw = this.rotationYaw;
            this.setRenderYawOffset(entity.rotationYaw);
            this.rotationYaw = entity.rotationYaw;
            if(!this.getEntityWorld().isRemote) {
                BoatLantern.instance.getNetworkWrapper().sendToAll(new MessageSyncServerPos(this));
            }
        }
    }

    @Override
    public double getYOffset() {
        return 0.2;
    }

    @Override
    public void dismountRidingEntity() {
        Entity riding = this.getRidingEntity();
        if (riding != null) {
            super.dismountRidingEntity();
            if(riding.isEntityAlive()) {
                this.startRiding(riding);
            }
        } else {
            this.dropItems();
            this.setDead();
        }
    }

    @Override
    public int getRemainingBurnTicks() {
        return this.getDataManager().get(DATA_BURN_TICKS);
    }

    public EntityLantern setBurnTicks(int ticks) {
        ticks = ticks < 0 ? 0 : ticks;
        this.getDataManager().set(DATA_BURN_TICKS, ticks);
        return this;
    }

    @Override
    public EntityLantern addBurnTicks(int ticks) {
        return this.setBurnTicks(this.getRemainingBurnTicks() + ticks);
    }

    @Override
    public void spreadLight() {
        LightingHandler.getInstance().spreadLight(this);
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean processInitialInteract(EntityPlayer player, @Nullable ItemStack stack, EnumHand hand) {
        if (player.isSneaking()) {
            if (!this.worldObj.isRemote) {
                GuiHandler.getInstance().openGui(player, this);
            }
        } else {
            boolean lit = this.isLit();
            if (lit || this.getRemainingBurnTicks() > 0 || this.consumeFuel()) {
                this.setLit(!lit);
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
            this.setBeenAttacked();
            boolean flag = source.getEntity() instanceof EntityPlayer && ((EntityPlayer) source.getEntity()).capabilities.isCreativeMode;
            if (flag) {
                this.dropItems();
                this.setDead();
            }
            return true;
        } else {
            return true;
        }
    }

    public void dropItems() {
        if (!this.worldObj.isRemote && this.worldObj.getGameRules().getBoolean("doEntityDrops")) {
            ItemStack stack = new ItemStack(BlockRegistry.getInstance().blockLantern, 1, 0);
            ItemHandlerLantern lantern = LanternItemCache.getInstance().getLantern(stack);
            if (lantern != null) {
                lantern.copyFrom(this);
            }
            this.entityDropItem(stack, 0.0F);
        }
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound tag) {
        this.writeInventoryToNBT(tag);
        this.setLit(tag.getBoolean(Names.NBT.LIT));
        this.setBurnTicks(tag.getInteger(Names.NBT.BURN_TICKS));
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound tag) {
        this.readInventoryFromNBT(tag);
        tag.setBoolean(Names.NBT.LIT, this.isLit());
        tag.setInteger(Names.NBT.BURN_TICKS, this.getRemainingBurnTicks());
    }

    @Override
    public void markDirty() {}

    public void mountOnBoat(EntityBoat boat) {
        this.startRiding(boat);
    }

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

    public static class RenderFactory implements IRenderFactory<EntityLantern> {
        public static final EntityLantern.RenderFactory FACTORY = new EntityLantern.RenderFactory();

        private RenderFactory() {}

        @Override
        @SideOnly(Side.CLIENT)
        public Render<? super EntityLantern> createRenderFor(RenderManager manager) {
            return new RenderEntityLantern(manager);
        }
    }
}
