package com.infinityraider.lantern.handler;

import com.infinityraider.lantern.item.ItemLantern;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static net.minecraftforge.eventbus.api.EventPriority.HIGHEST;

public class InteractionHandler {
    private static final InteractionHandler INSTANCE = new InteractionHandler();

    public static InteractionHandler getInstance() {
        return INSTANCE;
    }

    private InteractionHandler() {}

    @SubscribeEvent(priority = HIGHEST)
    @SuppressWarnings("unused")
    public void onPlayerInteraction(PlayerInteractEvent.EntityInteractSpecific event) {
        if(event.getPlayer().getEntityWorld().isRemote) {
            return;
        }
        if(!(event.getTarget() instanceof BoatEntity)) {
            return;
        }
        BoatEntity boat = (BoatEntity) event.getTarget();
        //Only convert vanilla boats, and not other boats extending EntityBoat
        if(boat.getClass() == BoatEntity.class) {
            PlayerEntity player = event.getPlayer();
            if(!player.isSneaking()) {
                return;
            }
            ItemStack stack = player.inventory.getStackInSlot(player.inventory.currentItem);
            if(stack.getItem() instanceof ItemLantern) {
                ((ItemLantern) stack.getItem()).mountLanternOnBoat(player, stack, boat);
            }
        }
    }
}
