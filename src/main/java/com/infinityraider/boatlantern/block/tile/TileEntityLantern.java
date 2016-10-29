package com.infinityraider.boatlantern.block.tile;

import com.infinityraider.boatlantern.block.BlockLantern;
import com.infinityraider.boatlantern.lantern.IInventoryLantern;
import com.infinityraider.boatlantern.lantern.ILantern;
import com.infinityraider.boatlantern.lantern.LanternLogic;
import com.infinityraider.boatlantern.reference.Names;
import com.infinityraider.infinitylib.block.tile.TileEntityBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;

public class TileEntityLantern extends TileEntityBase implements ILantern, IInventoryLantern, ITickable {
    private final LanternLogic lanternLogic = new LanternLogic(this);

    private ItemStack fuelStack;
    private int burnTicksRemaining;

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
        this.markDirty();
        return this;
    }

    @Override
    protected void writeTileNBT(NBTTagCompound tag) {
        tag.setInteger(Names.NBT.BURN_TICKS, burnTicksRemaining);
        this.writeInventoryToNBT(tag);
    }

    @Override
    protected void readTileNBT(NBTTagCompound tag) {
        this.burnTicksRemaining = tag.getInteger(Names.NBT.BURN_TICKS);
        this.readInventoryFromNBT(tag);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(this);
        } else {
            return super.getCapability(capability, facing);
        }
    }

    @Override
    public IInventoryLantern getInventory() {
        return this;
    }

    @Override
    public void setLit(boolean status) {
        boolean lit = this.isLit();
        if(lit != status) {
            IBlockState updated = BlockLantern.Properties.LIT.applyToBlockState(this.getState(), status);
            this.worldObj.setBlockState(this.getPos(), updated);
        }
    }

    @Override
    public boolean isLit() {
        return BlockLantern.Properties.LIT.getValue(this.getState());
    }

    @Override
    public int getRemainingBurnTicks() {
        return burnTicksRemaining;
    }

    @Override
    public ILantern addBurnTicks(int ticks) {
        this.burnTicksRemaining += ticks;
        if(this.burnTicksRemaining < 0) {
            this.burnTicksRemaining = 0;
        }
        this.markDirty();
        return this;
    }

    @Override
    public void spreadLight() {}

    @Override
    public void update() {
        if(!this.getWorld().isRemote) {
            this.lanternLogic.burnUpdate();
        }
    }
}
