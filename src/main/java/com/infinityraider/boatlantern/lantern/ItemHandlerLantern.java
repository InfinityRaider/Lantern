package com.infinityraider.boatlantern.lantern;

import com.infinityraider.boatlantern.handler.LightingHandler;
import com.infinityraider.boatlantern.reference.Names;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

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

    public void updateTick() {
        this.logic.burnUpdate();
    }

    protected void loadStack() {
        if(!parentStack.hasTagCompound()) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setBoolean(Names.NBT.HAS_STACK, false);
            tag.setInteger(Names.NBT.BURN_TICKS, 0);
            tag.setBoolean(Names.NBT.LIT, false);
            parentStack.setTagCompound(tag);
        } else {
            NBTTagCompound tag = parentStack.getTagCompound();
            if(tag.hasKey(Names.NBT.HAS_STACK)) {
                if(tag.getBoolean(Names.NBT.HAS_STACK)) {
                    this.fuelStack = ItemStack.loadItemStackFromNBT(tag);
                }
            } else {
                tag.setBoolean(Names.NBT.HAS_STACK, false);
            }
            if(tag.hasKey(Names.NBT.BURN_TICKS)) {
                this.burnTime = tag.getInteger(Names.NBT.BURN_TICKS);
            } else {
                tag.setInteger(Names.NBT.BURN_TICKS, 0);
            }
            if(tag.hasKey(Names.NBT.LIT)) {
                this.lit = tag.getBoolean(Names.NBT.LIT);
            } else {
                tag.setBoolean(Names.NBT.LIT, false);
            }
        }
    }

    protected void writeStack() {
        NBTTagCompound tag;
        if(fuelStack == null) {
            tag = new NBTTagCompound();
            tag.setBoolean(Names.NBT.HAS_STACK, false);
        } else {
            tag = parentStack.getTagCompound();
            tag.setBoolean(Names.NBT.HAS_STACK, true);
            fuelStack.writeToNBT(tag);
        }
        tag.setInteger(Names.NBT.BURN_TICKS, this.burnTime);
        tag.setBoolean(Names.NBT.LIT, this.lit);
        parentStack.setTagCompound(tag);
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
            if(!lit && this.entity != null) {
                LightingHandler.getInstance().removeLastLight(this.entity);
            }
            this.lit = status;
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
            LightingHandler.getInstance().spreadLight(this.entity);
        }
    }
}
