package com.onarandombox.multiverseinventories.api.profile;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

/**
 * Interface for a PlayerProfile which controls all the world specific data for a player.
 */
public interface PlayerProfile {

    /**
     * @return A map containing all the player data to be saved to disk.
     */
    Map<String, Object> serialize();

    /**
     * @return The type of player.
     */
    ProfileType getType();

    /**
     * @return the Player associated with this player.
     */
    OfflinePlayer getPlayer();

    /**
     * @return the Contents of the Profile's inventory.
     */
    ItemStack[] getInventoryContents();

    /**
     * Sets the inventory contents for this Profile.
     *
     * @param inventoryContents Inventory contents for this player.
     */
    void setInventoryContents(ItemStack[] inventoryContents);

    /**
     * @return the Contents of the Profile's armor.
     */
    ItemStack[] getArmorContents();

    /**
     * Sets the armor contents for this Profile.
     *
     * @param armorContents Armor contents for this player.
     */
    void setArmorContents(ItemStack[] armorContents);

    /**
     * @return The health of this Profile.
     */
    Integer getHealth();

    /**
     * Sets the health for this Profile.
     *
     * @param health New health for Profile.
     */
    void setHealth(Integer health);

    /**
     * @return The exp of this Profile.
     */
    Float getExp();

    /**
     * Sets the exp for this Profile.
     *
     * @param exp New exp for Profile.
     */
    void setExp(Float exp);

    /**
     * @return The total exp of this Profile.
     */
    Integer getTotalExperience();

    /**
     * Sets the total exp for this Profile.
     *
     * @param totalExperience exp New total exp for Profile.
     */
    void setTotalExperience(Integer totalExperience);

    /**
     * @return The level of this Profile.
     */
    Integer getLevel();

    /**
     * Sets the level for this Profile.
     *
     * @param level New level for Profile.
     */
    void setLevel(Integer level);

    /**
     * @return The food level of this Profile.
     */
    Integer getFoodLevel();

    /**
     * Sets the food level for this Profile.
     *
     * @param foodLevel New food level for Profile.
     */
    void setFoodLevel(Integer foodLevel);

    /**
     * @return The exhaustion of this Profile.
     */
    Float getExhaustion();

    /**
     * Sets the exhaustion for this Profile.
     *
     * @param exhaustion New exhaustion for Profile.
     */
    void setExhaustion(Float exhaustion);

    /**
     * @return The saturation of this Profile.
     */
    Float getSaturation();

    /**
     * Sets the saturation for this Profile.
     *
     * @param saturation New saturation for Profile.
     */
    void setSaturation(Float saturation);

    /**
     * @return The bed spawn location of this Profile.
     */
    Location getBedSpawnLocation();

    /**
     * Sets the bed spawn location for this Profile.
     *
     * @param location New bed spawn location for Profile.
     */
    void setBedSpawnLocation(Location location);
}

