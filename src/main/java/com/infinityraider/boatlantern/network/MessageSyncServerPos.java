package com.infinityraider.boatlantern.network;

import com.infinityraider.infinitylib.network.MessageBase;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class MessageSyncServerPos extends MessageBase<IMessage> {
    private Entity entity;
    private long x;
    private long y;
    private long z;

    public MessageSyncServerPos() {
        super();
    }

    public MessageSyncServerPos(Entity entity) {
        this();
        this.entity = entity;
        this.x = MathHelper.floor_double_long(entity.posX * 4096.0);
        this.y = MathHelper.floor_double_long(entity.posY * 4096.0);
        this.z = MathHelper.floor_double_long(entity.posZ * 4096.0);
    }

    @Override
    public Side getMessageHandlerSide() {
        return Side.CLIENT;
    }

    @Override
    protected void processMessage(MessageContext ctx) {
        if(ctx.side == Side.CLIENT && this.entity != null) {
            this.entity.serverPosX = this.x;
            this.entity.serverPosY = this.y;
            this.entity.serverPosZ = this.z;
        }
    }

    @Override
    protected IMessage getReply(MessageContext ctx) {
        return null;
    }
}
