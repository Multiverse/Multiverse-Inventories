package org.mvplugins.multiverse.inventories.profile.key;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.external.jetbrains.annotations.Nullable;
import org.mvplugins.multiverse.inventories.profile.PlayerNamesMapper;

import java.util.Objects;
import java.util.UUID;

public sealed class GlobalProfileKey permits ProfileFileKey {

    /**
     * Gets a GlobalProfileKey from a playerUUID. NOTE: Player name may be empty if player has never logged in before.
     *
     * @param playerUUID The player's UUID
     * @return A GlobalProfileKey
     */
    public static GlobalProfileKey of(UUID playerUUID) {
        return PlayerNamesMapper.getInstance().getKey(playerUUID)
                .getOrElse(() -> new GlobalProfileKey(playerUUID, playerUUID.toString()));
    }

    public static GlobalProfileKey of(OfflinePlayer offlinePlayer) {
        return of(offlinePlayer.getUniqueId(), offlinePlayer.getName());
    }

    public static GlobalProfileKey of(UUID playerUUID, String playerName) {
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
