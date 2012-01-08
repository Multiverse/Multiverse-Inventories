package com.onarandombox.multiverseinventories.data;

import com.onarandombox.multiverseinventories.world.WorldProfile;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;

/**
 * @author dumptruckman
 */
public interface MIData {

    public void save();

    public FileConfiguration getData();

    public HashMap<String, WorldProfile> getWorldProfiles();
}
