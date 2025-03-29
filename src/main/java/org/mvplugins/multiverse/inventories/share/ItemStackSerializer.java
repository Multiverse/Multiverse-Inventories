package org.mvplugins.multiverse.inventories.share;

import org.bukkit.inventory.ItemStack;
import org.mvplugins.multiverse.inventories.util.ItemStackConverter;

final class ItemStackSerializer implements SharableSerializer<ItemStack> {

    @Override
    public ItemStack deserialize(Object obj) {
        return ItemStackConverter.deserialize(obj);
    }

    @Override
    public Object serialize(ItemStack itemStack) {
        return ItemStackConverter.serialize(itemStack);
    }
}
