package com.infinityraider.lantern.container;

import com.infinityraider.infinitylib.container.IInfinityContainerType;
import com.infinityraider.infinitylib.network.serialization.PacketBufferUtil;
import com.infinityraider.lantern.Lantern;
import com.infinityraider.lantern.block.tile.TileEntityLantern;
import com.infinityraider.lantern.entity.EntityLantern;
import com.infinityraider.lantern.item.ItemLantern;
import com.infinityraider.lantern.lantern.IInventoryLantern;
import com.infinityraider.infinitylib.container.ContainerBase;
import com.infinityraider.lantern.lantern.ItemHandlerLantern;
import com.infinityraider.lantern.lantern.LanternItemCache;
import com.infinityraider.lantern.reference.Names;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.*;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.network.IContainerFactory;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class ContainerLantern extends ContainerBase {
    private final IInventoryLantern lantern;

    public ContainerLantern(int windowId, PlayerInventory inventory, IInventoryLantern lantern) {
        super(Lantern.instance.getModContainerRegistry().lantern, windowId, inventory, 8, 44);
        this.lantern = lantern;
        this.addSlot(new FuelSlot(this.getLanternInventory(), 0, 80, 9));
    }

    public IInventoryLantern getLanternInventory() {
        return this.lantern;
    }

    public boolean isLit() {
        return this.getLanternInventory().getLantern().isLit();
    }

    @Override
    public ItemStack transferStackInSlot(PlayerEntity player, int clickedSlot) {
        ItemStack itemstack = null;
        Slot slot = this.inventorySlots.get(clickedSlot);
        if (slot != null && slot.getHasStack()) {
            ItemStack itemStack1 = slot.getStack();
            itemstack = itemStack1.copy();
            //try to move item from the lantern into the player's inventory
            if (clickedSlot >= PLAYER_INVENTORY_SIZE) {
                if (!this.mergeItemStack(itemStack1, 0, inventorySlots.size() - 2, false)) {
                    return null;
                }
            }
            else {
                //try to move item from the player's inventory into the lantern
                if(itemStack1.getItem() != null) {
                    if(this.getLanternInventory().isItemValidForSlot(0, itemStack1)) {
                        if (!this.mergeItemStack(itemStack1, PLAYER_INVENTORY_SIZE, PLAYER_INVENTORY_SIZE + 1, false)) {
                            return null;
                        }
                    }
                }
            }
            if (itemStack1.getCount() == 0) {
                slot.putStack(null);
            }
            else {
                slot.onSlotChanged();
            }
            if (itemStack1.getCount() == itemstack.getCount()) {
                return null;
            }
            slot.onTake(player, itemStack1);
        }
        return itemstack;
    }

    private static class FuelSlot extends Slot {
        public FuelSlot(IInventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean isItemValid(@Nullable ItemStack stack) {
            return stack == null || ForgeHooks.getBurnTime(stack) > 0;
        }
    }

    private static final int LANTERN_INVENTORY_BLOCK = 0;
    private static final int LANTERN_INVENTORY_ITEM = 1;
    private static final int LANTERN_INVENTORY_ENTITY = 2;

    public static void open(PlayerEntity player, TileEntityLantern lantern) {
        open(player, lantern.getWorld(), (packet) -> {
            packet.writeInt(LANTERN_INVENTORY_BLOCK);
            PacketBufferUtil.writeTileEntity(packet, lantern);
        });
    }

    public static void open(PlayerEntity player, Hand hand, ItemStack stack) {
        ItemHandlerLantern lantern = LanternItemCache.getInstance().getLantern(player, stack);
        if(lantern != null) {
            open(player, player.getEntityWorld(), (packet) -> {
                packet.writeInt(LANTERN_INVENTORY_ITEM);
                packet.writeEnumValue(hand);
            });
        }
    }

    public static void open(PlayerEntity player, EntityLantern lantern) {
        open(player, player.getEntityWorld(), (packet) -> {
            packet.writeInt(LANTERN_INVENTORY_ENTITY);
            PacketBufferUtil.writeEntity(packet, lantern);
        });
    }

    private static final ITextComponent NAME = new TranslationTextComponent(Lantern.instance.getModId() + ".container." + Names.Blocks.LANTERN);
    private static final INamedContainerProvider NAME_PROVIDER = new SimpleNamedContainerProvider(Factory.getInstance(), NAME);

    private static void open(PlayerEntity player, @Nullable World world, Consumer<PacketBuffer> packet) {
        if(world != null && !world.isRemote) {
            NetworkHooks.openGui((ServerPlayerEntity) player, NAME_PROVIDER, packet);
        }
    }

    public static final class Factory implements IContainerFactory<ContainerLantern>, IContainerProvider {
        private static final Factory INSTANCE = new Factory();

        public static Factory getInstance() {
            return INSTANCE;
        }

        private Factory() {}

        @Override
        public ContainerLantern create(int windowId, PlayerInventory inventory, PacketBuffer data) {
            if(data != null) {
                int id = data.readInt();
                switch (id) {
                    case LANTERN_INVENTORY_BLOCK:
                        TileEntity te = PacketBufferUtil.readTileEntity(data);
                        if (te instanceof TileEntityLantern) {
                            return new ContainerLantern(windowId, inventory, (TileEntityLantern) te);
                        }
                        break;
                    case LANTERN_INVENTORY_ITEM:
                        ItemStack stack = inventory.player.getHeldItem(data.readEnumValue(Hand.class));
                        if (stack.getItem() instanceof ItemLantern) {
                            ItemLantern lantern = (ItemLantern) stack.getItem();
                            return new ContainerLantern(windowId, inventory, lantern.getLantern(inventory.player, stack));
                        }
                        break;
                    case LANTERN_INVENTORY_ENTITY:
                        Entity entity = PacketBufferUtil.readEntity(data);
                        if (entity instanceof IInventoryLantern) {
                            return new ContainerLantern(windowId, inventory, (IInventoryLantern) entity);
                        }
                        break;
                }
            }
            return null;
        }

        @Nullable
        @Override
        public ContainerLantern createMenu(int id, PlayerInventory inv, PlayerEntity player) {
            return this.create(id, inv);
        }
    }

    public static final class GuiFactory implements IInfinityContainerType.IGuiFactory<ContainerLantern> {
        private static final GuiFactory INSTANCE = new GuiFactory();

        public static GuiFactory getInstance() {
            return INSTANCE;
        }

        private GuiFactory() {}

        @Override
        @OnlyIn(Dist.CLIENT)
        public GuiContainerLantern createGui(ContainerLantern container, PlayerInventory inventory, ITextComponent name) {
            return new GuiContainerLantern(container, inventory, name);
        }
    }
}
