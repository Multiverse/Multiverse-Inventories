package com.onarandombox.multiverseinventories.share.serializers;

import com.onarandombox.multiverseinventories.share.serializers.SharableSerializer;
import com.onarandombox.multiverseinventories.util.MinecraftTools;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link SharableSerializer} usable with ItemStack[] which converts the ItemStack[] to the string format
 * that is used by default in Multiverse-Inventories.
 */
public final class InventorySerializer implements SharableSerializer<ItemStack[]> {

    private int inventorySize;

    public InventorySerializer(final int inventorySize) {
        this.inventorySize = inventorySize;
    }

    @Override
    public ItemStack[] deserialize(Object obj) {
        return unmapSlots(obj);
    }

    @Override
    public Object serialize(ItemStack[] itemStacks) {
        return mapSlots(itemStacks);
    }

    private Map<String, ItemStack> mapSlots(ItemStack[] itemStacks) {
        Map<String, ItemStack> result = new HashMap<>(itemStacks.length);
        for (int i = 0; i < itemStacks.length; i++) {
            if (itemStacks[i] != null && itemStacks[i].getType() != Material.AIR) {
                result.put(Integer.toString(i), itemStacks[i]);
            }
        }
        return result;
    }

    private ItemStack[] unmapSlots(Object obj) {
        ItemStack[] result = new ItemStack[inventorySize];
        if (obj instanceof Map) {
            Map<?, ?> invMap = (Map) obj;
            for (int i = 0; i < result.length; i++) {
                Object value = invMap.get(Integer.toString(i));
                if (value != null && value instanceof ItemStack) {
                    result[i] = (ItemStack) value;
                } else {
                    result[i] = new ItemStack(Material.AIR);
                }
            }
            return result;
        }
        return MinecraftTools.fillWithAir(result);
    }
}
