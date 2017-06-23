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
     * NBTTags identifier.
     */
    public static final String ITEM_ITEMSTACK = "is";
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
        if (locString.startsWith("{")) {
            return jsonParseLocation(locString);
        } else {
            return legacyParseLocation(locString);
        }
    }

    public static Location parseLocation(Map locMap) {
        return parseLocMap(locMap);
    }

    private static Location legacyParseLocation(String locString) {
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

    private static Location jsonParseLocation(String locString) {
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
     */
    public static PotionEffect[] parsePotionEffects(String potionsString) {
        if (potionsString.startsWith("[")) {
            return jsonParsePotionEffects(potionsString);
        } else {
            return legacyParsePotionEffects(potionsString);
        }
    }

    public static PotionEffect[] legacyParsePotionEffects(String potionsString) {
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

    public static PotionEffect[] jsonParsePotionEffects(String potionsString) {
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

    /**
     * Converts an ItemStack array into a String for easy persistence.
     *
     * @param items The items you wish to "string-i-tize".
     * @return A string representation of an inventory.
     */
    public static String valueOf(ItemStack[] items) {
        JSONObject jsonItems = new JSONObject();
        for (int i = 0; i < items.length; i++) {
            if (items[i] != null && items[i].getTypeId() != 0) {
                jsonItems.put(Integer.valueOf(i).toString(), new JSONItemWrapper(items[i]).asJSONObject());
            }
        }
        return jsonItems.toJSONString();
    }

    /**
     * Converts an ItemStack array into a String for easy persistence.
     *
     * @param items The items you wish to "string-i-tize".
     * @return A string representation of an inventory.
     */
    public static JSONObject asJsonObject(ItemStack[] items, int inventorySize) {
        JSONObject jsonItems = new JSONObject();
        for (int i = 0; i < items.length && i < inventorySize; i++) {
            if (items[i] != null && items[i].getTypeId() != 0) {
                jsonItems.put(Integer.valueOf(i).toString(), items[i]);
                //jsonItems.put(Integer.valueOf(i).toString(), new JSONItemWrapper(items[i]).asJSONObject());
            }
        }
        return jsonItems;
    }

    private static String legacyValueOf(ItemStack[] items) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < items.length; i++) {
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
        return valueOfLocation(location).toJSONString();
    }

    public static JSONObject valueOfLocation(Location location) {
        JSONObject jsonLoc = new JSONObject();
        jsonLoc.put(LOCATION_WORLD, location.getWorld().getName());
        jsonLoc.put(DataStrings.LOCATION_X, location.getX());
        jsonLoc.put(DataStrings.LOCATION_Y, location.getY());
        jsonLoc.put(DataStrings.LOCATION_Z, location.getZ());
        jsonLoc.put(DataStrings.LOCATION_PITCH, location.getPitch());
        jsonLoc.put(DataStrings.LOCATION_YAW, location.getYaw());
        return jsonLoc;
    }

    private static String legacyValueOf(Location location) {
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
        JSONArray jsonPotions = new JSONArray();
        for (PotionEffect potion : potionEffects) {
            JSONObject jsonPotion = new JSONObject();
            jsonPotion.put(DataStrings.POTION_TYPE, potion.getType().getId());
            jsonPotion.put(DataStrings.POTION_DURATION, potion.getDuration());
            jsonPotion.put(DataStrings.POTION_AMPLIFIER, potion.getAmplifier());
            jsonPotions.add(jsonPotion);
        }
        return jsonPotions.toJSONString();
    }

    private static String legacyValueOf(PotionEffect[] potionEffects) {
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
    public static abstract class ItemWrapper {

        protected ItemStack item;

        /**
         * Wraps the given {@link ItemStack} in an ItemWrapper so that it can be easily turned into a string.
         *
         * @param item The {@link ItemStack} to wrap.
         * @return The wrapped {@link ItemStack}.
         */
        public static ItemWrapper wrap(final ItemStack item) {
            return new JSONItemWrapper(item);
        }

        /**
         * Parses the given String as an ItemWrapper so that it can be easily turned into an {@link ItemStack}.
         *
         * @param itemString the String to parse.
         * @return The wrapped {@link ItemStack}.
         */
        public static ItemWrapper wrap(final String itemString) {
            try {
                if (itemString.startsWith("{")) {
                    return new JSONItemWrapper(itemString);
                } else {
                    return new LegacyItemWrapper(itemString);
                }
            } catch (Exception e) {
                Logging.warning("Encountered exception while converting item from string: " + e.getMessage());
            }
            return new JSONItemWrapper(new ItemStack(Material.AIR));
        }

        /**
         * Parses the given String as an ItemWrapper so that it can be easily turned into an {@link ItemStack}.
         *
         * @param jsonItems the items in JSONObject form to parse.
         * @return The wrapped {@link ItemStack}.
         */
        public static ItemWrapper wrap(final JSONObject jsonItems) {
            return new JSONItemWrapper(jsonItems);
        }

        /**
         * Retrieves the ItemStack that this class is wrapping.
         *
         * @return The ItemStack this class is wrapping.
         */
        public final ItemStack getItem() {
            return this.item;
        }

        @Override
        public String toString() {
            try {
                return asString();
            } catch (Exception e) {
                Logging.warning("Encountered exception while converting item to string: " + e.getMessage());
            }
            return "";
        }

        public abstract String asString();
    }

    /**
     * This is meant to wrap an ItemStack so that it can easily be serialized/deserialized in String format.
     */
    private static final class LegacyItemWrapper extends ItemWrapper {

        private LegacyItemWrapper(final ItemStack item) {
            this.item = item;
        }

        private LegacyItemWrapper(final String itemString) {
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

        private Map<Enchantment, Integer> parseEnchants(final String enchantsString) {
            String[] enchantData = enchantsString.split(DataStrings.SECONDARY_DELIMITER);
            Map<Enchantment, Integer> enchantsMap = new LinkedHashMap<Enchantment, Integer>(enchantData.length);

            for (String dataValue : enchantData) {
                String[] enchantValues = DataStrings.splitEntry(dataValue);
                try {
                    Enchantment enchantment = Enchantment.getByName(enchantValues[0]);
                    if (enchantment == null) {
                        enchantment = Enchantment.getById(Integer.valueOf(enchantValues[0]));
                        if (enchantment == null) {
                            Logging.fine("Could not parse item enchantment: " + enchantValues[0]);
                            continue;
                        }
                    }
                    enchantsMap.put(enchantment, Integer.valueOf(enchantValues[1]));
                } catch (Exception ignore) {
                }
            }
            return enchantsMap;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String asString() {
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
                    if (entry.getKey() == null) {
                        Logging.finer("Not saving null enchantment!");
                        continue;
                    }
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

    private static final JSONParser JSON_PARSER = new JSONParser(JSONParser.USE_INTEGER_STORAGE);
    /**
     * This is meant to wrap an ItemStack so that it can easily be serialized/deserialized in String format.
     */
    private static final class JSONItemWrapper extends ItemWrapper {

        private JSONItemWrapper(final ItemStack item) {
            this.item = item;
        }

        private JSONItemWrapper(final String itemString) {
            JSONObject itemData = null;
            try {
                Object obj = JSON_PARSER.parse(itemString);
                if (obj instanceof JSONObject) {
                    itemData = (JSONObject) obj;
                }
            } catch (ParseException e) {
                Logging.warning("Could not parse item: " + itemString + "!  Item may be lost!");
            }
            if (itemData == null) {
                Logging.warning("Could not parse item: " + itemString + "!  Item may be lost!");
            }
            createItem(itemData);
        }

        private JSONItemWrapper(JSONObject itemData) {
            if (itemData == null) {
                Logging.warning("Could not parse item!  Item may be lost!");
            }
            createItem(itemData);
        }

        private static boolean hasCraftBukkit() {
            try {
                Class.forName("org.bukkit.craftbukkit.v1_4_6.inventory.CraftItemStack");
                Class.forName("net.minecraft.server.v1_4_6.ItemStack");
            } catch (ClassNotFoundException e) {
                return false;
            }
            return true;
        }

        private void createItem(JSONObject itemData) {
            if (itemData != null && itemData.containsKey(ITEM_ITEMSTACK)) {
                Object obj = itemData.get(ITEM_ITEMSTACK);
                if (obj instanceof Map) {
                    Map<String, Object> map = (Map<String, Object>) obj;
                    // JSONObject apparently likes to store numbers as Longs
                    /*for (Map.Entry<String, Object> entry : map.entrySet()) {
                        if (entry.getValue() instanceof Long) {
                            entry.setValue(((Long) entry.getValue()).intValue());
                        }
                    }
                    */
                    this.item = ItemStack.deserialize(map);
                    /*
                    if (map.containsKey("meta")) {
                        Object metaObj = map.get("meta");
                        if (metaObj instanceof Map) {
                            Map<String, Object> metaMap = (Map<String, Object>) metaObj;
                            metaMap.put("==", "ItemMeta");
                            ItemMeta meta = (ItemMeta) ConfigurationSerialization.deserializeObject(metaMap);
                            this.item.setItemMeta(meta);
                        }
                    }
                    */
                    return;
                } else {
                    Logging.warning("Could not parse item: " + obj);
                }
            }
            int type = 0;
            short damage = 0;
            int amount = 1;
            if (itemData != null) {
                Object obj = itemData.get(ITEM_TYPE_ID);
                if (obj != null && obj instanceof Number) {
                    type = ((Number) obj).intValue();
                }
                obj = itemData.get(ITEM_AMOUNT);
                if (obj != null && obj instanceof Number) {
                    amount = ((Number) obj).intValue();
                }
                obj = itemData.get(ITEM_DURABILITY);
                if (obj != null && obj instanceof Number) {
                    damage = ((Number) obj).shortValue();
                }
            }
            this.item = new ItemStack(type, amount, damage);

            if (itemData != null && itemData.containsKey(ITEM_ENCHANTS)) {
                Object obj = itemData.get(ITEM_ENCHANTS);
                if (obj instanceof JSONObject) {
                    this.getItem().addUnsafeEnchantments(this.parseEnchants((JSONObject) obj));
                } else {
                    Logging.warning("Could not parse item enchantments: " + obj);
                }
            }

            if (!hasCraftBukkit() || itemData == null) {
                return;
            }
        }

        private Map<Enchantment, Integer> parseEnchants(JSONObject enchantData) {
            final Map<Enchantment, Integer> enchantsMap = new LinkedHashMap<Enchantment, Integer>(enchantData.size());
            for (Object key : enchantData.keySet()) {
                Enchantment enchantment = Enchantment.getByName(key.toString());
                Object value = enchantData.get(key);
                if (enchantment == null || !(value instanceof Number)) {
                    Logging.fine("Could not parse item enchantment: " + key.toString());
                    continue;
                }
                enchantsMap.put(enchantment, ((Number) value).intValue());
            }
            return enchantsMap;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String asString() {
            return asJSONObject().toJSONString();
        }

        private JSONObject jsonObjectFromMap(final Map<String, Object> map) {
            final JSONObject json = new JSONObject();
            for (final Map.Entry<String, Object> entry : map.entrySet()) {
                if (entry.getValue() instanceof Map) {
                    json.put(entry.getKey(), jsonObjectFromMap((Map<String, Object>) entry.getValue()));
                } else {
                    json.put(entry.getKey(), entry.getValue());
                }
            }
            return json;
        }

        public JSONObject asJSONObject() {
           // final JSONObject jsonItem = new JSONObject();
            //final Map<String, Object> map = getItem().serialize();
            //if (map.containsKey("meta")) {
            //    map.put("meta", getItem().getItemMeta().serialize());
            //}
            //jsonItem.put(ITEM_ITEMSTACK, jsonObjectFromMap(map));
            return jsonObjectFromMap(getItem().serialize());
        }
        
 
    }
    
}

