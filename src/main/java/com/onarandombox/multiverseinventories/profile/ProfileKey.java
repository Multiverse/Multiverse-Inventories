package com.onarandombox.multiverseinventories.profile;

import com.google.common.base.Objects;
import com.onarandombox.multiverseinventories.profile.container.ContainerType;
import org.bukkit.Bukkit;

import java.util.UUID;

public final class ProfileKey {

    public static ProfileKey createProfileKey(ContainerType containerType, String dataName,
                                              ProfileType profileType, UUID playerUUID, String playerName) {
        return new ProfileKey(containerType, dataName, profileType, playerUUID, playerName);
    }

    public static ProfileKey createProfileKey(ContainerType containerType, String dataName,
                                              ProfileType profileType, UUID playerUUID) {
        return new ProfileKey(containerType, dataName, profileType, playerUUID);
    }

    public static ProfileKey createProfileKey(ProfileKey copyKey, ContainerType containerType) {
        return new ProfileKey(containerType, copyKey.getDataName(), copyKey.getProfileType(), copyKey.getPlayerUUID(),
                copyKey.getPlayerName());
    }

    public static ProfileKey createProfileKey(ProfileKey copyKey, ProfileType profileType) {
        return new ProfileKey(copyKey.getContainerType(), copyKey.getDataName(), profileType, copyKey.getPlayerUUID(),
                copyKey.getPlayerName());
    }

    public static ProfileKey createProfileKey(ProfileKey copyKey, ContainerType containerType,
                                              ProfileType profileType) {
        return new ProfileKey(containerType, copyKey.getDataName(), profileType, copyKey.getPlayerUUID(),
                copyKey.getPlayerName());
    }

    private final ContainerType containerType;
    private final String dataName;
    private final ProfileType profileType;
    private final String playerName;
    private final UUID playerUUID;

    private ProfileKey(ContainerType containerType, String dataName, ProfileType profileType, UUID playerUUID) {
        this.containerType = containerType;
        this.dataName = dataName;
        this.profileType = profileType;
        this.playerUUID = playerUUID;
        this.playerName = Bukkit.getOfflinePlayer(playerUUID).getName();
    }

    private ProfileKey(ContainerType containerType, String dataName, ProfileType profileType,
                       UUID playerUUID, String playerName) {
        this.containerType = containerType;
        this.dataName = dataName;
        this.profileType = profileType;
        this.playerUUID = playerUUID;
        this.playerName = playerName;
    }

    public ContainerType getContainerType() {
        return containerType;
    }

    public String getDataName() {
        return dataName;
    }

    public ProfileType getProfileType() {
        return profileType;
    }

    public String getPlayerName() {
        return playerName;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProfileKey)) return false;
        final ProfileKey that = (ProfileKey) o;
        return getContainerType() == that.getContainerType() &&
                Objects.equal(getDataName(), that.getDataName()) &&
                Objects.equal(getProfileType(), that.getProfileType()) &&
                Objects.equal(getPlayerName(), that.getPlayerName()) &&
                Objects.equal(getPlayerUUID(), that.getPlayerUUID());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getContainerType(), getDataName(), getProfileType(), getPlayerName(), getPlayerUUID());
    }

    @Override
    public String toString() {
        return "ProfileKey{" +
                "containerType=" + containerType +
                ", dataName='" + dataName + '\'' +
                ", profileType=" + profileType +
                ", playerName='" + playerName + '\'' +
                ", playerUUID=" + playerUUID +
                '}';
    }
}
