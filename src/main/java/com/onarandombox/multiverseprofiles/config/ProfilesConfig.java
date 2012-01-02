package com.onarandombox.multiverseprofiles.config;

import org.bukkit.configuration.file.FileConfiguration;

/**
 * @author dumptruckman
 */
public interface ProfilesConfig {

    /**
     * Loads the configuration data into memory and sets defaults
     *
     * @throws java.io.IOException
     */
    public void load() throws Exception;

    public FileConfiguration getConfig();

    public boolean isDebugging();

    public long getDataSaveInterval();

    public String getLanguageFileName();

    public void loadWorldGroups();

    //public Shares getDefaultShares();
}
