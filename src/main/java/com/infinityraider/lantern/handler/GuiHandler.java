package com.infinityraider.lantern.handler;

import com.infinityraider.lantern.Lantern;
import com.infinityraider.lantern.block.tile.TileEntityLantern;
import com.infinityraider.lantern.container.ContainerLantern;
import com.infinityraider.lantern.container.GuiContainerLantern;
import com.infinityraider.lantern.entity.EntityLantern;
import com.infinityraider.lantern.item.ItemLantern;
import com.infinityraider.lantern.lantern.IInventoryLantern;
import com.infinityraider.lantern.lantern.ItemHandlerLantern;
import com.infinityraider.lantern.lantern.LanternItemCache;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class GuiHandler implements IGuiHandler {
    private static final GuiHandler INSTANCE = new GuiHandler();

    public static GuiHandler getInstance() {
        return INSTANCE;
    }

    public static final int LANTERN_INVENTORY_BLOCK = 0;
    public static final int LANTERN_INVENTORY_ITEM = 1;
    public static final int LANTERN_INVENTORY_ENTITY = 2;

    private GuiHandler() {}

    public void openGui(PlayerEntity player, TileEntityLantern lantern) {
        this.openGui(player, LANTERN_INVENTORY_BLOCK, lantern.getWorld(), lantern.xCoord(), lantern.yCoord(), lantern.zCoord());
    }

    public void openGui(PlayerEntity player, ItemStack stack) {
        ItemHandlerLantern lantern = LanternItemCache.getInstance().getLantern(player, stack);
        if(lantern != null) {
            this.openGui(player, LANTERN_INVENTORY_ITEM, player.getEntityWorld(), 0, 0, 0);
        }
    }

    public void openGui(PlayerEntity player, EntityLantern lantern) {
        this.openGui(player, LANTERN_INVENTORY_ENTITY, player.getEntityWorld(), lantern.getEntityId(), 0, 0);
    }

    protected void openGui(PlayerEntity player, int id, World world, int x, int y, int z) {
        if(!world.isRemote) {
            player.openGui(Lantern.instance, id, world, x, y, z);
        }
    }

    @Override
    public ContainerLantern getServerGuiElement(int id, PlayerEntity player, World world, int x, int y, int z) {
        switch(id) {
            case LANTERN_INVENTORY_BLOCK:
                TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
                if(te instanceof TileEntityLantern) {
                    return new ContainerLantern(player.inventory, (TileEntityLantern) te);
                }
                break;
            case LANTERN_INVENTORY_ITEM:
                ItemStack stack = player.inventory.getStackInSlot(player.inventory.currentItem);
                if(stack != null && stack.getItem() instanceof ItemLantern) {
                    ItemLantern lantern = (ItemLantern) stack.getItem();
                    return new ContainerLantern(player.inventory, lantern.getLantern(player, stack));
                }
                break;
            case LANTERN_INVENTORY_ENTITY:
                Entity entity = player.getEntityWorld().getEntityByID(x);
                if(entity instanceof IInventoryLantern) {
                    return new ContainerLantern(player.inventory, (IInventoryLantern) entity);
                }
                break;
        }
        return null;
    }

    @Override
    public GuiContainerLantern getClientGuiElement(int id, PlayerEntity player, World world, int x, int y, int z) {
        ContainerLantern container = getServerGuiElement(id, player, world, x, y, z);
        return container == null ? null : new GuiContainerLantern(container);
    }
}
