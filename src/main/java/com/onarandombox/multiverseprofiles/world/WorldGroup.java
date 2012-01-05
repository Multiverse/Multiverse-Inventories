package com.onarandombox.multiverseprofiles.world;

import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.HashSet;

public interface WorldGroup extends ConfigurationSerializable {

    public String getName();

    public void setName(String name);

    public void addWorld(World world);

    public HashSet<World> getWorlds();

    public Shares getShares();
}
