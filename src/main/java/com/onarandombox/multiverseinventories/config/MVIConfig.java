package com.onarandombox.multiverseinventories.config;

import org.bukkit.configuration.file.FileConfiguration;

import java.io.IOException;

/**
 * @author dumptruckman
 */
public interface MVIConfig {

    /**
     * Loads the configuration data into memory and sets defaults
     * @throws java.io.IOException
     */
    public void load() throws Exception;

    public FileConfiguration getConfig();

    public boolean isDebugging();

    public long getDataSaveInterval();

    public String getLanguageFileName();

    public void loadWorldGroups();
}
