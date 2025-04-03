package org.mvplugins.multiverse.inventories.profile.key;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.Objects;
import java.util.UUID;

public class GlobalProfileKey {

    public static GlobalProfileKey create(UUID playerUUID) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerUUID);
        return create(offlinePlayer);
    }

    public static GlobalProfileKey create(OfflinePlayer offlinePlayer) {
        return create(offlinePlayer.getUniqueId(), offlinePlayer.getName());
    }

    public static GlobalProfileKey create(UUID playerUUID, String playerName) {
        return new GlobalProfileKey(playerUUID, playerName);
    }

    private final UUID playerUUID;
    private final String playerName;

    private GlobalProfileKey(UUID playerUUID, String playerName) {
        this.playerUUID = playerUUID;
        this.playerName = playerName;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public String getPlayerName() {
        return playerName;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        GlobalProfileKey that = (GlobalProfileKey) o;
        return Objects.equals(playerUUID, that.playerUUID);
    }

    @Override
    public int hashCode() {
        return playerUUID.hashCode();
    }

    @Override
    public String toString() {
        return "GlobalProfileKey{" +
                "playerUUID=" + playerUUID +
                ", playerName='" + playerName + '\'' +
                '}';
    }
}
