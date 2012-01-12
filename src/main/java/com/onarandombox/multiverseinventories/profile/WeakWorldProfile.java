package com.onarandombox.multiverseinventories.profile;

import com.onarandombox.multiverseinventories.data.MIData;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Simple implementation of WorldProfile.
 */
public class WeakWorldProfile implements WorldProfile {

    private Map<OfflinePlayer, PlayerProfile> playerData = new WeakHashMap<OfflinePlayer, PlayerProfile>();
    private String worldName;
    private MIData data;

    public WeakWorldProfile(MIData data, String worldName) {
        this.data = data;
        this.worldName = worldName;
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
    protected Map<OfflinePlayer, PlayerProfile> getPlayerData() {
        return this.playerData;
    }
    
    private MIData getData() {
        return this.data;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PlayerProfile getPlayerData(OfflinePlayer player) {
        PlayerProfile playerProfile = this.playerData.get(player);
        if (playerProfile == null) {
            playerProfile = this.getData().getPlayerData(this.getWorld(), player.getName());
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
