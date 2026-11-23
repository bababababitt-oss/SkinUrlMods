package com.skinurl;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.ImageBufferDownload;
import net.minecraft.client.renderer.ThreadDownloadImageData;
import net.minecraft.util.ResourceLocation;

/** Lado do cliente: guarda quem usa qual URL, baixa as imagens e aplica nos jogadores. */
public class ClientSkins {

    /** nome do jogador -> url (escrito por varias threads, lido na tick). */
    private static final Map<String, String> urls = new ConcurrentHashMap<String, String>();

    /** url -> textura ja registrada (so thread principal). */
    private static final Map<String, ResourceLocation> textures = new HashMap<String, ResourceLocation>();

    /** nome -> skin original do jogador, para poder restaurar (so thread principal). */
    private static final Map<String, ResourceLocation> originals = new HashMap<String, ResourceLocation>();

    private static String ownUrl = "";
    private static File saveFile;
    private static volatile boolean announce = false;

    private static Field skinField;
    private static boolean fieldSearched = false;

    // ---------- chamadas de fora ----------

    public static void load(File configDir) {
        saveFile = new File(configDir, "skinurl.txt");
        try {
            if (saveFile.isFile()) {
                BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(saveFile), "UTF-8"));
                try {
                    String line = r.readLine();
                    if (line != null && SkinData.isValid(line.trim())) {
                        ownUrl = line.trim();
                    }
                } finally {
                    r.close();
                }
            }
        } catch (Throwable t) {
            System.out.println("[SkinUrl] nao consegui ler skinurl.txt: " + t);
        }
    }

    private static void save() {
        if (saveFile == null) {
            return;
        }
        try {
            OutputStreamWriter w = new OutputStreamWriter(new FileOutputStream(saveFile), "UTF-8");
            try {
                w.write(ownUrl);
            } finally {
                w.close();
            }
        } catch (Throwable t) {
            System.out.println("[SkinUrl] nao consegui salvar skinurl.txt: " + t);
        }
    }

    /** O proprio jogador escolheu (ou resetou) a skin. */
    public static void setOwn(String url) {
        ownUrl = url == null ? "" : url;
        save();
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) {
            return;
        }
        String me = mc.thePlayer.getCommandSenderName();
        if (ownUrl.length() == 0) {
            urls.remove(me);
        } else {
            urls.put(me, ownUrl);
        }
        try {
            SkinUrlMod.network.sendToServer(new PacketSetSkin(ownUrl));
        } catch (Throwable t) {
            // servidor sem o mod: a skin continua valendo so para voce
        }
    }

    /** O servidor avisou a skin de algum jogador. */
    public static void setRemote(String name, String url) {
        if (name == null || name.length() == 0) {
            return;
        }
        if (url == null || url.length() == 0) {
            urls.remove(name);
        } else {
            urls.put(name, url);
        }
    }

    // ---------- eventos ----------

    @SubscribeEvent
    public void onConnect(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        announce = true;
    }

    @SubscribeEvent
    public void onDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        urls.clear();
        announce = false;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null || mc.thePlayer == null) {
            return;
        }

        String me = mc.thePlayer.getCommandSenderName();
        if (announce) {
            announce = false;
            if (ownUrl.length() > 0) {
                urls.put(me, ownUrl);
                try {
                    SkinUrlMod.network.sendToServer(new PacketSetSkin(ownUrl));
                } catch (Throwable t) {
                    // servidor sem o mod
                }
            }
        }

        for (Object o : mc.theWorld.playerEntities) {
            if (!(o instanceof AbstractClientPlayer)) {
                continue;
            }
            AbstractClientPlayer p = (AbstractClientPlayer) o;
            String name = p.getCommandSenderName();
            String url = urls.get(name);
            if (url != null) {
                if (!originals.containsKey(name)) {
                    originals.put(name, getSkin(p));
                }
                setSkin(p, textureFor(url));
            } else if (originals.containsKey(name)) {
                setSkin(p, originals.remove(name));
            }
        }
    }

    // ---------- internos ----------

    private static ResourceLocation textureFor(String url) {
        ResourceLocation loc = textures.get(url);
        if (loc == null) {
            loc = new ResourceLocation("skinurl", "skin/" + sha1(url));
            ThreadDownloadImageData data = new ThreadDownloadImageData(
                    null, url, AbstractClientPlayer.locationStevePng, new ImageBufferDownload());
            Minecraft.getMinecraft().getTextureManager().loadTexture(loc, data);
            textures.put(url, loc);
        }
        return loc;
    }

    private static String sha1(String s) {
        try {
            byte[] d = MessageDigest.getInstance("SHA-1").digest(s.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : d) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception e) {
            return Integer.toHexString(s.hashCode());
        }
    }

    /** Acha o campo privado da skin do jogador (nome normal, nome SRG, ou o 1o ResourceLocation de instancia). */
    private static Field findSkinField() {
        if (fieldSearched) {
            return skinField;
        }
        fieldSearched = true;
        String[] names = {"locationSkin", "field_110312_d"};
        for (String n : names) {
            try {
                Field f = AbstractClientPlayer.class.getDeclaredField(n);
                f.setAccessible(true);
                skinField = f;
                return f;
            } catch (Throwable t) {
                // tenta o proximo
            }
        }
        for (Field f : AbstractClientPlayer.class.getDeclaredFields()) {
            if (!Modifier.isStatic(f.getModifiers()) && f.getType() == ResourceLocation.class) {
                f.setAccessible(true);
                skinField = f;
                return f;
            }
        }
        System.out.println("[SkinUrl] ERRO: nao achei o campo da skin do jogador.");
        return null;
    }

    private static ResourceLocation getSkin(AbstractClientPlayer p) {
        Field f = findSkinField();
        if (f == null) {
            return null;
        }
        try {
            return (ResourceLocation) f.get(p);
        } catch (Throwable t) {
            return null;
        }
    }

    private static void setSkin(AbstractClientPlayer p, ResourceLocation loc) {
        Field f = findSkinField();
        if (f == null) {
            return;
        }
        try {
            f.set(p, loc);
        } catch (Throwable t) {
            // ignora
        }
    }
}
