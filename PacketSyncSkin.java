package com.skinurl;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

/** Servidor -> clientes. Diz qual jogador usa qual skin (url vazia = skin normal). */
public class PacketSyncSkin implements IMessage {

    public String name = "";
    public String url = "";

    public PacketSyncSkin() {
    }

    public PacketSyncSkin(String name, String url) {
        this.name = name == null ? "" : name;
        this.url = url == null ? "" : url;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        name = ByteBufUtils.readUTF8String(buf);
        url = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, name);
        ByteBufUtils.writeUTF8String(buf, url);
    }

    public static class Handler implements IMessageHandler<PacketSyncSkin, IMessage> {
        @Override
        public IMessage onMessage(PacketSyncSkin message, MessageContext ctx) {
            if (SkinData.isValid(message.url)) {
                SkinUrlMod.proxy.onSkinSync(message.name, message.url);
            }
            return null;
        }
    }
}
