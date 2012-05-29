package com.onarandombox.multiverseinventories.api;

import com.onarandombox.multiverseinventories.util.Logging;
import com.onarandombox.multiverseinventories.util.MinecraftTools;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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
     * Player bed spawn location identifier.
     */
    public static final String PLAYER_LAST_LOCATION = "lastLocation";
    /**
     * Player bed spawn location identifier.
     */
    public static final String PLAYER_LAST_WORLD = "lastWorld";
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
     * @param inventoryString An inventory in string form to be parsed into an ItemStack array.
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
     * @param potionsString A player's potion effects in string form to be parsed into Collection<PotionEffect>.
     * @return a collection of potion effects parsed from potionsString.
     */
    public static PotionEffect[] parsePotionEffects(String potionsString) {
        List<PotionEffect> potionEffectList = new LinkedList<PotionEffect>();
        String[] potionsArray = potionsString.split(DataStrings.GENERAL_DELIMITER);
        for (String potionString : potionsArray) {
            String[] potionArray = potionString.split(DataStrings.SECONDARY_DELIMITER);
            int type = -1;
            int duration = -1;
            int amplifier = -1;
            for (String entryString : potionArray) {
                try {
                    String[] potionValue = DataStrings.splitEntry(entryString);
                    if (potionValue[0].equals(POTION_TYPE)) {
                        type = Integer.valueOf(potionValue[1]);
                    } else if (potionValue[0].equals(POTION_DURATION)) {
                        duration = Integer.valueOf(potionValue[1]);
                    } else if (potionValue[0].equals(POTION_AMPLIFIER)) {
                        amplifier = Integer.valueOf(potionValue[1]);
                    }
                } catch (Exception ignore) { }
            }
            if (type == -1 || duration == -1 || amplifier == -1) {
                if (!potionString.isEmpty()) {
                    Logging.fine("Could not potion effect string: " + potionString);
                }
            } else {
                potionEffectList.add(new PotionEffect(PotionEffectType.getById(type), duration, amplifier));
            }
        }
        return potionEffectList.toArray(new PotionEffect[potionEffectList.size()]);
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

    /**
     * Converts a Collection of {@link PotionEffect} into a String for easy persistence.
     *
     * @param potionEffects The potion effects you wish to "string-i-tize".
     * @return A String representation of a Collection<{@link PotionEffect}>
     */
    public static String valueOf(PotionEffect[] potionEffects) {
        StringBuilder builder = new StringBuilder();
        for (PotionEffect potion : potionEffects) {
            if (!builder.toString().isEmpty()) {
                builder.append(DataStrings.GENERAL_DELIMITER);
            }
            builder.append(DataStrings.createEntry(DataStrings.POTION_TYPE, potion.getType().getId()));
            builder.append(DataStrings.SECONDARY_DELIMITER);
            builder.append(DataStrings.createEntry(DataStrings.POTION_DURATION, potion.getDuration()));
            builder.append(DataStrings.SECONDARY_DELIMITER);
            builder.append(DataStrings.createEntry(DataStrings.POTION_AMPLIFIER, potion.getAmplifier()));
        }
        return builder.toString();
    }

    /**
     * This is meant to wrap an ItemStack so that it can easily be serialized/deserialized in String format.
     */
    public static final class ItemWrapper {

        private ItemStack item;

        /**
         * Wraps the given {@link ItemStack} in an ItemWrapper so that it can be easily turned into a string.
         *
         * @param item The {@link ItemStack} to wrap.
         * @return The wrapped {@link ItemStack}.
         */
        public static ItemWrapper wrap(ItemStack item) {
            return new ItemWrapper(item);
        }

        /**
         * Parses the given String as an ItemWrapper so that it can be easily turned into an {@link ItemStack}.
         *
         * @param itemString the String to parse.
         * @return The wrapped {@link ItemStack}.
         */
        public static ItemWrapper wrap(String itemString) {
            return new ItemWrapper(itemString);
        }

        private ItemWrapper(ItemStack item) {
            this.item = item;
        }

        private ItemWrapper(String itemString) {
            int type = 0;
            short damage = 0;
            int amount = 1;
            String enchantsString = null;

            String[] itemData = itemString.split(DataStrings.GENERAL_DELIMITER);

            for (String dataString : itemData) {
                String[] dataValue = DataStrings.splitEntry(dataString);
                try {
                    if (dataValue[0].equals(DataStrings.ITEM_TYPE_ID)) {
                        type = Integer.valueOf(dataValue[1]);
                    } else if (dataValue[0].equals(DataStrings.ITEM_DURABILITY)) {
                        damage = Short.valueOf(dataValue[1]);
                    } else if (dataValue[0].equals(DataStrings.ITEM_AMOUNT)) {
                        amount = Integer.valueOf(dataValue[1]);
                    } else if (dataValue[0].equals(DataStrings.ITEM_ENCHANTS)) {
                        enchantsString = dataValue[1];
                    }
                } catch (Exception e) {
                    Logging.fine("Could not parse item string: " + itemString);
                    Logging.fine(e.getMessage());
                }
            }
            this.item = new ItemStack(type, amount, damage);
            if (enchantsString != null) {
                this.getItem().addUnsafeEnchantments(this.parseEnchants(enchantsString));
            }
        }

        private Map<Enchantment, Integer> parseEnchants(String enchantsString) {
            String[] enchantData = enchantsString.split(DataStrings.SECONDARY_DELIMITER);
            Map<Enchantment, Integer> enchantsMap = new LinkedHashMap<Enchantment, Integer>(enchantData.length);

            for (String dataValue : enchantData) {
                String[] enchantValues = DataStrings.splitEntry(dataValue);
                try {
                    Enchantment enchantment = Enchantment.getByName(enchantValues[0]);
                    if (enchantment == null) {
                        Logging.fine("Could not parse item enchantment: " + enchantValues[0]);
                        continue;
                    }
                    enchantsMap.put(enchantment, Integer.valueOf(enchantValues[1]));
                } catch (Exception ignore) {
                }
            }
            return enchantsMap;
        }

        /**
         * Retrieves the ItemStack that this class is wrapping.
         *
         * @return The ItemStack this class is wrapping.
         */
        public ItemStack getItem() {
            return this.item;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            StringBuilder result = new StringBuilder();

            result.append(DataStrings.createEntry(DataStrings.ITEM_TYPE_ID, this.getItem().getTypeId()));

            if (this.getItem().getDurability() != 0) {
                result.append(DataStrings.GENERAL_DELIMITER);
                result.append(DataStrings.createEntry(DataStrings.ITEM_DURABILITY, this.getItem().getDurability()));
            }

            if (this.getItem().getAmount() != 1) {
                result.append(DataStrings.GENERAL_DELIMITER);
                result.append(DataStrings.createEntry(DataStrings.ITEM_AMOUNT, this.getItem().getAmount()));
            }

            Map<Enchantment, Integer> enchants = getItem().getEnchantments();
            if (enchants.size() > 0) {
                result.append(DataStrings.GENERAL_DELIMITER);
                result.append(DataStrings.ITEM_ENCHANTS);
                result.append(DataStrings.VALUE_DELIMITER);

                boolean first = true;
                for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
                    if (first) {
                        first = false;
                    } else {
                        result.append(DataStrings.SECONDARY_DELIMITER);
                    }
                    result.append(entry.getKey().getName());
                    result.append(DataStrings.VALUE_DELIMITER);
                    result.append(entry.getValue());
                }
            }

            return result.toString();
        }
    }
}

