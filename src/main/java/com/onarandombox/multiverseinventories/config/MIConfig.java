package com.onarandombox.multiverseinventories.config;

import com.onarandombox.multiverseinventories.world.WorldGroup;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.List;

/**
 * @author dumptruckman
 */
public interface MIConfig {

    public FileConfiguration getConfig();

    public boolean isDebugging();

    public long getDataSaveInterval();

    public String getLocale();

    public HashMap<String, List<WorldGroup>> getWorldGroups();

    //public Shares getDefaultShares();
}
