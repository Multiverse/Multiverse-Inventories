package com.onarandombox.multiverseinventories;

import com.dumptruckman.minecraft.util.Logging;
import com.onarandombox.multiverseinventories.util.MinecraftTools;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This class handles the formatting of strings for data i/o.
 */
public class DataStrings {

    /**
     * Delimiter to separate a key and it's value.
     */
    public static final String VALUE_DELIMITER = ":";
    /**
     * Player stats identifier.
     */
    public static final String PLAYER_STATS = "stats";
    /**
     * Player inventory contents identifier.
     */
    public static final String PLAYER_INVENTORY_CONTENTS = "inventoryContents";
    /**
     * Player armor contents identifier.
     */
    public static final String PLAYER_ARMOR_CONTENTS = "armorContents";
    /**
     * Player armor contents identifier.
     */
    public static final String PLAYER_OFF_HAND_ITEM = "offHandItem";
    /**
     * Ender chest inventory contents identifier.
     */
    public static final String ENDER_CHEST_CONTENTS = "enderChestContents";
    /**
     * Player bed spawn location identifier.
     */
    public static final String PLAYER_BED_SPAWN_LOCATION = "bedSpawnLocation";
    /**
     * Player bed spawn location identifier.
     */
    public static final String PLAYER_LAST_LOCATION = "lastLocation";
    /**
     * Player last world identifier.
     */
    public static final String PLAYER_LAST_WORLD = "lastWorld";
    /**
     * Player should load identifier.
     */
    public static final String PLAYER_SHOULD_LOAD = "shouldLoad";
    /**
     * Player bed spawn location identifier.
     */
    public static final String PLAYER_PROFILE_TYPE = "profileType";
    /**
     * Player health identifier.
     */
    public static final String PLAYER_HEALTH = "hp";
    /**
     * Player exp identifier.
     */
    public static final String PLAYER_EXPERIENCE = "xp";
    /**
     * Player total exp identifier.
     */
    public static final String PLAYER_TOTAL_EXPERIENCE = "txp";
    /**
     * Player exp level identifier.
     */
    public static final String PLAYER_LEVEL = "el";
    /**
     * Player food level identifier.
     */
    public static final String PLAYER_FOOD_LEVEL = "fl";
    /**
     * Player exhaustion identifier.
     */
    public static final String PLAYER_EXHAUSTION = "ex";
    /**
     * Player saturation identifier.
     */
    public static final String PLAYER_SATURATION = "sa";
    /**
     * Player fall distance identifier.
     */
    public static final String PLAYER_FALL_DISTANCE = "fd";
    /**
     * Player fire ticks identifier.
     */
    public static final String PLAYER_FIRE_TICKS = "ft";
    /**
     * Player remaining air identifier.
     */
    public static final String PLAYER_REMAINING_AIR = "ra";
    /**
     * Player saturation identifier.
     */
    public static final String PLAYER_MAX_AIR = "ma";
    /**
     * Location x identifier.
     */
    public static final String LOCATION_X = "x";
    /**
     * Location y identifier.
     */
    public static final String LOCATION_Y = "y";
    /**
     * Location z identifier.
     */
    public static final String LOCATION_Z = "z";
    /**
     * Location world identifier.
     */
    public static final String LOCATION_WORLD = "wo";
    /**
     * Location pitch identifier.
     */
    public static final String LOCATION_PITCH = "pi";
    /**
     * Location yaw identifier.
     */
    public static final String LOCATION_YAW = "ya";
    /**
     * Potion type identifier.
     */
    public static final String POTION_TYPE = "pt";
    /**
     * Potion duration identifier.
     */
    public static final String POTION_DURATION = "pd";
    /**
     * Potion amplifier identifier.
     */
    public static final String POTION_AMPLIFIER = "pa";

    private DataStrings() {
        throw new AssertionError();
    }

    /**
     * @param locString Parses this string and creates Location.
     * @return New location object or null if no location could be created.
     * @deprecated Locations do not use special handling because they are {@link org.bukkit.configuration.serialization.ConfigurationSerializable}
     */
    @Deprecated
    public static Location parseLocation(String locString) {
        if (locString.isEmpty()) {
            return null;
        }
        JSONObject jsonLoc;
        try {
            jsonLoc = (JSONObject) JSON_PARSER.parse(locString);
        } catch (ParseException e) {
            Logging.warning("Could not parse location! " + e.getMessage());
            return null;
        } catch (ClassCastException e) {
            Logging.warning("Could not parse location! " + e.getMessage());
            return null;
        }
        return parseLocMap(jsonLoc);
    }

