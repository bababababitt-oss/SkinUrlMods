package com.skinurl;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import java.io.File;
import java.util.Map;
import net.minecraft.entity.player.EntityPlayerMP;

public class CommonProxy {

    public void preInit(File configDir) {
        FMLCommonHandler.instance().bus().register(new ServerEvents());
    }

    /** Chamado (no cliente) quando o servidor informa a skin de um jogador. */
    public void onSkinSync(String name, String url) {
    }

    public static class ServerEvents {
        /** Quem entra recebe a lista de skins de quem ja esta no servidor. */
        @SubscribeEvent
        public void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
            if (!(event.player instanceof EntityPlayerMP)) {
                return;
            }
            EntityPlayerMP target = (EntityPlayerMP) event.player;
            for (Map.Entry<String, String> e : SkinData.SERVER.entrySet()) {
                SkinUrlMod.network.sendTo(new PacketSyncSkin(e.getKey(), e.getValue()), target);
            }
        }
    }
}
