package com.onarandombox.multiverseinventories.data;

/**
 * @author dumptruckman
 */
public class DataStrings {

    public static final String GENERAL_DELIMITER = ";";
    public static final String SECONDARY_DELIMITER = ",";
    public static final String ITEM_DELIMITER = "/";
    public static final String VALUE_DELIMITER = ":";

    public static final String ITEM_TYPE_ID = "t";
    public static final String ITEM_DURABILITY = "d";
    public static final String ITEM_AMOUNT = "#";
    public static final String ITEM_ENCHANTS = "e";
    public static final String PLAYER_HEALTH = "hp";
    public static final String PLAYER_EXPERIENCE = "xp";
    public static final String PLAYER_TOTAL_EXPERIENCE = "txp";
    public static final String PLAYER_LEVEL = "el";
    public static final String PLAYER_FOOD_LEVEL = "fl";
    public static final String PLAYER_EXHAUSTION = "ex";
    public static final String PLAYER_SATURATION = "sa";

    public static String[] splitValue(String valueString) {
        return valueString.split(VALUE_DELIMITER, 2);
    }

    public static String createEntry(Object key, Object value) {
        return key + VALUE_DELIMITER + value;
    }
}
