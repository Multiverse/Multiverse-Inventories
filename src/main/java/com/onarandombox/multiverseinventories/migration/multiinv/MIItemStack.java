package com.onarandombox.multiverseinventories.migration.multiinv;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * Wraps an item brought in from MultiInv's data files.
 */
public class MIItemStack {

    private final int dataLength = 4;
    private int itemID = 0;
    private int quantity = 0;
    private short durability = 0;
    private Map<Enchantment, Integer> enchantments = new HashMap<Enchantment, Integer>();

    // Constructor to create an MIItemStack from a string containing its data
    public MIItemStack(String dataString) {
        String[] data = dataString.split(",");
        if (data.length == this.dataLength) {
            try {
                this.itemID = Integer.parseInt(data[0]);
                this.quantity = Integer.parseInt(data[1]);
                this.durability = Short.parseShort(data[2]);
                getEnchantments(data[3]);
            } catch (NumberFormatException ignore) { }
        }
    }

    private void getEnchantments(String enchantmentString) {
        if (!"0".equals(enchantmentString)) {
            String[] enchants = enchantmentString.split("#");
            for (String enchantment : enchants) {
                String[] parts = enchantment.split("-");
                int id = Integer.parseInt(parts[0]);
                int level = Integer.parseInt(parts[1]);
                Enchantment e = Enchantment.getById(id);
                this.enchantments.put(e, level);
            }
        }
    }

    /**
     * @return The ItemStack this object wraps.
     */
    public ItemStack getItemStack() {
        ItemStack itemStack = null;
        if (itemID != 0 && quantity != 0) {
            itemStack = new ItemStack(itemID, quantity, durability);
            itemStack.addEnchantments(enchantments);
        }
        return itemStack;
    }
}
