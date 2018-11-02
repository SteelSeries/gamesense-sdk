package com.sse3.gamesense.config;

import com.sse3.gamesense.GameSenseMod;

import net.minecraftforge.common.config.Config;

import static net.minecraftforge.common.config.Config.*;

@Config(modid = GameSenseMod.MODID, name = GameSenseMod.MODNAME, category = "")
public class ModConfig {

    public static ModConfiguration modconfig = new ModConfiguration();

    public static class ModConfiguration{
        @Name("Mod Enabled")
        @RequiresMcRestart
        public boolean modEnabled = true;

        @Name("Check for Updates")
        public boolean CheckForUpdates = true;
    }
}
