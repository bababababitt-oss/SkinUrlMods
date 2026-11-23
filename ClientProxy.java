package com.skinurl;

import cpw.mods.fml.common.FMLCommonHandler;
import java.io.File;
import net.minecraftforge.client.ClientCommandHandler;

public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(File configDir) {
        super.preInit(configDir);
        ClientSkins skins = new ClientSkins();
        ClientSkins.load(configDir);
        FMLCommonHandler.instance().bus().register(skins);
        ClientCommandHandler.instance.registerCommand(new CommandSkinUrl());
    }

    @Override
    public void onSkinSync(String name, String url) {
        ClientSkins.setRemote(name, url);
    }
}
