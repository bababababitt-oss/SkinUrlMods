package com.skinurl;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;

/** Cliente -> servidor. url vazia = voltar para a skin normal. */
public class PacketSetSkin implements IMessage {

    public String url = "";

    public PacketSetSkin() {
    }

    public PacketSetSkin(String url) {
        this.url = url == null ? "" : url;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        url = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, url);
    }

    public static class Handler implements IMessageHandler<PacketSetSkin, IMessage> {
        @Override
        public IMessage onMessage(PacketSetSkin message, MessageContext ctx) {
            if (!SkinData.isValid(message.url)) {
                return null;
            }
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            String name = player.getCommandSenderName();
            if (message.url.length() == 0) {
                SkinData.SERVER.remove(name);
            } else {
                SkinData.SERVER.put(name, message.url);
            }
            SkinUrlMod.network.sendToAll(new PacketSyncSkin(name, message.url));
            return null;
        }
    }
}
