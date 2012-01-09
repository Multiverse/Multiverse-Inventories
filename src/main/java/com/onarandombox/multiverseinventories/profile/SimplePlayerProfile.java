package com.onarandombox.multiverseinventories.profile;

import com.onarandombox.multiverseinventories.data.DataStrings;
import com.onarandombox.multiverseinventories.item.ItemWrapper;
import com.onarandombox.multiverseinventories.item.SimpleItemWrapper;
import com.onarandombox.multiverseinventories.util.MILog;
import com.onarandombox.multiverseinventories.util.MinecraftTools;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

/**
 * @author dumptruckman
 */
public class SimplePlayerProfile implements PlayerProfile {

    private ItemStack[] inventoryContents = new ItemStack[36];
    private ItemStack[] armorContents = new ItemStack[4];
    private Integer health = 20;
    private Float exp = 0F;
    private Integer level = 0;
    private Integer foodLevel = 20;
    private Float exhaustion = 0F;
    private Float saturation = 5F;

    private OfflinePlayer player;

    public SimplePlayerProfile(OfflinePlayer player) {
        this.player = player;
    }

    public SimplePlayerProfile(String playerName, ConfigurationSection playerSection) {
        this(Bukkit.getOfflinePlayer(playerName));

        this.parsePlayerStats(playerSection.getString("stats").split(DataStrings.GENERAL_DELIMITER));
        this.parsePlayerInventory(playerSection.getString("inventoryContents").split(DataStrings.ITEM_DELIMITER));
        this.parsePlayerArmor(playerSection.getString("armorContents").split(DataStrings.ITEM_DELIMITER));
    }

    private void parsePlayerStats(String[] statsArray) {
        for (String stat : statsArray) {
            try {
                String[] statValues = DataStrings.splitValue(stat);
                if (statValues[0].equals(DataStrings.PLAYER_HEALTH)) {
                    this.setHealth(Integer.valueOf(statValues[1]));
                } else if (statValues[0].equals(DataStrings.PLAYER_EXPERIENCE)) {
                    this.setExp(Float.valueOf(statValues[1]));
                } else if (statValues[0].equals(DataStrings.PLAYER_LEVEL)) {
                    this.setLevel(Integer.valueOf(statValues[1]));
                } else if (statValues[0].equals(DataStrings.PLAYER_FOOD_LEVEL)) {
                    this.setFoodLevel(Integer.valueOf(statValues[1]));
                } else if (statValues[0].equals(DataStrings.PLAYER_EXHAUSTION)) {
                    this.setExhaustion(Float.valueOf(statValues[1]));
                } else if (statValues[0].equals(DataStrings.PLAYER_SATURATION)) {
                    this.setSaturation(Float.valueOf(statValues[1]));
                }
            } catch (Exception e) {
                if (!stat.isEmpty()) {
                    MILog.debug("Could not parse stat: '" + stat + "'");
                    MILog.debug(e.getMessage());
                }
            }
        }
    }

    private void parsePlayerInventory(String[] inventoryArray) {
        ItemStack[] inventoryContents = MinecraftTools.fillWithAir(new ItemStack[36]);
        for (String itemString : inventoryArray) {
            String[] itemValues = DataStrings.splitValue(itemString);
            try {
                MILog.debug("Unwrapping item from string: " + itemString);
                ItemWrapper itemWrapper = new SimpleItemWrapper(itemValues[1]);
                MILog.debug("Unwrapped item: " + itemWrapper.getItem().toString());
                inventoryContents[Integer.valueOf(itemValues[0])] = itemWrapper.getItem();
            } catch (Exception e) {
                if (!itemString.isEmpty()) {
                    MILog.debug("Could not parse item string: " + itemString);
                    MILog.debug(e.getMessage());
                }
            }
        }
        this.setInventoryContents(inventoryContents);
    }

