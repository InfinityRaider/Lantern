package com.infinityraider.boatlantern.handler;

import com.infinityraider.boatlantern.block.BlockLantern;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static net.minecraftforge.fml.common.eventhandler.EventPriority.HIGHEST;

public class InteractionHandler {
    private static final InteractionHandler INSTANCE = new InteractionHandler();

    public static InteractionHandler getInstance() {
        return INSTANCE;
    }

    private InteractionHandler() {}

    @SubscribeEvent(priority = HIGHEST)
    @SuppressWarnings("unused")
    public void onPlayerInteraction(PlayerInteractEvent.EntityInteractSpecific event) {
        if(event.getEntityPlayer().getEntityWorld().isRemote) {
            return;
        }
        if(!(event.getTarget() instanceof EntityBoat)) {
            return;
        }
        EntityBoat boat = (EntityBoat) event.getTarget();
        //Only convert vanilla boats, and not other boats extending EntityBoat
        if(boat.getClass() == EntityBoat.class) {
            EntityPlayer player = event.getEntityPlayer();
            if(!player.isSneaking()) {
                return;
            }
            ItemStack stack = player.inventory.getStackInSlot(player.inventory.currentItem);
            if(stack != null && stack.getItem() instanceof BlockLantern.BlockItem) {
                ((BlockLantern.BlockItem) stack.getItem()).mountLanternOnBoat(player, stack, boat);
            }
        }
    }
}
