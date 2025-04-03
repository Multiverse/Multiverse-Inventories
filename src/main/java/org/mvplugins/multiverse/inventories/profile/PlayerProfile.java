package org.mvplugins.multiverse.inventories.profile;

import org.mvplugins.multiverse.inventories.profile.key.ProfileKey;
import org.mvplugins.multiverse.inventories.profile.key.ProfileType;
import org.mvplugins.multiverse.inventories.profile.key.ContainerType;

import java.util.UUID;

/**
 * Contains all the world/group specific data for a player.
 */
public final class PlayerProfile extends ProfileDataSnapshot {

    static PlayerProfile createPlayerProfile(ProfileKey profileKey) {
        return new PlayerProfile(
                profileKey.getContainerType(),
                profileKey.getDataName(),
                profileKey.getProfileType(),
                profileKey.getPlayerUUID(),
                profileKey.getPlayerName()
        );
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
                "playerUUID=" + playerUUID +
                ", playerName='" + playerName + '\'' +
                ", containerType=" + containerType +
                ", containerName='" + containerName + '\'' +
                ", profileType=" + profileType +
                '}';
    }
}
