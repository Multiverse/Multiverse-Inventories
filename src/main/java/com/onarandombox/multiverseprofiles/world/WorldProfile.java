package com.onarandombox.multiverseprofiles.world;

import com.onarandombox.multiverseprofiles.player.PlayerProfile;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.HashMap;

public interface WorldProfile extends ConfigurationSerializable {

    public World getWorld();

    public void setWorld(World world);

    public HashMap<OfflinePlayer, PlayerProfile> getPlayerData();

    public PlayerProfile getPlayerData(OfflinePlayer player);

    public void addPlayerData(PlayerProfile playerProfile);
}
