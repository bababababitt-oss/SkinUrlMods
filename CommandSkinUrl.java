package com.skinurl;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

/** /skinurl <link>  |  /skinurl reset   (comando so do cliente) */
public class CommandSkinUrl extends CommandBase {

    @Override
    public String getCommandName() {
        return "skinurl";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/skinurl <link direto da imagem .png>  |  /skinurl reset";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length != 1) {
            sender.addChatMessage(new ChatComponentText("Uso: " + getCommandUsage(sender)));
            return;
        }
        String arg = args[0].trim();
        if (arg.equalsIgnoreCase("reset")) {
            ClientSkins.setOwn("");
            sender.addChatMessage(new ChatComponentText("Skin normal restaurada."));
            return;
        }
        if (arg.length() == 0 || !SkinData.isValid(arg)) {
            sender.addChatMessage(new ChatComponentText(
                    "Link invalido. Use um link http(s) direto para uma imagem de skin (ate " + SkinData.MAX_URL + " letras)."));
            return;
        }
        ClientSkins.setOwn(arg);
        sender.addChatMessage(new ChatComponentText(
                "Skin definida! Quem tiver o mod vai ver. (Se nao aparecer, o link precisa abrir direto a imagem .png)"));
    }
}
