package com.onarandombox.multiverseinventories.api;

/**
 * A collection of values relating to a Minecraft player.
 */
public class PlayerStats {

    /**
     * Number of slots in Minecraft player inventory.
     */
    public static final int INVENTORY_SIZE = 36;
    /**
     * Number of slots for armor for player.
     */
    public static final int ARMOR_SIZE = 4;
    /**
     * Number of slots in an ender chest.
     */
    public static final int ENDER_CHEST_SIZE = 27;
    /**
     * Default health value.
     */
    public static final int HEALTH = 20;
    /**
     * Default experience value.
     */
    public static final float EXPERIENCE = 0F;
    /**
     * Default total experience value.
     */
    public static final int TOTAL_EXPERIENCE = 0;
    /**
     * Default level value.
     */
    public static final int LEVEL = 0;
    /**
     * Default food level value.
     */
    public static final int FOOD_LEVEL = 20;
    /**
     * Default exhaustion value.
     */
    public static final float EXHAUSTION = 0F;
    /**
     * Default saturation value.
     */
    public static final float SATURATION = 5F;
    /**
     * Default fall distance value.
     */
    public static final float FALL_DISTANCE = 0F;
    /**
     * Default fire ticks value.
     */
    public static final int FIRE_TICKS = 0;
    /**
     * Default remaining air value.
     */
    public static final int REMAINING_AIR = 300;
    /**
     * Default maximum air value.
     */
    public static final int MAXIMUM_AIR = 300;

    private PlayerStats() {
        throw new AssertionError();
    }
}

