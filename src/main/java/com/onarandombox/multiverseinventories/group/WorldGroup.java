package com.onarandombox.multiverseinventories.group;

import com.onarandombox.multiverseinventories.share.Shares;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashSet;

public interface WorldGroup {

    public String getName();

    public void setName(String name);

    public void addWorld(String worldName);

    public void addWorld(World world);

    public HashSet<String> getWorlds();

    public void setShares(Shares shares);

    public Shares getShares();

    public String getPermission();

    public void setPermission(String permission);

    public void serialize(ConfigurationSection groupData);
}
