package com.onarandombox.multiverseinventories.api;

import com.onarandombox.multiverseinventories.util.Logging;
import com.onarandombox.multiverseinventories.util.MinecraftTools;
import com.onarandombox.multiverseinventories.util.data.ItemWrapper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

/**
 * This class handles the formatting of strings for data i/o.
 */
public class DataStrings {

    /**
     * General delimiter to separate data items.
     */
    public static final String GENERAL_DELIMITER = ";";
    /**
     * Secondary delimiter to separate data items where general delimiter is used in a broader purpose.
     */
    public static final String SECONDARY_DELIMITER = ",";
    /**
     * Special delimiter to separate items since they use both the general and secondary delimiters already.
     */
    public static final String ITEM_DELIMITER = "/";
    /**
     * Delimiter to separate a key and it's value.
     */
    public static final String VALUE_DELIMITER = ":";
    /**
     * Item type identifier.
     */
    public static final String ITEM_TYPE_ID = "t";
    /**
     * Item durability identifier.
     */
    public static final String ITEM_DURABILITY = "d";
    /**
     * Item amount identifier.
     */
    public static final String ITEM_AMOUNT = "#";
    /**
     * Item enchantments identifier.
     */
    public static final String ITEM_ENCHANTS = "e";
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
     * Player bed spawn location identifier.
     */
    public static final String PLAYER_BED_SPAWN_LOCATION = "bedSpawnLocation";
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
     * Player exp identifier.
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
     * Player saturation identifier.
     */
    public static final String PLAYER_FALL_DISTANCE = "fd";
    /**
     * Player saturation identifier.
     */
    public static final String PLAYER_FIRE_TICKS = "ft";
    /**
     * Player saturation identifier.
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

    private DataStrings() {
        throw new AssertionError();
    }

    /**
     * Splits a key:value string into a String[2] where string[0] == key and string[1] == value.
     *
     * @param valueString A key:value string.
     * @return A string array split on the {@link #VALUE_DELIMITER}.
     */
    public static String[] splitEntry(String valueString) {
        return valueString.split(VALUE_DELIMITER, 2);
    }

    /**
     * Creates a key:value string from the string form of the key object and value object.
     *
     * @param key   Object that is to be the key.
     * @param value Object that is to be the value.
     * @return String of key and value joined with the {@link #VALUE_DELIMITER}.
     */
    public static String createEntry(Object key, Object value) {
        return key + VALUE_DELIMITER + value;
    }



    /**
     * @param locString Parses this string and creates Location.
     * @return New location object or null if no location could be created.
     */
    public static Location parseLocation(String locString) {
        String[] locArray = locString.split(DataStrings.GENERAL_DELIMITER);
        World world = null;
        double x = 0;
        double y = 0;
        double z = 0;
        float pitch = 0;
        float yaw = 0;
        try {
            for (String stat : locArray) {
                String[] statValues = DataStrings.splitEntry(stat);
                if (statValues[0].equals(DataStrings.LOCATION_X)) {
                    x = Double.valueOf(statValues[1]);
                } else if (statValues[0].equals(DataStrings.LOCATION_Y)) {
                    y = Double.valueOf(statValues[1]);
                } else if (statValues[0].equals(DataStrings.LOCATION_Z)) {
                    z = Double.valueOf(statValues[1]);
                } else if (statValues[0].equals(DataStrings.LOCATION_WORLD)) {
                    world = Bukkit.getWorld(statValues[1]);
                } else if (statValues[0].equals(DataStrings.LOCATION_PITCH)) {
                    yaw = Float.valueOf(statValues[1]);
                } else if (statValues[0].equals(DataStrings.LOCATION_YAW)) {
                    pitch = Float.valueOf(statValues[1]);
                }
            }
        } catch (Exception e) {
            Logging.fine("Could not parse location: " + locString);
            return null;
        }
        if (world == null) {
            return null;
        }
        return new Location(world, x, y, z, yaw, pitch);
    }

    /**
     * @param inventoryString An inventory in string for to be parsed into an ItemStack array.
     * @param inventorySize The number of item slots in the inventory.
     * @return an ItemStack array containing the inventory contents parsed from inventoryString.
     */
    public static ItemStack[] parseInventory(String inventoryString, int inventorySize) {
        String[] inventoryArray = inventoryString.split(DataStrings.ITEM_DELIMITER);
        ItemStack[] invContents = MinecraftTools.fillWithAir(new ItemStack[inventorySize]);
        for (String itemString : inventoryArray) {
            String[] itemValues = DataStrings.splitEntry(itemString);
            try {
                ItemWrapper itemWrapper = ItemWrapper.wrap(itemValues[1]);
                invContents[Integer.valueOf(itemValues[0])] = itemWrapper.getItem();
                //Logging.debug("ItemString '" + itemString + "' unwrapped as: " + itemWrapper.getItem().toString());
            } catch (Exception e) {
                if (!itemString.isEmpty()) {
                    Logging.fine("Could not parse item string: " + itemString);
                    Logging.fine(e.getMessage());
                }
            }
        }
        return invContents;
    }

    /**
     * Converts an ItemStack array into a String for easy persistence.
     *
     * @param items The items you wish to "string-i-tize".
     * @return A string representation of an inventory.
     */
    public static String valueOf(ItemStack[] items) {
        StringBuilder builder = new StringBuilder();
        for (Integer i = 0; i < items.length; i++) {
            if (items[i] != null && items[i].getTypeId() != 0) {
                if (!builder.toString().isEmpty()) {
                    builder.append(DataStrings.ITEM_DELIMITER);
                }
                builder.append(DataStrings.createEntry(i, ItemWrapper.wrap(items[i]).toString()));
            }
        }
        return builder.toString();
    }

    /**
     * Converts a {@link Location} into a String for easy persistence.
     *
     * @param location The location you wish to "string-i-tize".
     * @return A String representation of a {@link Location}
     */
    public static String valueOf(Location location) {
        StringBuilder builder = new StringBuilder();
        builder.append(DataStrings.createEntry(DataStrings.LOCATION_WORLD, location.getWorld().getName()));
        builder.append(DataStrings.GENERAL_DELIMITER);
        builder.append(DataStrings.createEntry(DataStrings.LOCATION_X, location.getX()));
        builder.append(DataStrings.GENERAL_DELIMITER);
        builder.append(DataStrings.createEntry(DataStrings.LOCATION_Y, location.getY()));
        builder.append(DataStrings.GENERAL_DELIMITER);
        builder.append(DataStrings.createEntry(DataStrings.LOCATION_Z, location.getZ()));
        builder.append(DataStrings.GENERAL_DELIMITER);
        builder.append(DataStrings.createEntry(DataStrings.LOCATION_PITCH, location.getPitch()));
        builder.append(DataStrings.GENERAL_DELIMITER);
        builder.append(DataStrings.createEntry(DataStrings.LOCATION_YAW, location.getYaw()));
        return builder.toString();
    }
}

