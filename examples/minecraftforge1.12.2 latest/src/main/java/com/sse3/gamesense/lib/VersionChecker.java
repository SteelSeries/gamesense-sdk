package com.sse3.gamesense.lib;

import com.sse3.gamesense.GameSenseMod;

import net.minecraft.client.Minecraft;

import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.versioning.ComparableVersion;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

import java.util.ArrayList;
import java.util.function.Function;

public class VersionChecker {
    private static final ArrayList<String> updates = new ArrayList<>();

    private static class ThreadUpdateCheck extends Thread {
        private final URL url;
        private final Function<String, Void> handler;

        ThreadUpdateCheck(URL url, Function<String, Void> handler) {
            this.url = url;
            this.handler = handler;

            setName("GameSense Update Checker");
        }

        @Override
        public void run() {
            try {
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                BufferedReader read = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String ret = read.readLine();
                read.close();
                if (ret == null) {
                    ret = "";
                }
                handler.apply(ret);
            } catch (SocketTimeoutException ignored) {
            } catch (IOException iox) {
                iox.printStackTrace();
            }
        }
    }

    public static void tick() {
        Minecraft mc = Minecraft.getMinecraft();
        if (!mc.inGameHasFocus) {
            return;
        }

        synchronized (updates) {
            for (String updateMessage : updates) {
                System.out.println("message send");
                mc.player.sendMessage(new TextComponentString(updateMessage));
            }
            updates.clear();
        }
    }

    private static void addUpdateMessage(String s) {
        synchronized (updates) {
            updates.add(s);
        }
    }

    public static void updateCheck(final String mod) {
        updateCheck("https://lateur.pro/mods/notifications/index.php?" +
                "mcversion=" + GameSenseMod.MINECRAFTVERSIONS + "&" +
                "file=" +  mod +"&"+
                "beta=" + GameSenseMod.beta, ret -> {
                    if (!ret.startsWith("Ret: ")) {
                        GameSenseMod.logger.error(String.format("Failed to check update for %s returned: %s", mod, ret));
                        return null;
                    }
                    ComparableVersion newVersion = new ComparableVersion(ret.substring(5));
                    if (newVersion.compareTo(new ComparableVersion(GameSenseMod.VERSION)) > 0) {
                        addUpdateMessage(String.format("Version %s of %s is available", newVersion, mod));
                    }
                    return null;
                });
    }

    private static void updateCheck(String url, Function<String, Void> handler) {
        try {
            new ThreadUpdateCheck(new URL(url), handler).start();
        } catch (MalformedURLException e) {
            String error = e.toString().isEmpty() ? "" : " Error: "+e.toString();
            GameSenseMod.logger.error("Malformed URL: " + url + error );
        }
    }
}
