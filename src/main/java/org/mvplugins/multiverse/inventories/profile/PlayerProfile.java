package org.mvplugins.multiverse.inventories.profile;

import org.mvplugins.multiverse.inventories.share.Sharable;
import org.mvplugins.multiverse.inventories.profile.container.ContainerType;
import org.bukkit.OfflinePlayer;
import org.mvplugins.multiverse.inventories.share.Sharables;

import java.util.HashMap;
import java.util.Map;

/**
 * Contains all the world/group specific data for a player.
 */
public final class PlayerProfile implements Cloneable {

    static PlayerProfile createPlayerProfile(ContainerType containerType, String containerName,
                                                    ProfileType profileType, OfflinePlayer player) {
        return new PlayerProfile(containerType, containerName, profileType, player);
    }

    private final Map<Sharable, Object> data = new HashMap<>(Sharables.all().size());

    private final OfflinePlayer player;
    private final ContainerType containerType;
    private final String containerName;
    private final ProfileType profileType;

    private PlayerProfile(ContainerType containerType, String containerName, ProfileType profileType, OfflinePlayer player) {
        this.containerType = containerType;
        this.profileType = profileType;
        this.containerName = containerName;
        this.player = player;
    }

    /**
     * @return The container type of profile, a group or world.
     */
    public ContainerType getContainerType() {
        return this.containerType;
    }

    /**
     * @return The name of the container, world or group, containing this profile.
     */
    public String getContainerName() {
        return this.containerName;
    }

    /**
     * @return the Player associated with this profile.
     */
    public OfflinePlayer getPlayer() {
        return this.player;
    }

    /**
     * @return The type of profile this object represents.
     */
    public ProfileType getProfileType() {
        return this.profileType;
    }

    /**
     * Retrieves the profile's value of the {@link Sharable} passed in.
     *
     * @param sharable Represents the key for the data wanted from the profile.
     * @param <T>      This indicates the type of return value to be expected.
     * @return The value of the sharable for this profile. Null if no value is set.
     */
    public <T> T get(Sharable<T> sharable) {
        return sharable.getType().cast(this.data.get(sharable));
    }

    /**
     * Sets the profile's value for the {@link Sharable} passed in.
     *
     * @param sharable Represents the key for the data to store.
     * @param value    The value of the data.
     * @param <T>      The type of value to be expected.
     */
    public <T> void set(Sharable<T> sharable, T value) {
        this.data.put(sharable, value);
    }

    public PlayerProfile clone() {
        try {
            return (PlayerProfile) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<Sharable, Object> getData() {
        return data;
    }
}
