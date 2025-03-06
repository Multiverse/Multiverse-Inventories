package org.mvplugins.multiverse.inventories.util;

import com.dumptruckman.minecraft.util.Logging;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Exists for backward compatibility with older versions of Multiverse-Inventories.
 */
@Deprecated
public final class LegacyParsers {

    private static final JSONParser JSON_PARSER = new JSONParser(
            JSONParser.USE_INTEGER_STORAGE | JSONParser.ACCEPT_TAILLING_SPACE);

    /**
     * @param locString Parses this string and creates Location.
     * @return New location object or null if no location could be created.
     * @deprecated Locations do not use special handling because they are
     * {@link org.bukkit.configuration.serialization.ConfigurationSerializable}.
     */
    @Deprecated
    public static Location parseLocation(String locString) {
        if (locString.isEmpty()) {
            return null;
        }
        JSONObject jsonLoc;
        try {
            jsonLoc = (JSONObject) JSON_PARSER.parse(locString);
        } catch (ParseException | ClassCastException e) {
            Logging.warning("Could not parse location! " + e.getMessage());
            return null;
        }
        return parseLocMap(jsonLoc);
    }

    /**
     * @deprecated Locations do not use special handling because they are
     * {@link org.bukkit.configuration.serialization.ConfigurationSerializable}.
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
        if (locMap.containsKey(DataStrings.LOCATION_WORLD)) {
            world = Bukkit.getWorld(locMap.get(DataStrings.LOCATION_WORLD).toString());
        }
        if (locMap.containsKey(DataStrings.LOCATION_X)) {
            Object value = locMap.get(DataStrings.LOCATION_X);
            if (value instanceof Number) {
                x = ((Number) value).doubleValue();
            }
        }
        if (locMap.containsKey(DataStrings.LOCATION_Y)) {
            Object value = locMap.get(DataStrings.LOCATION_Y);
            if (value instanceof Number) {
                y = ((Number) value).doubleValue();
            }
        }
        if (locMap.containsKey(DataStrings.LOCATION_Z)) {
            Object value = locMap.get(DataStrings.LOCATION_Z);
            if (value instanceof Number) {
                z = ((Number) value).doubleValue();
            }
        }
        if (locMap.containsKey(DataStrings.LOCATION_PITCH)) {
            Object value = locMap.get(DataStrings.LOCATION_PITCH);
            if (value instanceof Number) {
                pitch = ((Number) value).floatValue();
            }
        }
        if (locMap.containsKey(DataStrings.LOCATION_YAW)) {
            Object value = locMap.get(DataStrings.LOCATION_YAW);
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
     * @param potionsString A player's potion effects in string form to be parsed into
     *                      {@link java.util.Collection}&lt;{@link org.bukkit.potion.PotionEffect}&gt;.
     * @return a collection of potion effects parsed from potionsString.
     * @deprecated PotionEffect do not use special handling because they are
     * {@link org.bukkit.configuration.serialization.ConfigurationSerializable}.
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
                if (jsonPotion.containsKey(DataStrings.POTION_TYPE)) {
                    Object value = jsonPotion.get(DataStrings.POTION_TYPE);
                    if (value instanceof Number) {
                        type = ((Number) value).intValue();
                    }
                }
                if (jsonPotion.containsKey(DataStrings.POTION_AMPLIFIER)) {
                    Object value = jsonPotion.get(DataStrings.POTION_AMPLIFIER);
                    if (value instanceof Number) {
                        amplifier = ((Number) value).intValue();
                    }
                }
                if (jsonPotion.containsKey(DataStrings.POTION_DURATION)) {
                    Object value = jsonPotion.get(DataStrings.POTION_DURATION);
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
        return potionEffectList.toArray(new PotionEffect[0]);
    }

    private LegacyParsers() {
        throw new IllegalStateException();
    }
}
