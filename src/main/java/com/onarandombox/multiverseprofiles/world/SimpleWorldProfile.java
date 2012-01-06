package com.onarandombox.multiverseprofiles.world;

import com.onarandombox.multiverseprofiles.player.PlayerProfile;
import com.onarandombox.multiverseprofiles.player.SimplePlayerProfile;
import com.onarandombox.multiverseprofiles.util.ProfilesLog;
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

    public static WorldProfile deserialize(String worldName, ConfigurationSection section) {
        WorldProfile worldProfile = new SimpleWorldProfile(worldName);
        ConfigurationSection playerData = section.getConfigurationSection("playerData");
        for (String playerName : playerData.getKeys(false)) {
            if (playerData.get(playerName) instanceof PlayerProfile) {
                PlayerProfile playerProfile = (PlayerProfile)playerData.get(playerName);
                if (playerProfile != null) {
                    worldProfile.addPlayerData(playerProfile);
                } else {
                    ProfilesLog.warning("Unable to load a player's data for world: " + worldProfile.getWorld());
                }
            }
        }
        return worldProfile;
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
