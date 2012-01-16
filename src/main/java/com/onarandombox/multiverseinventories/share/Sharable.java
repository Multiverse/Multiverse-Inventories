package com.onarandombox.multiverseinventories.share;

import java.util.HashMap;
import java.util.Map;

/**
 * Enum for specifying the different sharable things.
 */
public enum Sharable {

    /**
     * Sharing Inventory.
     */
    INVENTORY("inventory", "inv"),
    /**
     * Sharing Health.
     */
    HEALTH("health", "hp"),
    /**
     * Sharing Experience.
     */
    EXPERIENCE("experience", "exp", "xp"),
    /**
     * Sharing Hunger.
     */
    HUNGER("hunger", "food"),
    /**
     * Sharing Effects.
     */
    EFFECTS("effects", "fx", "potions"),

    /**
     * Indicates ALL of the above.
     */
    ALL("*", "all", "everything");

    private String name;

    private static Map<String, Sharable> lookup = new HashMap<String, Sharable>();

    Sharable(String name, String...extraNames) {
        this.addLookup(name);
        for (String extraName : extraNames) {
            this.addLookup(extraName);
        }
    }

    private void addLookup(String name) {
        Sharable.lookup.put(name, this);
    }

    @Override
    public String toString() {
        return this.name;
    }

    /**
     * Looks up a sharable by one of the acceptable names.
     *
     * @param name Name to look up by.
     * @return Sharable by that name or null if none by that name.
     */
    public static Sharable lookup(String name) {
        return Sharable.lookup.get(name);
    }
}
