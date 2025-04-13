package com.onarandombox.multiverseinventories.share;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemStackSerializer implements SharableSerializer<ItemStack> {
    @Override
    public ItemStack deserialize(Object obj) {
        return obj instanceof ItemStack ? (ItemStack) obj : null;
    }

    @Override
    public Object serialize(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR || itemStack.getAmount() == 0) {
            return null;
        }
        return itemStack;
    }
}
