package com.onarandombox.multiverseinventories.data;

import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author dumptruckman
 */
public class MVIWorldGroup implements ConfigurationSerializable {

    private HashSet<World> worlds = new HashSet<World>();
    private boolean sharesInventory = false;
    private boolean sharesHealth = false;
    private boolean sharesExp = false;
    private boolean sharesHunger = false;
    private boolean sharesEffects = false;

    public Map<String, Object> serialize() {
        Map<String, Object> result = new LinkedHashMap<String, Object>();

        //result.put("world", this.getBlock().getWorld());

        return result;
    }

    public static MVIWorldGroup deserialize(Map<String, Object> args) {

        return null;
    }

    public void addWorld(World world) {
        worlds.add(world);
    }

    public HashSet<World> getWorlds() {
        return worlds;
    }

    public boolean isSharesInventory() {
        return sharesInventory;
    }

    public void setSharesInventory(boolean sharesInventory) {
        this.sharesInventory = sharesInventory;
    }

    public boolean isSharesHealth() {
        return sharesHealth;
    }

    public void setSharesHealth(boolean sharesHealth) {
        this.sharesHealth = sharesHealth;
    }

    public boolean isSharesExp() {
        return sharesExp;
    }

    public void setSharesExp(boolean sharesExp) {
        this.sharesExp = sharesExp;
    }

    public boolean isSharesHunger() {
        return sharesHunger;
    }

    public void setSharesHunger(boolean sharesHunger) {
        this.sharesHunger = sharesHunger;
    }

    public boolean isSharesEffects() {
        return sharesEffects;
    }

    public void setSharesEffects(boolean sharesEffects) {
        this.sharesEffects = sharesEffects;
    }
}
