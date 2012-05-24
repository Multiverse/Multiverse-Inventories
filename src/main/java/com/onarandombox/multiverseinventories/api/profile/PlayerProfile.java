package com.onarandombox.multiverseinventories.api.profile;

import com.onarandombox.multiverseinventories.api.share.Sharable;
import org.bukkit.OfflinePlayer;

import java.util.Map;

/**
 * Interface for a PlayerProfile which controls all the world/group specific data for a player.
 * This represents what is saved/loaded to/from persistence.
 */
public interface PlayerProfile {

    /**
     * @return A map containing all the player data to be saved to disk.
     */
    Map<String, Object> serialize();

    /**
     * @return The type of profile.
     */
    ContainerType getType();

    /**
     * @return The name of the container, world or group, containing this profile.
     */
    String getContainerName();

    /**
     * @return the Player associated with this profile.
     */
    OfflinePlayer getPlayer();

    /**
     * Retrieves the profile's value of the {@link Sharable} passed in for the
     * {@link com.onarandombox.multiverseinventories.ProfileTypes#DEFAULT} profile.
     *
     * @param sharable Represents the key for the data wanted from the profile.
     * @param <T>      This indicates the type of return value to be expected.
     * @return The value of the sharable for this profile.  Null if no value is set.
     */
    <T> T get(Sharable<T> sharable);

    /**
     * Sets the profile's value for the {@link Sharable} passed in for the
     * {@link com.onarandombox.multiverseinventories.ProfileTypes#DEFAULT} profile.
     *
     * @param sharable Represents the key for the data to store.
     * @param value    The value of the data.
     * @param <T>      The type of value to be expected.
     */
    <T> void set(Sharable<T> sharable, T value);

    /**
     * Retrieves the profile's value of the {@link Sharable} passed in for the specified {@link ProfileType}.
     *
     * @param profileType The type of profile to get data for such as default or creative.
     * @param sharable Represents the key for the data wanted from the profile.
     * @param <T>      This indicates the type of return value to be expected.
     * @return The value of the sharable for this profile.  Null if no value is set.
     */
    <T> T get(ProfileType profileType, Sharable<T> sharable);

    /**
     * Sets the profile's value for the {@link Sharable} passed in for the specified {@link ProfileType}.
     *
     * @param profileType The type of profile to set data for such as default or creative.
     * @param sharable Represents the key for the data to store.
     * @param value    The value of the data.
     * @param <T>      The type of value to be expected.
     */
    <T> void set(ProfileType profileType, Sharable<T> sharable, T value);
}

