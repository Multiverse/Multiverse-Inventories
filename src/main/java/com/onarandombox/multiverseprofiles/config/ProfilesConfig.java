package com.onarandombox.multiverseprofiles.config;

import org.bukkit.configuration.file.FileConfiguration;

/**
 * @author dumptruckman
 */
public interface ProfilesConfig {

    public FileConfiguration getConfig();

    public boolean isDebugging();

    public long getDataSaveInterval();

    public String getLanguageFileName();

    public void loadWorldGroups();

    //public Shares getDefaultShares();
}
