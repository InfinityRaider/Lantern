package com.infinityraider.lantern.entity;

import com.infinityraider.infinitylib.entity.EntityBase;
import com.infinityraider.lantern.container.ContainerLantern;
import com.infinityraider.lantern.handler.LightingHandler;
import com.infinityraider.lantern.lantern.*;
import com.infinityraider.lantern.reference.Names;
import com.infinityraider.lantern.registry.BlockRegistry;
import com.infinityraider.lantern.registry.EntityRegistry;
import com.infinityraider.lantern.render.entity.RenderEntityLantern;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class EntityLantern extends EntityBase implements ILantern, IInventoryLantern {
    public static final DataParameter<Boolean> DATA_LIT = EntityDataManager.createKey(EntityLantern.class, DataSerializers.BOOLEAN);
    public static final DataParameter<Integer> DATA_BURN_TICKS = EntityDataManager.createKey(EntityLantern.class, DataSerializers.VARINT);

    private final LazyOptional<IItemHandler> capabilityItemHandler;
    private final LanternLogic lanternLogic = new LanternLogic(this);
    private ItemStack fuelStack;

    //For client side spawning
    public EntityLantern(EntityType<? extends EntityLantern> type, World world) {
        super(type, world);
        this.capabilityItemHandler = LazyOptional.of(() -> this);
        this.fuelStack = ItemStack.EMPTY;
    }

    public EntityLantern(EntityType<? extends EntityLantern> type, Entity source) {
        this(type, source.getEntityWorld());
        this.copyLocationAndAnglesFrom(source);
    }

    public EntityLantern(Entity source) {
        this(EntityRegistry.getInstance().entityLantern, source);
    }

    @Override
    protected void registerData() {
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
            LightingHandler.getInstance(this).removeLastLight(this);
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
    public void tick() {
        super.tick();
        if(!this.getEntityWorld().isRemote) {
            this.lanternLogic.burnUpdate();
        }
        if(this.getRidingEntity() == null && !this.firstUpdate) {
            this.dropItems();
            this.setDead();
        }
    }

    @Override
    public void updateRidden() {
        this.tick();
        Entity mount = this.getRidingEntity();
        if(mount == null) {
            if(!this.firstUpdate) {
                this.dismount();
            }
            return;
        }
        if (this.isPassenger() && !mount.isAlive()) {
            if(!this.firstUpdate) {
                this.dismount();
            }
        } else {
            this.setMotion(0, 0, 0);
            float yaw = mount.rotationYaw;
            // calculate yaw cosine and sine
            double cosY = Math.cos(Math.toRadians(yaw));
            double sinY = Math.sin(Math.toRadians(yaw));
            // offset on boat
            double dx = -0.145;
            double dy = 0.25;
            double dz = -0.6;
            // calculate correct position:
            double x = mount.getPosX() + dx*cosY - dz*sinY;
            double y = mount.getPosY() + dy;
            double z = mount.getPosZ() + dx*sinY + dz*cosY;
            // apply position
            this.setPosition(x, y, z);
            this.rotationYaw = yaw;
        }
    }

    protected void setDead() {
        super.setDead();
    }

    @Override
    public double getYOffset() {
        return 0.2;
    }

    @Override
    public void dismount() {
        Entity riding = this.getRidingEntity();
        if (riding != null) {
            super.dismount();
            if(riding.isAlive()) {
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
        ticks = Math.max(ticks, 0);
        this.getDataManager().set(DATA_BURN_TICKS, ticks);
        return this;
    }

    @Override
    public EntityLantern addBurnTicks(int ticks) {
        return this.setBurnTicks(this.getRemainingBurnTicks() + ticks);
    }

    @Override
    public void spreadLight() {
        LightingHandler.getInstance(this).spreadLight(this);
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public ActionResultType processInitialInteract(PlayerEntity player, Hand hand) {
        if(this.getEntityWorld().isRemote || hand == Hand.OFF_HAND) {
            return super.processInitialInteract(player, hand);
        }
        if (player.isSneaking()) {
            ContainerLantern.open(player, this);
            return ActionResultType.CONSUME;
        } else {
            boolean lit = this.isLit();
            if (lit || this.getRemainingBurnTicks() > 0 || this.consumeFuel()) {
                this.setLit(!lit);
            }
            return ActionResultType.CONSUME;
        }
    }

    /**
     * Overridden to drop the lantern too
     */
    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        } else if (!this.getEntityWorld().isRemote && this.isAlive()) {
            boolean flag = source.getTrueSource() instanceof PlayerEntity && !((PlayerEntity) source.getTrueSource()).isCreative();
            if (flag) {
                this.dropItems();
            }
            this.setDead();
            return true;
        } else {
            return true;
        }
    }

    public void dropItems() {
        if (!this.getEntityWorld().isRemote && this.getEntityWorld().getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS)) {
            ItemStack stack = new ItemStack(BlockRegistry.getInstance().blockLantern, 1);
            ItemHandlerLantern lantern = LanternItemCache.getInstance().getLantern(stack);
            if (lantern != null) {
                lantern.copyFrom(this);
            }
            this.entityDropItem(stack, 0.0F);
        }
    }

    @Override
    public void writeCustomEntityData(CompoundNBT tag) {
        this.writeInventoryToNBT(tag);
        tag.putBoolean(Names.NBT.LIT, this.isLit());
        tag.putInt(Names.NBT.BURN_TICKS, this.getRemainingBurnTicks());
    }

    @Override
    public void readCustomEntityData(CompoundNBT tag) {
        this.readInventoryFromNBT(tag);
        this.setLit(tag.getBoolean(Names.NBT.LIT));
        this.setBurnTicks(tag.getInt(Names.NBT.BURN_TICKS));
    }

    @Override
    public void markDirty() {}

    public void mountOnBoat(BoatEntity boat) {
        this.startRiding(boat);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction facing) {
        if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return capabilityItemHandler.cast();
        } else {
            return super.getCapability(capability, facing);
        }
    }

    public static class SpawnFactory implements EntityType.IFactory<EntityLantern> {
        private static final EntityLantern.SpawnFactory INSTANCE = new EntityLantern.SpawnFactory();

        public static EntityLantern.SpawnFactory getInstance() {
            return INSTANCE;
        }

        private SpawnFactory() {}

        @Override
        public EntityLantern create(EntityType<EntityLantern> type, World world) {
            return new EntityLantern(type, world);
        }
    }

    public static class RenderFactory implements IRenderFactory<EntityLantern> {
        private static final EntityLantern.RenderFactory INSTANCE = new EntityLantern.RenderFactory();

        public static EntityLantern.RenderFactory getInstance() {
            return INSTANCE;
        }

        private RenderFactory() {}

        @Override
        public EntityRenderer<? super EntityLantern> createRenderFor(EntityRendererManager manager) {
            return new RenderEntityLantern(manager);
        }
    }
}
