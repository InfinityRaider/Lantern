package com.infinityraider.lantern.network;

import com.infinityraider.infinitylib.network.MessageBase;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

public class MessageSyncServerPos extends MessageBase {
    private Entity entity;
    private double x;
    private double y;
    private double z;

    public MessageSyncServerPos() {
        super();
    }

    public MessageSyncServerPos(Entity entity) {
        this();
        this.entity = entity;
        this.x = entity.getPosX();
        this.y = entity.getPosY();
        this.z = entity.getPosZ();
    }

    @Override
    public NetworkDirection getMessageDirection() {
        return NetworkDirection.PLAY_TO_CLIENT;
    }

    @Override
    protected void processMessage(NetworkEvent.Context ctx) {
        if(this.entity != null) {
            this.entity.setRawPosition(this.x, this.y, this.z);
        }
    }
}