    private void parsePlayerArmor(String[] armorArray) {
        ItemStack[] armorContents = MinecraftTools.fillWithAir(new ItemStack[4]);
        for (String itemString : armorArray) {
            String[] itemValues = DataStrings.splitValue(itemString);
            try {
                armorContents[Integer.valueOf(itemValues[0])] = new SimpleItemWrapper(itemValues[1]).getItem();
            } catch (Exception e) {
                if (!itemString.isEmpty()) {
                    MILog.debug("Could not parse armor string: " + itemString);
                    MILog.debug(e.getMessage());
                }
            }
        }
        this.setArmorContents(armorContents);
    }

    public void serialize(ConfigurationSection playerData) {
        StringBuilder builder = new StringBuilder();

        builder.append(DataStrings.createEntry(DataStrings.PLAYER_HEALTH, this.getHealth()));
        builder.append(DataStrings.GENERAL_DELIMITER);
        builder.append(DataStrings.createEntry(DataStrings.PLAYER_EXPERIENCE, this.getExp()));
        builder.append(DataStrings.GENERAL_DELIMITER);
        builder.append(DataStrings.createEntry(DataStrings.PLAYER_LEVEL, this.getLevel()));
        builder.append(DataStrings.GENERAL_DELIMITER);
        builder.append(DataStrings.createEntry(DataStrings.PLAYER_FOOD_LEVEL, this.getFoodLevel()));
        builder.append(DataStrings.GENERAL_DELIMITER);
        builder.append(DataStrings.createEntry(DataStrings.PLAYER_EXHAUSTION, this.getExhaustion()));
        builder.append(DataStrings.GENERAL_DELIMITER);
        builder.append(DataStrings.createEntry(DataStrings.PLAYER_SATURATION, this.getSaturation()));

        playerData.set("stats", builder.toString());

        builder = new StringBuilder();
        boolean first = true;
        for (Integer i = 0; i < 36; i++) {
            if (this.getInventoryContents()[i] != null && this.getInventoryContents()[i].getTypeId() != 0) {
                if (first) {
                    first = false;
                } else {
                    builder.append(DataStrings.ITEM_DELIMITER);
                }
                builder.append(DataStrings.createEntry(i, new SimpleItemWrapper(this.getInventoryContents()[i]).toString()));
            }
        }
        playerData.set("inventoryContents", builder.toString());

        builder = new StringBuilder();
        first = true;
        for (Integer i = 0; i < 4; i++) {
            if (this.getArmorContents()[i] != null && this.getArmorContents()[i].getTypeId() != 0) {
                if (first) {
                    first = false;
                } else {
                    builder.append(DataStrings.ITEM_DELIMITER);
                }
                builder.append(DataStrings.createEntry(i, new SimpleItemWrapper(this.getArmorContents()[i]).toString()));
            }
        }
        playerData.set("armorContents", builder.toString());
    }

    public OfflinePlayer getPlayer() {
        return this.player;
    }

    public ItemStack[] getInventoryContents() {
        return this.inventoryContents;
    }

    public void setInventoryContents(ItemStack[] inventoryContents) {
        this.inventoryContents = inventoryContents;
    }

    public ItemStack[] getArmorContents() {
        return this.armorContents;
    }

    public void setArmorContents(ItemStack[] armorContents) {
        this.armorContents = armorContents;
    }

    public Integer getHealth() {
        return this.health;
    }

    public void setHealth(Integer health) {
        this.health = health;
    }

    public Float getExp() {
        return this.exp;
    }

    public void setExp(Float exp) {
        this.exp = exp;
    }

    public Integer getLevel() {
        return this.level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Integer getFoodLevel() {
        return this.foodLevel;
    }

    public void setFoodLevel(Integer foodLevel) {
        this.foodLevel = foodLevel;
    }

    public Float getExhaustion() {
        return this.exhaustion;
    }

    public void setExhaustion(Float exhaustion) {
        this.exhaustion = exhaustion;
    }

    public Float getSaturation() {
        return this.saturation;
    }

    public void setSaturation(Float saturation) {
        this.saturation = saturation;
    }
}
