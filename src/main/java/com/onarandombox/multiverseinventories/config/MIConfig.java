package com.onarandombox.multiverseinventories.config;

import com.onarandombox.multiverseinventories.group.WorldGroup;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

/**
 * @author dumptruckman
 */
public interface MIConfig {

    public CommentedConfiguration getConfig();

    public boolean isDebugging();

    public String getLocale();

    public List<WorldGroup> getWorldGroups();
    
    public void updateWorldGroup(WorldGroup worldGroup);

    public boolean isFirstRun();

    public void setFirstRun(boolean firstRun);

    public void save();
}
