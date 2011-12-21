package com.onarandombox.multiverseinventories.data;

import com.onarandombox.multiverseinventories.MVIManager;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * @author dumptruckman
 */
public class WorldGroup implements ConfigurationSerializable {

    private String name = "";
    private HashSet<World> worlds = new HashSet<World>();
    private boolean sharingInventory = false;
    private boolean sharingArmor = false;
    private boolean sharingHealth = false;
    private boolean sharingExp = false;
    private boolean sharingHunger = false;
    private boolean sharingEffects = false;

    private HashMap<Player, WorldGroupPlayer> playerData = null;

    public Map<String, Object> serialize() {
        Map<String, Object> result = new LinkedHashMap<String, Object>();

        //result.put("world", this.getBlock().getWorld());

        return result;
    }

    public static WorldGroup deserialize(Map<String, Object> args) {

        return null;
    }

    public static List<WorldGroup> getGroupsForWorld(World world) {
        return MVIManager.getWorldGroups().get(world);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addWorld(World world) {
        worlds.add(world);
    }

    public HashSet<World> getWorlds() {
        return worlds;
    }

    public boolean isSharingInventory() {
        return sharingInventory;
    }

    public void setSharingInventory(boolean sharingInventory) {
        this.sharingInventory = sharingInventory;
    }

    public boolean isSharingArmor() {
        return sharingArmor;
    }

    public void setSharingArmor(boolean sharingArmor) {
        this.sharingArmor = sharingArmor;
    }

    public boolean isSharingHealth() {
        return sharingHealth;
    }

    public void setSharingHealth(boolean sharingHealth) {
        this.sharingHealth = sharingHealth;
    }

    public boolean isSharingExp() {
        return sharingExp;
    }

    public void setSharingExp(boolean sharingExp) {
        this.sharingExp = sharingExp;
    }

    public boolean isSharingHunger() {
        return sharingHunger;
    }

    public void setSharingHunger(boolean sharingHunger) {
        this.sharingHunger = sharingHunger;
    }

    public boolean isSharingEffects() {
        return sharingEffects;
    }

    public void setSharingEffects(boolean sharingEffects) {
        this.sharingEffects = sharingEffects;
    }

    public WorldGroupPlayer getPlayerData(Player player) {
        return this.playerData.get(player);
    }
}
