package com.onarandombox.multiverseinventories.profile;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

public interface PlayerProfile {

    public void serialize(ConfigurationSection playerData);

    public OfflinePlayer getPlayer();

    public ItemStack[] getInventoryContents();

    public void setInventoryContents(ItemStack[] inventoryContents);

    public ItemStack[] getArmorContents();

    public void setArmorContents(ItemStack[] armorContents);

    public Integer getHealth();

    public void setHealth(Integer health);

    public Float getExp();

    public void setExp(Float exp);

    public Integer getLevel();

    public void setLevel(Integer level);

    public Integer getFoodLevel();

    public void setFoodLevel(Integer foodLevel);

    public Float getExhaustion();

    public void setExhaustion(Float exhaustion);

    public Float getSaturation();

    public void setSaturation(Float saturation);
}
