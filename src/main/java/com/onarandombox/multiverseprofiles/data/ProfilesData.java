package com.onarandombox.multiverseprofiles.data;

import com.onarandombox.multiverseprofiles.world.WorldProfile;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;

/**
 * @author dumptruckman
 */
public interface ProfilesData {

    public void save();
    
    public FileConfiguration getData();

    public HashMap<String, WorldProfile> getWorldProfiles();
}
