package com.sse3.gamesense.config;

import net.minecraftforge.common.config.Configuration;
import java.io.File;

public class LoadConfig {
    public boolean ModEnabled = true;
    public boolean CheckForUpdates = true;

    public LoadConfig(File file) {
        if (!file.exists()) {
            return;
        }

        Configuration config = new Configuration(file);
        ModEnabled = config.get("modconfig", "Mod Enabled", true).getBoolean();
        CheckForUpdates = config.get("modconfig", "Check for Updates", true).getBoolean();
    }
}