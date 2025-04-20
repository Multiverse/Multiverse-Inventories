package org.mvplugins.multiverse.inventories.profile.key;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.external.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

public sealed class GlobalProfileKey permits ProfileFileKey {

    public static GlobalProfileKey create(OfflinePlayer offlinePlayer) {
        return create(offlinePlayer.getUniqueId(), offlinePlayer.getName());
    }

    public static GlobalProfileKey create(UUID playerUUID, String playerName) {
        return new GlobalProfileKey(playerUUID, playerName);
    }

    protected final UUID playerUUID;
    protected final String playerName;

    protected GlobalProfileKey(UUID playerUUID, String playerName) {
        this.playerUUID = playerUUID;
        this.playerName = playerName;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public String getPlayerName() {
        return playerName;
    }

    public @NotNull OfflinePlayer getOfflinePlayer() {
        return Bukkit.getOfflinePlayer(playerUUID);
    }

    public @Nullable Player getOnlinePlayer() {
        return Bukkit.getPlayer(playerUUID);
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
