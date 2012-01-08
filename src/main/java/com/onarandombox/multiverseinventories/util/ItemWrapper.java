package com.onarandombox.multiverseinventories.util;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedHashMap;
import java.util.Map;

public class ItemWrapper implements ConfigurationSerializable {

    private ItemStack item;

    public ItemWrapper(ItemStack item) {
        this.item = item;
    }

    public ItemStack getItem() {
        return this.item;
    }

    public Map<String, Object> serialize() {
        Map<String, Object> result = new LinkedHashMap<String, Object>();

        result.put("typeId", this.getItem().getTypeId());

        if (this.getItem().getDurability() != 0) {
            result.put("durability", this.getItem().getDurability());
        }

        if (this.getItem().getAmount() != 1) {
            result.put("amount", this.getItem().getAmount());
        }

        if (this.getItem().getData().getData() != 0) {
            result.put("data", this.getItem().getData().getData());
        }

        Map<Enchantment, Integer> enchants = getItem().getEnchantments();

        if (enchants.size() > 0) {
            ConfigurationSection enchantsSection = new MemoryConfiguration();

            for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
                enchantsSection.set(entry.getKey().getName(), entry.getValue());
            }

            result.put("enchantments", enchantsSection);
        }

        return result;
    }

    public static ItemWrapper deserialize(Map<String, Object> args) {
        int type = (Integer) args.get("typeId");
        short damage = 0;
        int amount = 1;
        byte data = 0;

        if (args.containsKey("durability")) {
            damage = (Short) args.get("durability");
        }

        if (args.containsKey("amount")) {
            amount = (Integer) args.get("amount");
        }

        if (args.containsKey("data")) {
            data = (Byte) args.get("data");
        }

        ItemWrapper result = new ItemWrapper(new ItemStack(type, amount, damage, data));

        if (args.containsKey("enchantments")) {
            Object raw = args.get("enchantments");

            if (raw instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<Object, Object> map = (Map<Object, Object>) raw;

                for (Map.Entry<Object, Object> entry : map.entrySet()) {
                    Enchantment enchantment = Enchantment.getByName(entry.getKey().toString());

                    if ((enchantment != null) && (entry.getValue() instanceof Integer)) {
                        result.getItem().addEnchantment(enchantment, (Integer) entry.getValue());
                    }
                }
            }
        }

        return result;
    }
}
