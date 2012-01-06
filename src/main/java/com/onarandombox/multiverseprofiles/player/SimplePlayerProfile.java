package com.onarandombox.multiverseprofiles.player;

import com.onarandombox.multiverseprofiles.inventory.ItemWrapper;
import com.onarandombox.multiverseprofiles.util.MinecraftTools;
import com.onarandombox.multiverseprofiles.util.ProfilesLog;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedHashMap;
import java.util.Map;

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
    private Float saturation = 0F;

    private OfflinePlayer player;

    public SimplePlayerProfile(OfflinePlayer player) {
        this.player = player;
    }

    public Map<String, Object> serialize() {
        Map<String, Object> result = new LinkedHashMap<String, Object>();

        result.put("player", this.getPlayer().getName());
        result.put("health", this.getHealth());
        result.put("exp", this.getExp());
        result.put("level", this.getLevel());
        result.put("foodLevel", this.getFoodLevel());
        result.put("exhaustion", this.getExhaustion());
        result.put("saturation", this.getSaturation());
        
        ConfigurationSection inventory = new MemoryConfiguration();
        for (Integer i = 0; i < 36; i++) {
            if (this.getInventoryContents()[i] != null && this.getInventoryContents()[i].getTypeId() != 0) {
                inventory.set(i.toString(), new ItemWrapper(this.getInventoryContents()[i]));
            }
        }
        result.put("inventoryContents", inventory);

        ConfigurationSection armor = new MemoryConfiguration();
        for (Integer i = 0; i < 4; i++) {
            if (this.getArmorContents()[i] != null && this.getArmorContents()[i].getTypeId() != 0) {
                armor.set(i.toString(), new ItemWrapper(this.getArmorContents()[i]));
            }
        }
        result.put("armorContents", armor);

        return result;
    }

    public static PlayerProfile deserialize(Map<String, Object> args) {
        PlayerProfile playerProfile = null;

        Object object = args.get("player");
        if (object != null && object instanceof String) {
            playerProfile = new SimplePlayerProfile(Bukkit.getOfflinePlayer(object.toString()));
            try {
                playerProfile.setHealth((Integer) args.get("health"));
                try {
                    playerProfile.setExp(Float.valueOf(args.get("exp").toString()));
                } catch (NumberFormatException ignore) {}
                playerProfile.setLevel((Integer) args.get("level"));
                playerProfile.setFoodLevel((Integer) args.get("foodLevel"));
                try {
                    playerProfile.setExhaustion(Float.valueOf(args.get("exhaustion").toString()));
                } catch (NumberFormatException ignore) {}
                try {
                    playerProfile.setSaturation(Float.valueOf(args.get("saturation").toString()));
                } catch (NumberFormatException ignore) {}

                object = args.get("inventoryContents");
                ItemStack[] inventoryContents = MinecraftTools.fillWithAir(new ItemStack[36]);
                if (object != null && object instanceof Map) {
                    Map itemsMap = (Map) object;
                    for (Object itemSlot : itemsMap.keySet()) {
                        try {
                            int index = Integer.valueOf(itemSlot.toString());
                            Object itemObject = itemsMap.get(itemSlot);
                            if (itemObject instanceof Map) {
                                inventoryContents[index] = ItemWrapper.deserialize((Map)itemObject).getItem();
                            }
                        } catch (NumberFormatException ignore) {}
                    }
                }
                playerProfile.setInventoryContents(inventoryContents);

                object = args.get("armorContents");
                ItemStack[] armorContents = MinecraftTools.fillWithAir(new ItemStack[4]);
                if (object != null && object instanceof Map) {
                    Map itemsMap = (Map) object;
                    for (Object obj : itemsMap.keySet()) {
                        if (obj instanceof Integer) {
                            int index = (Integer) obj;
                            Object itemObject = itemsMap.get(obj);
                            if (itemObject instanceof ItemWrapper) {
                                armorContents[index] = ((ItemWrapper) itemObject).getItem();
                            }
                        }
                    }
                }
                playerProfile.setArmorContents(armorContents);
            } catch (ClassCastException e) {
                ProfilesLog.severe("Could not load data for player: " + playerProfile.getPlayer().getName());
                ProfilesLog.severe("Data was not formatted correctly!");
                return null;
            }
        }

        return playerProfile;
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
