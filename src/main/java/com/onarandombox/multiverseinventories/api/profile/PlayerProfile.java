package com.onarandombox.multiverseinventories.api.profile;

import com.onarandombox.multiverseinventories.share.Sharable;
import org.bukkit.OfflinePlayer;

import java.util.Map;

/**
 * Interface for a PlayerProfile which controls all the world specific data for a player.
 */
public interface PlayerProfile {

    /**
     * @return A map containing all the player data to be saved to disk.
     */
    Map<String, Object> serialize();

    /**
     * @return The type of profile.
     */
    ProfileType getType();

    /**
     * @return The name of the container, world or group, containing this profile.
     */
    String getContainerName();

    /**
     * @return the Player associated with this profile.
     */
    OfflinePlayer getPlayer();

    <T> T get(Sharable<T> sharable);

    <T> void set(Sharable<T> sharable, T value);
}

