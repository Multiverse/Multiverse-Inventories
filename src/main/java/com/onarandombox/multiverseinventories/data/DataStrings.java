package com.onarandombox.multiverseinventories.data;

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
}

