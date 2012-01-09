package com.onarandombox.multiverseinventories.profile;

import com.onarandombox.multiverseinventories.profile.PlayerProfile;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;

import java.util.HashMap;

public interface WorldProfile {

    public World getBukkitWorld();

    public String getWorld();

    public void setWorld(String worldName);

    public HashMap<OfflinePlayer, PlayerProfile> getPlayerData();

    public PlayerProfile getPlayerData(OfflinePlayer player);

    public void addPlayerData(PlayerProfile playerProfile);
}
