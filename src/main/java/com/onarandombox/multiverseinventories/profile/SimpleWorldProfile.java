package com.onarandombox.multiverseinventories.profile;

import com.onarandombox.multiverseinventories.util.DeserializationException;
import com.onarandombox.multiverseinventories.util.MILog;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;

/**
 * Simple implementation of WorldProfile.
 */
public class SimpleWorldProfile implements WorldProfile {

    private HashMap<OfflinePlayer, PlayerProfile> playerData = new HashMap<OfflinePlayer, PlayerProfile>();
    private String worldName;

    public SimpleWorldProfile(String worldName) {
        this.worldName = worldName;
    }

    public SimpleWorldProfile(String worldName, ConfigurationSection section) throws DeserializationException {
        this(worldName);
        ConfigurationSection data = section.getConfigurationSection("playerData");
        if (data == null) {
            throw new DeserializationException("Missing playerData for world: " + worldName);
        }
        for (String playerName : data.getKeys(false)) {
            ConfigurationSection playerSection = data.getConfigurationSection(playerName);
            if (playerSection != null) {
                this.addPlayerData(new SimplePlayerProfile(playerName, playerSection));
            } else {
                MILog.warning("Player data invalid for world: " + worldName + " and player: " + playerName);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public World getBukkitWorld() {
        return Bukkit.getWorld(this.getWorld());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getWorld() {
        return this.worldName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setWorld(String worldName) {
        this.worldName = this.worldName;
    }

    /**
     * @return The map of bukkit players to their player profiles for this world profile.
     */
    protected HashMap<OfflinePlayer, PlayerProfile> getPlayerData() {
        return this.playerData;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PlayerProfile getPlayerData(OfflinePlayer player) {
        PlayerProfile playerProfile = this.playerData.get(player);
        if (playerProfile == null) {
            playerProfile = new SimplePlayerProfile(player);
            this.playerData.put(player, playerProfile);
        }
        return playerProfile;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addPlayerData(PlayerProfile playerProfile) {
        this.getPlayerData().put(playerProfile.getPlayer(), playerProfile);
    }
}
