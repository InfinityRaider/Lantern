package com.infinityraider.boatlantern.handler;

import com.infinityraider.boatlantern.BoatLantern;
import com.infinityraider.boatlantern.block.BlockLantern;
import com.infinityraider.boatlantern.block.tile.TileEntityLantern;
import com.infinityraider.boatlantern.container.ContainerLantern;
import com.infinityraider.boatlantern.container.GuiContainerLantern;
import com.infinityraider.boatlantern.entity.EntityLantern;
import com.infinityraider.boatlantern.lantern.IInventoryLantern;
import com.infinityraider.boatlantern.lantern.ItemHandlerLantern;
import com.infinityraider.boatlantern.lantern.LanternItemCache;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler {
    private static final GuiHandler INSTANCE = new GuiHandler();

    public static GuiHandler getInstance() {
        return INSTANCE;
    }

    public static final int LANTERN_INVENTORY_BLOCK = 0;
    public static final int LANTERN_INVENTORY_ITEM = 1;
    public static final int LANTERN_INVENTORY_ENTITY = 2;

    private GuiHandler() {}

    public void openGui(EntityPlayer player, TileEntityLantern lantern) {
        this.openGui(player, LANTERN_INVENTORY_BLOCK, lantern.getWorld(), lantern.xCoord(), lantern.yCoord(), lantern.zCoord());
    }

    public void openGui(EntityPlayer player, ItemStack stack) {
        ItemHandlerLantern lantern = LanternItemCache.getInstance().getLantern(player, stack);
        if(lantern != null) {
            this.openGui(player, LANTERN_INVENTORY_ITEM, player.getEntityWorld(), 0, 0, 0);
        }
    }

    public void openGui(EntityPlayer player, EntityLantern lantern) {
        this.openGui(player, LANTERN_INVENTORY_ENTITY, player.getEntityWorld(), lantern.getEntityId(), 0, 0);
    }

    protected void openGui(EntityPlayer player, int id, World world, int x, int y, int z) {
        if(!world.isRemote) {
            player.openGui(BoatLantern.instance, id, world, x, y, z);
        }
    }

    @Override
    public ContainerLantern getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        switch(id) {
            case LANTERN_INVENTORY_BLOCK:
                TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
                if(te instanceof TileEntityLantern) {
                    return new ContainerLantern(player.inventory, (TileEntityLantern) te);
                }
                break;
            case LANTERN_INVENTORY_ITEM:
                ItemStack stack = player.inventory.getStackInSlot(player.inventory.currentItem);
                if(stack != null && stack.getItem() instanceof BlockLantern.BlockItem) {
                    BlockLantern.BlockItem lantern = (BlockLantern.BlockItem) stack.getItem();
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
    public GuiContainerLantern getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        ContainerLantern container = getServerGuiElement(id, player, world, x, y, z);
        return container == null ? null : new GuiContainerLantern(container);
    }
}
