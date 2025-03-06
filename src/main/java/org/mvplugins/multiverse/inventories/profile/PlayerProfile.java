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
public final class PlayerProfile extends ProfileDataSnapshot {

    static PlayerProfile createPlayerProfile(ContainerType containerType, String containerName,
                                                    ProfileType profileType, OfflinePlayer player) {
        return new PlayerProfile(containerType, containerName, profileType, player);
    }

    private final OfflinePlayer player;
    private final ContainerType containerType;
    private final String containerName;
    private final ProfileType profileType;

    private PlayerProfile(ContainerType containerType, String containerName, ProfileType profileType, OfflinePlayer player) {
        super();
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

    public PlayerProfile clone() {
        return (PlayerProfile) super.clone();
    }

    @Override
    public String toString() {
        return "PlayerProfile{" +
                "player=" + player.getName() +
                ", containerType=" + containerType +
                ", containerName='" + containerName + '\'' +
                ", profileType=" + profileType +
                '}';
    }
}
