package com.onarandombox.multiverseinventories.data;

import com.onarandombox.multiverseinventories.profile.WorldProfile;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

/**
 * Interface for interacting with the data of this plugin.
 */
public interface MIData {

    /**
     * Convenience method for saving the underlying FileConfiguration object.
     */
    void save();

    /**
     * Retrieves the underlying FileConfiguration object for direct manipulation of the data.
     *
     * @return the underlying FileConfiguration object.
     */
    FileConfiguration getData();

    /**
     * Retrieves a list of the World Profiles from the data file.
     *
     * @return The list of World Profiles from the data.
     */
    List<WorldProfile> getWorldProfiles();
}
