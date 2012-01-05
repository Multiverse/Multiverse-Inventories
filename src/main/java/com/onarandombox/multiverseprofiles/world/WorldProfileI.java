package com.onarandombox.multiverseprofiles.world;

import com.onarandombox.multiverseprofiles.player.PlayerProfileI;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.HashMap;

public interface WorldProfileI extends ConfigurationSerializable {

    public World getWorld();

    public void setWorld(World world);

    public HashMap<OfflinePlayer, PlayerProfileI> getPlayerData();

    public PlayerProfileI getPlayerData(OfflinePlayer player);

    public void addPlayerData(PlayerProfileI playerProfile);
}
