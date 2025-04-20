package org.mvplugins.multiverse.inventories.util;

import com.dumptruckman.minecraft.util.Logging;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.mvplugins.multiverse.external.vavr.control.Try;
import org.mvplugins.multiverse.inventories.MultiverseInventories;
import org.mvplugins.multiverse.inventories.config.InventoriesConfig;

import java.util.Base64;

public final class ItemStackConverter {

    private final static boolean hasByteSerializeSupport;

    static {
        hasByteSerializeSupport = Try.run(() -> ItemStack.class.getMethod("deserializeBytes", byte[].class))
                .map(ignore -> true)
                .recover(ignore -> false)
                .getOrElse(false);
    }

    private static InventoriesConfig config = null;

    public static void init(MultiverseInventories plugin) {
        config = plugin.getServiceLocator().getService(InventoriesConfig.class);
    }

    public static boolean isEmptyItemStack(@Nullable ItemStack itemStack) {
        return itemStack == null || itemStack.getType() == Material.AIR || itemStack.getAmount() == 0;
    }

//    /**
//     * Format: `material[custom properties] amount`
//     * Example: iron_sword[attribute_modifiers=[{type:"generic.attack_damage",id:"op_damage",amount:10000,operation:"add_value",slot:"mainhand"}]] 1
//     *
//     * @return
//     */
//    @Nullable
//    public static ItemStack fromString(String item) {
//
//    }

    @Nullable
    public static ItemStack deserialize(Object obj) {
        if (obj instanceof ItemStack itemStack) {
            // Already handled by ConfigurationSerialization
            return itemStack;
        }
        if (hasByteSerializeSupport && obj instanceof String string) {
            byte[] bytes = Base64.getDecoder().decode(string);
            return ItemStack.deserializeBytes(bytes);
        }
        return null;
    }

    @Nullable
    public static Object serialize(ItemStack itemStack) {
        if (isEmptyItemStack(itemStack)) {
            return null;
        }
        if (config == null || !config.getUseByteSerializationForInventoryData() || !hasByteSerializeSupport) {
            // let ConfigurationSerialization handle it
            return itemStack;
        }
        return Try.of(() -> Base64.getEncoder().encodeToString(itemStack.serializeAsBytes()))
                .onFailure(e -> Logging.severe("Could not byte serialize item stack: %s", e.getMessage()))
                .getOrNull();
    }

    public static boolean hasByteSerializeSupport() {
        return hasByteSerializeSupport;
    }

    private ItemStackConverter() {
        throw new IllegalStateException();
    }
}
