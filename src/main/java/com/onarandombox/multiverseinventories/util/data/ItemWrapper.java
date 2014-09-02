package com.onarandombox.multiverseinventories.util.data;

import com.dumptruckman.minecraft.util.Logging;
import com.onarandombox.multiverseinventories.api.DataStrings;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is meant to wrap an ItemStack so that it can easily be serialized/deserialized in String format.
 * @deprecated Moved class to {@link ItemWrapper}
 */
@Deprecated
public final class ItemWrapper {

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

