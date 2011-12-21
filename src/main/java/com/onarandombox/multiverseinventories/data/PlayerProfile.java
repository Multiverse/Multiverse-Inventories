package com.onarandombox.multiverseinventories.data;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author dumptruckman
 */
public class PlayerProfile implements ConfigurationSerializable {

    private ItemStack[] inventoryContents = null;
    private ItemStack[] armorContents = null;
    private Integer health = null;
    private Integer exp = null;
    private Integer level = null;
    private Integer foodLevel = null;
    private Float exhaustion = null;
    private Float saturation = null;

    public Map<String, Object> serialize() {
        Map<String, Object> result = new LinkedHashMap<String, Object>();

        //result.put("world", this.getBlock().getWorld());
        return result;
    }

    public static PlayerProfile deserialize(Map<String, Object> args) {
        Player player;
        return null;
    }

    public Inventory[] getInventoryContents() {

        return null;
    }

    public ItemStack[] getArmorContents() {
        return armorContents;
    }

    public void setArmorContents(ItemStack[] armorContents) {
        this.armorContents = armorContents;
    }

    public Integer getHealth() {
        return health;
    }

    public void setHealth(Integer health) {
        this.health = health;
    }

    public Integer getExp() {
        return exp;
    }

    public void setExp(Integer exp) {
        this.exp = exp;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Integer getFoodLevel() {
        return foodLevel;
    }

    public void setFoodLevel(Integer foodLevel) {
        this.foodLevel = foodLevel;
    }

    public Float getExhaustion() {
        return exhaustion;
    }

    public void setExhaustion(Float exhaustion) {
        this.exhaustion = exhaustion;
    }

    public Float getSaturation() {
        return saturation;
    }

    public void setSaturation(Float saturation) {
        this.saturation = saturation;
    }
}
