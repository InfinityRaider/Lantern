package com.infinityraider.lantern.block.tile;

import com.infinityraider.lantern.Lantern;
import com.infinityraider.lantern.block.BlockLantern;
import com.infinityraider.lantern.lantern.IInventoryLantern;
import com.infinityraider.lantern.lantern.ILantern;
import com.infinityraider.lantern.lantern.LanternLogic;
import com.infinityraider.lantern.reference.Names;
import com.infinityraider.infinitylib.block.tile.TileEntityBase;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class TileEntityLantern extends TileEntityBase implements ILantern, IInventoryLantern, ITickableTileEntity {
    private final LanternLogic lanternLogic = new LanternLogic(this);
    private final LazyOptional<IItemHandler> capability;

    private ItemStack fuelStack;
    private int burnTicksRemaining;

    public TileEntityLantern() {
        super(Lantern.instance.getModTileRegistry().lantern);
        this.capability = LazyOptional.of(() -> this);
        this.fuelStack = ItemStack.EMPTY;
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
        this.markDirty();
        return this;
    }

    @Override
    protected void writeTileNBT(CompoundNBT tag) {
        tag.putInt(Names.NBT.BURN_TICKS, burnTicksRemaining);
        this.writeInventoryToNBT(tag);
    }

    @Override
    protected void readTileNBT(BlockState state, CompoundNBT tag) {
        this.burnTicksRemaining = tag.getInt(Names.NBT.BURN_TICKS);
        this.readInventoryFromNBT(tag);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction facing) {
        if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return this.capability.cast();
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
            BlockState updated = BlockLantern.LIT.apply(this.getBlockState(), status);
            this.getWorld().setBlockState(this.getPos(), updated);
        }
    }

    @Override
    public boolean isLit() {
        return BlockLantern.LIT.fetch(this.getBlockState());
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
    public void tick() {
        World world = this.getWorld();
        if(world != null && !world.isRemote) {
            this.lanternLogic.burnUpdate();
        }
    }
}
