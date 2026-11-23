package com.skinurl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Dados e validacao compartilhados (lado do servidor guarda nome -> url). */
public class SkinData {

    public static final int MAX_URL = 300;

    /** Skins conhecidas pelo servidor (so em memoria; cada cliente reenvia a sua ao entrar). */
    public static final Map<String, String> SERVER = new ConcurrentHashMap<String, String>();

    /** Vazio = remover skin. Aceita apenas http/https, sem espacos nem caracteres de controle. */
    public static boolean isValid(String url) {
        if (url == null) {
            return false;
        }
        if (url.length() == 0) {
            return true;
        }
        if (url.length() > MAX_URL) {
            return false;
        }
        if (!(url.startsWith("http://") || url.startsWith("https://"))) {
            return false;
        }
        for (int i = 0; i < url.length(); i++) {
            char c = url.charAt(i);
            if (c <= 32 || c == 127) {
                return false;
            }
        }
        return true;
    }
}
