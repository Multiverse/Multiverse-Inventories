package org.mvplugins.multiverse.inventories.profile.key;

import com.google.common.base.Objects;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.inventories.profile.data.PlayerProfile;

import java.util.UUID;

public final class ProfileKey extends ProfileFileKey {

    public static ProfileKey fromPlayerProfile(PlayerProfile profile) {
        return of(
                profile.getContainerType(),
                profile.getContainerName(),
                profile.getProfileType(),
                profile.getPlayerUUID(),
                profile.getPlayerName()
        );
    }

    public static ProfileKey of(
            ContainerType containerType,
            String dataName,
            ProfileType profileType,
            UUID playerUUID) {
        return of(containerType, dataName, profileType, GlobalProfileKey.of(playerUUID));
    }

    public static ProfileKey of(
            ContainerType containerType,
            String dataName,
            ProfileType profileType,
            GlobalProfileKey globalProfileKey) {
        return of(containerType, dataName, profileType, globalProfileKey.getPlayerUUID(), globalProfileKey.getPlayerName());
    }

    public static ProfileKey of(
            ContainerType containerType,
            String dataName,
            ProfileType profileType,
            OfflinePlayer offlinePlayer) {
        return of(containerType, dataName, profileType, offlinePlayer.getUniqueId(), offlinePlayer.getName());
    }

    public static ProfileKey of(
            ContainerType containerType,
            String dataName,
            ProfileType profileType,
            UUID playerUUID,
            String playerName) {
        return new ProfileKey(containerType, dataName, profileType, playerUUID, playerName);
    }

    private final ProfileType profileType;

    private ProfileKey(
            ContainerType containerType,
            String dataName,
            ProfileType profileType,
            UUID playerUUID,
            String playerName
    ) {
        super(containerType, dataName, playerUUID, playerName, Objects.hashCode(containerType, dataName, profileType, playerUUID));
        this.profileType = profileType;
    }

    @Override
    public ProfileKey forContainerType(@NotNull ContainerType containerType) {
        return new ProfileKey(containerType, dataName, profileType, playerUUID, playerName);
    }

    public ProfileType getProfileType() {
        return profileType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProfileKey that)) return false;
        return getContainerType() == that.getContainerType() &&
                Objects.equal(getDataName(), that.getDataName()) &&
                Objects.equal(getProfileType(), that.getProfileType()) &&
                Objects.equal(getPlayerUUID(), that.getPlayerUUID());
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
