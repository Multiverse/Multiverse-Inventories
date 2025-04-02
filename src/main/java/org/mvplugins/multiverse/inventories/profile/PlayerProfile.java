package org.mvplugins.multiverse.inventories.profile;

import org.mvplugins.multiverse.inventories.profile.container.ContainerType;

import java.util.UUID;

/**
 * Contains all the world/group specific data for a player.
 */
public final class PlayerProfile extends ProfileDataSnapshot {

    static PlayerProfile createPlayerProfile(ContainerType containerType, String containerName,
                                             ProfileType profileType, UUID playerUUID, String playerName) {
        return new PlayerProfile(containerType, containerName, profileType, playerUUID, playerName);
    }

    private final ContainerType containerType;
    private final String containerName;
    private final ProfileType profileType;
    private final UUID playerUUID;
    private final String playerName;

    private PlayerProfile(ContainerType containerType, String containerName, ProfileType profileType, UUID playerUUID, String playerName) {
        super();
        this.containerType = containerType;
        this.profileType = profileType;
        this.containerName = containerName;
        this.playerUUID = playerUUID;
        this.playerName = playerName;
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
     * @return the Player uuid associated with this profile.
     */
    public UUID getPlayerUUID() {
        return this.playerUUID;
    }

    /**
     * @return the Player name associated with this profile.
     */
    public String getPlayerName() {
        return this.playerName;
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
                "player=" + playerName +
                ", containerType=" + containerType +
                ", containerName='" + containerName + '\'' +
                ", profileType=" + profileType +
                '}';
    }
}