    /**
     * @deprecated Locations do not use special handling because they are {@link org.bukkit.configuration.serialization.ConfigurationSerializable}
     */
    @Deprecated
    public static Location parseLocation(Map locMap) {
        return parseLocMap(locMap);
    }

    @Deprecated
    private static Location parseLocMap(Map locMap) {
        World world = null;
        double x = 0;
        double y = 0;
        double z = 0;
        float pitch = 0;
        float yaw = 0;
        if (locMap.containsKey(LOCATION_WORLD)) {
            world = Bukkit.getWorld(locMap.get(LOCATION_WORLD).toString());
        }
        if (locMap.containsKey(LOCATION_X)) {
            Object value = locMap.get(LOCATION_X);
            if (value instanceof Number) {
                x = ((Number) value).doubleValue();
            }
        }
        if (locMap.containsKey(LOCATION_Y)) {
            Object value = locMap.get(LOCATION_Y);
            if (value instanceof Number) {
                y = ((Number) value).doubleValue();
            }
        }
        if (locMap.containsKey(LOCATION_Z)) {
            Object value = locMap.get(LOCATION_Z);
            if (value instanceof Number) {
                z = ((Number) value).doubleValue();
            }
        }
        if (locMap.containsKey(LOCATION_PITCH)) {
            Object value = locMap.get(LOCATION_PITCH);
            if (value instanceof Number) {
                pitch = ((Number) value).floatValue();
            }
        }
        if (locMap.containsKey(LOCATION_YAW)) {
            Object value = locMap.get(LOCATION_YAW);
            if (value instanceof Number) {
                yaw = ((Number) value).floatValue();
            }
        }
        if (world == null) {
            return null;
        }
        return new Location(world, x, y, z, yaw, pitch);
    }

    /**
     * @param potionsString A player's potion effects in string form to be parsed into Collection<PotionEffect>.
     * @return a collection of potion effects parsed from potionsString.
     * @deprecated PotionEffect do not use special handling because they are {@link org.bukkit.configuration.serialization.ConfigurationSerializable}
     */
    @Deprecated
    public static PotionEffect[] parsePotionEffects(String potionsString) {
        List<PotionEffect> potionEffectList = new LinkedList<PotionEffect>();
        if (potionsString.isEmpty()) {
            return potionEffectList.toArray(new PotionEffect[potionEffectList.size()]);
        }
        JSONArray jsonPotions;
        try {
            jsonPotions = (JSONArray) JSON_PARSER.parse(potionsString);
        } catch (ParseException e) {
            Logging.warning("Could not parse potions! " + e.getMessage());
            return potionEffectList.toArray(new PotionEffect[potionEffectList.size()]);
        } catch (ClassCastException e) {
            Logging.warning("Could not parse potions! " + e.getMessage());
            return potionEffectList.toArray(new PotionEffect[potionEffectList.size()]);
        }
        for (Object obj : jsonPotions) {
            if (obj instanceof JSONObject) {
                JSONObject jsonPotion = (JSONObject) obj;
                int type = -1;
                int duration = -1;
                int amplifier = -1;
                if (jsonPotion.containsKey(POTION_TYPE)) {
                    Object value = jsonPotion.get(POTION_TYPE);
                    if (value instanceof Number) {
                        type = ((Number) value).intValue();
                    }
                }
                if (jsonPotion.containsKey(POTION_AMPLIFIER)) {
                    Object value = jsonPotion.get(POTION_AMPLIFIER);
                    if (value instanceof Number) {
                        amplifier = ((Number) value).intValue();
                    }
                }
                if (jsonPotion.containsKey(POTION_DURATION)) {
                    Object value = jsonPotion.get(POTION_DURATION);
                    if (value instanceof Number) {
                        duration = ((Number) value).intValue();
                    }
                }
                if (type == -1 || duration == -1 || amplifier == -1) {
                    Logging.fine("Could not parse potion effect string: " + obj);
                } else {
                    PotionEffectType pType = PotionEffectType.getById(type);
                    if (pType == null) {
                        Logging.warning("Could not parse potion effect type: " + type);
                        continue;
                    }
                    potionEffectList.add(new PotionEffect(pType, duration, amplifier));
                }
            } else {
                Logging.warning("Could not parse potion effect: " + obj);
            }
        }
        return potionEffectList.toArray(new PotionEffect[potionEffectList.size()]);
    }

    private static final JSONParser JSON_PARSER = new JSONParser(JSONParser.USE_INTEGER_STORAGE);
}

