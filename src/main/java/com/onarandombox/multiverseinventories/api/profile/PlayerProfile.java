package com.onarandombox.multiverseinventories.api.profile;

import com.onarandombox.multiverseinventories.api.share.Sharable;
import org.bukkit.OfflinePlayer;

import java.util.Map;

/**
 * Interface for a PlayerProfile which controls all the world/group specific data for a player.
 * This represents what is saved/loaded to/from persistence.
 */
public interface PlayerProfile extends Cloneable {

    /**
     * @return A map containing all the player data to be saved to disk.
     */
    Map<String, Object> serialize();

    /**
     * @return The container type of profile, a group or world.
     */
    ContainerType getContainerType();

    /**
     * @return The name of the container, world or group, containing this profile.
     */
    String getContainerName();

    /**
     * @return the Player associated with this profile.
     */
    OfflinePlayer getPlayer();

    /**
     * @return The type of profile this object represents.
     */
    ProfileType getProfileType();

    /**
     * Retrieves the profile's value of the {@link Sharable} passed in.
     *
     * @param sharable Represents the key for the data wanted from the profile.
     * @param <T>      This indicates the type of return value to be expected.
     * @return The value of the sharable for this profile.  Null if no value is set.
     */
    <T> T get(Sharable<T> sharable);

    /**
     * Sets the profile's value for the {@link Sharable} passed in.
     *
     * @param sharable Represents the key for the data to store.
     * @param value    The value of the data.
     * @param <T>      The type of value to be expected.
     */
    <T> void set(Sharable<T> sharable, T value);

    PlayerProfile clone() throws CloneNotSupportedException;
}

