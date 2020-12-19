package com.infinityraider.lantern.lantern;

import com.infinityraider.lantern.handler.LightingHandler;
import com.infinityraider.lantern.reference.Names;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

import javax.annotation.Nonnull;

public class ItemHandlerLantern implements ILantern, IInventoryLantern {
    private final ItemStack parentStack;
    private final LanternLogic logic;

    private Entity entity;

    private ItemStack fuelStack;
    private int burnTime;
    private boolean lit;

    public ItemHandlerLantern(ItemStack stack) {
        this.parentStack = stack;
        this.loadStack();
        this.logic = new LanternLogic(this);
    }

    public ItemHandlerLantern setEntity(Entity entity) {
        if(this.entity == null) {
            this.entity = entity;
        }
        return this;
    }

    public Entity getEntity() {
        return this.entity;
    }

    public void updateTick() {
        this.logic.burnUpdate();
    }

    protected void loadStack() {
        if(!parentStack.hasTag()) {
            CompoundNBT tag = new CompoundNBT();
            tag.putBoolean(Names.NBT.HAS_STACK, false);
            tag.putInt(Names.NBT.BURN_TICKS, 0);
            tag.putBoolean(Names.NBT.LIT, false);
            parentStack.setTag(tag);
        } else {
            CompoundNBT tag = parentStack.getTag();
            if(tag.contains(Names.NBT.HAS_STACK)) {
                if(tag.getBoolean(Names.NBT.HAS_STACK)) {
                    this.fuelStack = ItemStack.read(tag);
                }
            } else {
                tag.putBoolean(Names.NBT.HAS_STACK, false);
            }
            if(tag.contains(Names.NBT.BURN_TICKS)) {
                this.burnTime = tag.getInt(Names.NBT.BURN_TICKS);
            } else {
                tag.putInt(Names.NBT.BURN_TICKS, 0);
            }
            if(tag.contains(Names.NBT.LIT)) {
                this.lit = tag.getBoolean(Names.NBT.LIT);
            } else {
                tag.putBoolean(Names.NBT.LIT, false);
            }
        }
    }

    protected void writeStack() {
        CompoundNBT tag;
        if(fuelStack == null) {
            tag = new CompoundNBT();
            tag.putBoolean(Names.NBT.HAS_STACK, false);
        } else {
            tag = parentStack.getTag();
            tag.putBoolean(Names.NBT.HAS_STACK, true);
            fuelStack.write(tag);
        }
        tag.putInt(Names.NBT.BURN_TICKS, this.burnTime);
        tag.putBoolean(Names.NBT.LIT, this.lit);
        parentStack.setTag(tag);
    }

    @Override
    public ILantern getLantern() {
        return this;
    }

    @Override
    public ItemStack getFuelStack() {
        return fuelStack;
    }

    @Override
    public IInventoryLantern setFuelStack(ItemStack stack) {
        this.fuelStack = stack;
        this.markDirty();
        return this;
    }

    @Override
    public void markDirty() {
        this.writeStack();
    }

    @Override
    public IInventoryLantern getInventory() {
        return this;
    }

    @Override
    public void setLit(boolean status) {
        boolean lit = this.isLit();
        if(lit != status) {
            this.lit = status;
            if(!status && this.getEntity() != null) {
                LightingHandler.getInstance().removeLastLight(this.getEntity());
            }
            this.markDirty();
        }
    }

    @Override
    public boolean isLit() {
        return this.lit;
    }

    @Override
    public int getRemainingBurnTicks() {
        return this.burnTime;
    }

    @Override
    public ILantern addBurnTicks(int ticks) {
        if(ticks != 0) {
            this.burnTime += ticks;
            if (this.burnTime < 0) {
                this.burnTime = 0;
            }
            this.markDirty();
        }
        return this;
    }

    @Override
    public void spreadLight() {
        if(this.entity != null) {
            LightingHandler.getInstance().spreadLight(this.getEntity());
        }
    }
}
