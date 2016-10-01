package com.infinityraider.boatlantern.entity;

import com.infinityraider.boatlantern.handler.LightingHandler;
import com.infinityraider.boatlantern.reference.Names;
import com.infinityraider.boatlantern.registry.BlockRegistry;
import com.infinityraider.boatlantern.render.RenderEntityBoatLantern;
import com.infinityraider.infinitylib.utility.inventory.IInventorySerializableItemHandler;
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
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public class EntityBoatLantern extends EntityBoat implements IInventorySerializableItemHandler {
    public static final DataParameter<Integer> DATA_BURN_TICKS = EntityDataManager.createKey(EntityBoatLantern.class, DataSerializers.VARINT);

    private ItemStack fuelStack;

    /** Constructor which is used to instantiate the entity client side or server side after a world reload using reflection */
    @SuppressWarnings("unused")
    public EntityBoatLantern(World world) {
        super(world);
    }

    public EntityBoatLantern(World world, double x, double y, double z) {
        super(world, x, y, z);
    }

    public EntityBoatLantern(EntityBoat boat) {
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
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.getDataManager().register(DATA_BURN_TICKS, 0);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        this.burnUpdate();
    }

    protected void burnUpdate() {
        int ticks = this.getRemainingBurnTicks();
        if(ticks > 0) {
            LightingHandler.getInstance().spreadLight(this);
            this.addBurnTicks(-1);
        }
    }

    public int getRemainingBurnTicks() {
        return this.getDataManager().get(DATA_BURN_TICKS);
    }

    public EntityBoatLantern setBurnTicks(int ticks) {
        ticks = ticks < 0 ? 0 : ticks;
        this.getDataManager().set(DATA_BURN_TICKS, ticks);
        return this;
    }

    public EntityBoatLantern addBurnTicks(int ticks) {
        return this.setBurnTicks(this.getRemainingBurnTicks() + ticks);
    }

    public boolean isLit() {
        return getRemainingBurnTicks() > 0;
    }

    @Override
    public boolean processInitialInteract(EntityPlayer player, @Nullable ItemStack stack, EnumHand hand) {
        if(player.isSneaking()) {
            if(!this.worldObj.isRemote) {
                player.displayGUIChest(this);
            }
            return true;
        } else {
            return super.processInitialInteract(player, stack, hand);
        }
    }

    /**
     * Overridden to drop all the items in the inventory too
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
        this.entityDropItem(new ItemStack(BlockRegistry.getInstance().blockLantern, 1), 0.0F);
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound tag) {
        super.writeEntityToNBT(tag);
        this.writeInventoryToNBT(tag);
        this.setBurnTicks(tag.getInteger(Names.NBT.BURN_TICKS));
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound tag) {
        super.readEntityFromNBT(tag);
        this.readInventoryFromNBT(tag);
        tag.setInteger(Names.NBT.BURN_TICKS, this.getRemainingBurnTicks());
    }

    @Override
    public int getSizeInventory() {
        return 1;
    }

    @Nullable
    @Override
    public ItemStack getStackInSlot(int index) {
        return fuelStack;
    }

    @Nullable
    @Override
    public ItemStack decrStackSize(int index, int count) {
        count = Math.min(count, this.fuelStack.stackSize);
        ItemStack stack = this.fuelStack.copy();
        stack.stackSize = count;
        if(this.fuelStack.stackSize <= count) {
            this.setInventorySlotContents(index, null);
        }
        return stack;
    }

    @Nullable
    @Override
    public ItemStack removeStackFromSlot(int index) {
        ItemStack stack = this.fuelStack.copy();
        this.setInventorySlotContents(index, null);
        return stack;
    }

    @Override
    public void setInventorySlotContents(int index, @Nullable ItemStack stack) {
        this.fuelStack = stack;
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public void markDirty() {}

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return true;
    }

    @Override
    public void openInventory(EntityPlayer player) {}

    @Override
    public void closeInventory(EntityPlayer player) {}

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return true;
    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {}

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {
        this.fuelStack = null;
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
