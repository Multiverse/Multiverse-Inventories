package org.mvplugins.multiverse.inventories.util;

import com.dumptruckman.minecraft.util.Logging;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.mvplugins.multiverse.external.vavr.control.Try;
import org.mvplugins.multiverse.inventories.MultiverseInventories;
import org.mvplugins.multiverse.inventories.config.InventoriesConfig;

import java.util.Base64;

public class ItemStackConverter {

    public final static boolean hasByteSerializeSupport;

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
        if (config != null && config.getUseByteSerializationForInventoryData() && hasByteSerializeSupport) {
            if (itemStack.getType() == Material.AIR) {
                return null;
            }
            return Try.of(() -> Base64.getEncoder().encodeToString(itemStack.serializeAsBytes()))
                    .onFailure(e -> Logging.severe("Could not serialize item stack: %s", e.getMessage()))
                    .getOrNull();
        }
        // let ConfigurationSerialization handle it
        return itemStack;
    }
}
