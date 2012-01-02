package com.onarandombox.multiverseprofiles.data;

import com.onarandombox.multiverseprofiles.util.MinecraftTools;
import com.onarandombox.multiverseprofiles.util.ProfilesLog;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author dumptruckman
 */
public class PlayerProfile implements ConfigurationSerializable {

    private ItemStack[] inventoryContents = new ItemStack[36];
    private ItemStack[] armorContents = new ItemStack[4];
    private Integer health = 20;
    private Float exp = 0F;
    private Integer level = 0;
    private Integer foodLevel = 20;
    private Float exhaustion = 0F;
    private Float saturation = 0F;

    private OfflinePlayer player;

    public PlayerProfile(OfflinePlayer player) {
        this.player = player;
    }

    public Map<String, Object> serialize() {
        Map<String, Object> result = new LinkedHashMap<String, Object>();

        result.put("player", this.getPlayer());
        result.put("health", this.getHealth());
        result.put("exp", this.getExp());
        result.put("level", this.getLevel());
        result.put("foodLevel", this.getFoodLevel());
        result.put("exhaustion", this.getExhaustion());
        result.put("saturation", this.getSaturation());

        Map<Integer, ItemStack> inventoryMap = new HashMap<Integer, ItemStack>(36);
        for (int i = 0; i < 36; i++) {
            if (this.getInventoryContents()[i] == null) {
                this.getInventoryContents()[i] = new ItemStack(0);
            }
            inventoryMap.put(i, this.getInventoryContents()[i]);
        }
        result.put("inventoryContents", inventoryMap);

        Map<Integer, ItemStack> armorMap = new HashMap<Integer, ItemStack>(4);
        for (int i = 0; i < 4; i++) {
            if (this.getArmorContents()[i] == null) {
                this.getArmorContents()[i] = new ItemStack(0);
            }
            armorMap.put(i, this.getArmorContents()[i]);
        }
        result.put("armorContents", armorMap);

        return result;
    }

    public static PlayerProfile deserialize(Map<String, Object> args) {
        PlayerProfile playerProfile = null;

        Object object = args.get("player");
        if (object != null && object instanceof OfflinePlayer) {
            playerProfile = new PlayerProfile((OfflinePlayer) object);
            try {
                playerProfile.setHealth((Integer) args.get("health"));
                playerProfile.setExp((Float) args.get("exp"));
                playerProfile.setLevel((Integer) args.get("level"));
                playerProfile.setFoodLevel((Integer) args.get("foodLevel"));
                playerProfile.setExhaustion((Float) args.get("exhaustion"));
                playerProfile.setSaturation((Float) args.get("saturation"));

                object = args.get("inventoryContents");
                ItemStack[] inventoryContents = MinecraftTools.fillWithAir(new ItemStack[36]);
                if (object != null && object instanceof Map) {
                    Map itemsMap = (Map) object;
                    for (Object obj : itemsMap.keySet()) {
                        if (obj instanceof Integer) {
                            int index = (Integer) obj;
                            Object itemObject = itemsMap.get(obj);
                            if (itemObject instanceof ItemStack) {
                                inventoryContents[index] = (ItemStack) itemObject;
                            }
                        }
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
                            if (itemObject instanceof ItemStack) {
                                armorContents[index] = (ItemStack) itemObject;
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
