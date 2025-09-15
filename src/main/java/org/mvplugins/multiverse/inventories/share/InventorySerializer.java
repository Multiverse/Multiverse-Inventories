package org.mvplugins.multiverse.inventories.share;

import org.mvplugins.multiverse.inventories.MultiverseInventoriesApi;
import org.mvplugins.multiverse.inventories.util.ItemStackConverter;
import org.mvplugins.multiverse.inventories.util.MinecraftTools;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link SharableSerializer} usable with ItemStack[] which converts the ItemStack[] to the string format
 * that is used by default in Multiverse-Inventories.
 */
abstract class InventorySerializer implements SharableSerializer<ItemStack[]> {

    @Override
    public ItemStack[] deserialize(Object obj) {
        return unmapSlots(obj);
    }

    @Override
    public Object serialize(ItemStack[] itemStacks) {
        return mapSlots(itemStacks);
    }

    private Map<String, Object> mapSlots(ItemStack[] itemStacks) {
        Map<String, Object> result = new HashMap<>(itemStacks.length);
        for (int i = 0; i < itemStacks.length; i++) {
            Object serialize = ItemStackConverter.serialize(itemStacks[i]);
            if (serialize != null) {
                result.put(Integer.toString(i), serialize);
            }
        }
        return result;
    }

    private ItemStack[] unmapSlots(Object obj) {
        ItemStack[] inventory = new ItemStack[getInventorySize()];
        if (!(obj instanceof Map invMap)) {
            return MinecraftTools.fillWithAir(inventory);
        }
        for (int i = 0; i < inventory.length; i++) {
            Object value = invMap.get(Integer.toString(i));
            if (value == null) {
                inventory[i] = new ItemStack(Material.AIR);
                continue;
            }
            ItemStack item = ItemStackConverter.deserialize(value);
            if (item == null) {
                inventory[i] = new ItemStack(Material.AIR);
                continue;
            }
            inventory[i] = item;
        }
        return inventory;
    }

    protected abstract int getInventorySize();

    static final class MainInventorySerializer extends InventorySerializer {
        @Override
        protected int getInventorySize() {
            return MultiverseInventoriesApi.get().getInventoriesConfig().getMaxInventoryItemsSize();
        }
    }

    static final class EnderChestSerializer extends InventorySerializer {
        @Override
        protected int getInventorySize() {
            return MultiverseInventoriesApi.get().getInventoriesConfig().getMaxEnderChestItemsSize();
        }
    }

    static final class ArmorSerializer extends InventorySerializer {
        @Override
        protected int getInventorySize() {
            return MultiverseInventoriesApi.get().getInventoriesConfig().getMaxArmorItemsSize();
        }
    }
}
