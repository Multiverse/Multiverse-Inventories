package org.mvplugins.multiverse.inventories.profile.key;

import com.google.common.base.Objects;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mvplugins.multiverse.inventories.profile.PlayerProfile;

import java.util.UUID;

public sealed class ProfileFileKey permits ProfileKey {

    public static ProfileFileKey create(
            ContainerType containerType,
            String dataName,
            UUID playerUUID,
            String playerName) {
        return new ProfileFileKey(containerType, dataName, playerUUID, playerName);
    }

    public static ProfileFileKey create(
            ContainerType containerType,
            String dataName,
            OfflinePlayer offlinePlayer) {
        return new ProfileFileKey(containerType, dataName, offlinePlayer.getUniqueId(), offlinePlayer.getName());
    }

    public static ProfileFileKey create(
            ContainerType containerType,
            String dataName,
            UUID playerUUID) {
        return new ProfileFileKey(containerType, dataName, playerUUID, Bukkit.getOfflinePlayer(playerUUID).getName());
    }

    public static ProfileFileKey fromPlayerProfile(PlayerProfile profile) {
        return new ProfileFileKey(
                profile.getContainerType(),
                profile.getContainerName(),
                profile.getPlayerUUID(),
                profile.getPlayerName()
        );
    }

    protected final ContainerType containerType;
    protected final String dataName;
    protected final String playerName;
    protected final UUID playerUUID;
    protected final int hashCode;

    private ProfileFileKey(ContainerType containerType, String dataName, UUID playerUUID, String playerName) {
        this(containerType,
                dataName,
                playerUUID,
                playerName,
                Objects.hashCode(containerType, dataName, playerUUID, playerName));
    }

    protected ProfileFileKey(ContainerType containerType, String dataName, UUID playerUUID, String playerName, int hashCode) {
        this.containerType = containerType;
        this.dataName = dataName;
        this.playerUUID = playerUUID;
        this.playerName = playerName;
        this.hashCode = hashCode;
    }

    public ProfileKey forProfileType(@Nullable ProfileType profileType) {
        return ProfileKey.create(containerType, dataName, profileType, playerUUID, playerName);
    }

    public ProfileFileKey forContainerType(@NotNull ContainerType containerType) {
        return new ProfileFileKey(containerType, dataName, playerUUID, playerName);
    }

    public ContainerType getContainerType() {
        return containerType;
    }

    public String getDataName() {
        return dataName;
    }

    public String getPlayerName() {
        return playerName;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public boolean isSameFile(ProfileFileKey other) {
        return Objects.equal(getContainerType(), other.getContainerType()) &&
                Objects.equal(getDataName(), other.getDataName()) &&
                Objects.equal(getPlayerUUID(), other.getPlayerUUID());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProfileKey that)) return false;
        return getContainerType() == that.getContainerType() &&
                Objects.equal(getDataName(), that.getDataName()) &&
                Objects.equal(getPlayerUUID(), that.getPlayerUUID());
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public String toString() {
        return "ProfileFileKey{" +
                "containerType=" + containerType +
                ", dataName='" + dataName + '\'' +
                ", playerName='" + playerName + '\'' +
                ", playerUUID=" + playerUUID +
                '}';
    }
}
