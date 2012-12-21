package com.onarandombox.multiverseinventories.api.share;

import com.onarandombox.multiverseinventories.api.DataStrings;
import org.bukkit.inventory.ItemStack;

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
        return DataStrings.parseInventory(obj, this.inventorySize);
    }

    @Override
    public Object serialize(ItemStack[] itemStacks) {
        //return DataStrings.valueOf(itemStacks);
        return DataStrings.asJsonObject(itemStacks);
    }
}
