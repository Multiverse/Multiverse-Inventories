package com.onarandombox.multiverseinventories.data;

import com.onarandombox.multiverseinventories.profile.WorldProfile;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

/**
 * @author dumptruckman
 */
public interface MIData {

    public void save();

    public FileConfiguration getData();

    public List<WorldProfile> getWorldProfiles();
}
