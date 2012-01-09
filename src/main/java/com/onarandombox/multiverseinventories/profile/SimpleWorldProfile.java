package com.onarandombox.multiverseinventories.profile;

import com.onarandombox.multiverseinventories.util.DeserializationException;
import com.onarandombox.multiverseinventories.util.MILog;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;

/**
 * @author dumptruckman
 */
public class SimpleWorldProfile implements WorldProfile {

    private HashMap<OfflinePlayer, PlayerProfile> playerData = new HashMap<OfflinePlayer, PlayerProfile>();
    private String worldName;

    public SimpleWorldProfile(String worldName) {
        this.worldName = worldName;
    }

    public SimpleWorldProfile(String worldName, ConfigurationSection section) throws DeserializationException {
        this(worldName);
        ConfigurationSection playerData = section.getConfigurationSection("playerData");
        if (playerData == null) {
            throw new DeserializationException("Missing playerData for world: " + worldName);
        }
        for (String playerName : playerData.getKeys(false)) {
            ConfigurationSection playerSection = playerData.getConfigurationSection(playerName);
            if (playerSection != null) {
                this.addPlayerData(new SimplePlayerProfile(playerName, playerSection));
            } else {
                MILog.warning("Player data invalid for world: " + worldName + " and player: " + playerName);
            }
        }
    }

    public World getBukkitWorld() {
        return Bukkit.getWorld(this.getWorld());
    }

    public String getWorld() {
        return this.worldName;
    }

    public void setWorld(String worldName) {
        this.worldName = this.worldName;
    }

    public HashMap<OfflinePlayer, PlayerProfile> getPlayerData() {
        return this.playerData;
    }

    public PlayerProfile getPlayerData(OfflinePlayer player) {
        PlayerProfile playerProfile = this.playerData.get(player);
        if (playerProfile == null) {
            playerProfile = new SimplePlayerProfile(player);
            this.playerData.put(player, playerProfile);
        }
        return playerProfile;
    }

    public void addPlayerData(PlayerProfile playerProfile) {
        this.getPlayerData().put(playerProfile.getPlayer(), playerProfile);
    }
}
