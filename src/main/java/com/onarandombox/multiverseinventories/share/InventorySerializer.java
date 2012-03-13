package com.onarandombox.multiverseinventories.share;

import com.onarandombox.multiverseinventories.api.DataStrings;
import org.bukkit.inventory.ItemStack;

public final class InventorySerializer implements SharableSerializer<ItemStack[]> {

    private int inventorySize;

    public InventorySerializer(final int inventorySize) {
        this.inventorySize = inventorySize;
    }

    @Override
    public final ItemStack[] deserialize(Object obj) {
        return DataStrings.parseInventory(obj.toString(), this.inventorySize);
    }

    @Override
    public final Object serialize(ItemStack[] itemStacks) {
        return DataStrings.valueOf(itemStacks);
    }
}
