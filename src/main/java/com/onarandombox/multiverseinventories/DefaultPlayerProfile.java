package com.onarandombox.multiverseinventories;

import com.onarandombox.multiverseinventories.api.profile.ProfileType;
import com.onarandombox.multiverseinventories.api.DataStrings;
import com.onarandombox.multiverseinventories.api.profile.PlayerProfile;
import com.onarandombox.multiverseinventories.api.PlayerStats;
import com.onarandombox.multiverseinventories.share.Sharable;
import com.onarandombox.multiverseinventories.share.Sharables;
import com.onarandombox.multiverseinventories.util.ItemWrapper;
import com.onarandombox.multiverseinventories.util.Logging;
import com.onarandombox.multiverseinventories.util.MinecraftTools;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The default implementation of PlayerProfile.
 */
class DefaultPlayerProfile implements PlayerProfile {

    private ItemStack[] inventoryContents = new ItemStack[PlayerStats.INVENTORY_SIZE];
    private ItemStack[] armorContents = new ItemStack[PlayerStats.ARMOR_SIZE];
    private int health = PlayerStats.HEALTH;
    private float exp = PlayerStats.EXPERIENCE;
    private int totalExperience = PlayerStats.TOTAL_EXPERIENCE;
    private int level = PlayerStats.LEVEL;
    private int foodLevel = PlayerStats.FOOD_LEVEL;
    private float exhaustion = PlayerStats.EXHAUSTION;
    private float saturation = PlayerStats.SATURATION;
    private Location bedSpawnLocation = null;
    private float fallDistance = PlayerStats.FALL_DISTANCE;
    private int fireTicks = PlayerStats.FIRE_TICKS;
    private int remainingAir = PlayerStats.REMAINING_AIR;
    private int maximumAir = PlayerStats.MAXIMUM_AIR;

    private OfflinePlayer player;
    private ProfileType type;

    public DefaultPlayerProfile(ProfileType type, OfflinePlayer player) {
        this.type = type;
        this.player = player;
    }

    public DefaultPlayerProfile(ProfileType type, String playerName, Map<String, Object> playerData) {
        this(type, Bukkit.getOfflinePlayer(playerName));
        for (Sharable sharable : Sharables.all()) {
            sharable.addToProfile(playerData, this);
        }
        Logging.finer("Created player profile from map for '" + playerName + "'.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> playerData = new LinkedHashMap<String, Object>();
        for (Sharable sharable : Sharables.all()) {
            sharable.addToMap(this, playerData);
        }
        return playerData;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProfileType getType() {
        return this.type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OfflinePlayer getPlayer() {
        return this.player;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ItemStack[] getInventoryContents() {
        return this.inventoryContents;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setInventoryContents(ItemStack[] inventoryContents) {
        this.inventoryContents = inventoryContents;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ItemStack[] getArmorContents() {
        return this.armorContents;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setArmorContents(ItemStack[] armorContents) {
        this.armorContents = armorContents;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getHealth() {
        return this.health;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setHealth(int health) {
        this.health = health;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Float getExp() {
        return this.exp;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setExp(float exp) {
        this.exp = exp;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getTotalExperience() {
        return this.totalExperience;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTotalExperience(int totalExperience) {
        this.totalExperience = totalExperience;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getLevel() {
        return this.level;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLevel(int level) {
        this.level = level;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getFoodLevel() {
        return this.foodLevel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFoodLevel(int foodLevel) {
        this.foodLevel = foodLevel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Float getExhaustion() {
        return this.exhaustion;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setExhaustion(float exhaustion) {
        this.exhaustion = exhaustion;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Float getSaturation() {
        return this.saturation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSaturation(float saturation) {
        this.saturation = saturation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Location getBedSpawnLocation() {
        return this.bedSpawnLocation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setBedSpawnLocation(Location location) {
        this.bedSpawnLocation = location;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Float getFallDistance() {
        return this.fallDistance;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFallDistance(float fallDistance) {
        this.fallDistance = fallDistance;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getFireTicks() {
        return this.fireTicks;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFireTicks(int fireTicks) {
        this.fireTicks = fireTicks;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getRemainingAir() {
        return this.remainingAir;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setRemainingAir(int remainingAir) {
        this.remainingAir = remainingAir;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getMaximumAir() {
        return this.maximumAir;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setMaximumAir(int maximumAir) {
        this.maximumAir = maximumAir;
    }
}

