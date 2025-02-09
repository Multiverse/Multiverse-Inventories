package org.mvplugins.multiverse.inventories.util;

/**
 * This class handles the formatting of strings for data i/o.
 */
public final class DataStrings {

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
     * Player off hand item identifier.
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
     * Player last location identifier.
     */
    public static final String PLAYER_LAST_LOCATION = "lastLocation";
    /**
     * Player global profile data
     */
    public static final String PLAYER_DATA = "playerData";
    /**
     * Player last world identifier.
     */
    public static final String PLAYER_LAST_WORLD = "lastWorld";
    /**
     * Player should load identifier.
     */
    public static final String PLAYER_SHOULD_LOAD = "shouldLoad";
    /**
     * Player last known name identifier.
     */
    public static final String PLAYER_LAST_KNOWN_NAME = "lastKnownName";
    /**
     * Player profile type identifier.
     */
    public static final String PLAYER_PROFILE_TYPE = "profileType";
    /**
     * Player health identifier.
     */
    public static final String PLAYER_HEALTH = "hp";
    /**
     * Player experience identifier.
     */
    public static final String PLAYER_EXPERIENCE = "xp";
    /**
     * Player total experience identifier.
     */
    public static final String PLAYER_TOTAL_EXPERIENCE = "txp";
    /**
     * Player experience level identifier.
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
     * Player max air identifier.
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
        throw new IllegalStateException();
    }
}
