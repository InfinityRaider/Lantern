package com.infinityraider.lantern.network;

import com.infinityraider.infinitylib.network.MessageBase;
import com.infinityraider.lantern.item.ItemLantern;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

public class MessageApplyLanternToBoat extends MessageBase {
    private BoatEntity boat;
    private Hand hand;

    public MessageApplyLanternToBoat() {
        super();
    }

    public MessageApplyLanternToBoat(BoatEntity boat, Hand hand) {
        this();
        this.boat = boat;
        this.hand = hand;
    }

    @Override
    public NetworkDirection getMessageDirection() {
        return NetworkDirection.PLAY_TO_SERVER;
    }

    @Override
    protected void processMessage(NetworkEvent.Context ctx) {
        if(this.boat != null) {
            PlayerEntity player = ctx.getSender();
            if(player == null) {
                return;
            }
            ItemStack stack = player.getHeldItem(this.hand);
            if(stack.getItem() instanceof ItemLantern) {
                ((ItemLantern) stack.getItem()).mountLanternOnBoat(player, stack, boat);
            }
        }
    }
}
