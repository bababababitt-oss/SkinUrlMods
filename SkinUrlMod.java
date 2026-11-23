package com.skinurl;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

@Mod(modid = SkinUrlMod.MODID, name = "Skin URL", version = "1.0.0", acceptableRemoteVersions = "*")
public class SkinUrlMod {

    public static final String MODID = "skinurl";

    public static SimpleNetworkWrapper network;

    @SidedProxy(clientSide = "com.skinurl.ClientProxy", serverSide = "com.skinurl.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        network = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);
        // cliente -> servidor: "quero esta skin"
        network.registerMessage(PacketSetSkin.Handler.class, PacketSetSkin.class, 0, Side.SERVER);
        // servidor -> todos os clientes: "jogador X usa esta skin"
        network.registerMessage(PacketSyncSkin.Handler.class, PacketSyncSkin.class, 1, Side.CLIENT);
        proxy.preInit(event.getModConfigurationDirectory());
    }
}
