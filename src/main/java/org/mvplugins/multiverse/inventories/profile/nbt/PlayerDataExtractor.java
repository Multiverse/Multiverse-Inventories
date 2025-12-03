package org.mvplugins.multiverse.inventories.profile.nbt;

import com.dumptruckman.minecraft.util.Logging;
import com.viaversion.nbt.io.NBTIO;
import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.nbt.tag.ListTag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.external.vavr.control.Try;
import org.mvplugins.multiverse.inventories.profile.data.ProfileData;
import org.mvplugins.multiverse.inventories.profile.data.ProfileDataSnapshot;
import org.mvplugins.multiverse.inventories.share.Sharables;
import org.mvplugins.multiverse.inventories.util.PlayerStats;

import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.zip.GZIPOutputStream;

/**
 * @deprecated This feature has been moved to Multiverse-InventoriesImporter plugin. This class's code logic is outdated
 *             and does not work on some Minecraft versions, hence should not be used.
 */
@Deprecated(since = "5.3", forRemoval = true)
@ApiStatus.AvailableSince("5.2")
@Service
public final class PlayerDataExtractor {

    @ApiStatus.AvailableSince("5.2")
    public Try<ProfileData> extract(Path path) {
        return Try.of(() -> {
            if (!path.toFile().exists()) {
                Logging.warning("File %s does not exist! %s", path);
                throw new IOException();
            }
            Logging.finest("Extracting %s", path);

            CompoundTag playerData = NBTIO
                    .reader(CompoundTag.class)
                    .named()
                    .read(path, true);

            int dataVersion = playerData.getInt("DataVersion");
            Logging.finest("Data version: %s", dataVersion);
            CompoundTag equipment = playerData.getCompoundTag("equipment");
            if (equipment == null) {
                equipment = new CompoundTag();
            }

            ProfileData profileData = new ProfileDataSnapshot();

            profileData.set(Sharables.ARMOR, new ItemStack[]{
                    extractItem(equipment.getCompoundTag("feet"), dataVersion),
                    extractItem(equipment.getCompoundTag("legs"), dataVersion),
                    extractItem(equipment.getCompoundTag("chest"), dataVersion),
                    extractItem(equipment.getCompoundTag("head"), dataVersion)
            });
            // ADVANCEMENTS
            // BED_SPAWN
            profileData.set(Sharables.ENDER_CHEST, extractItems(
                    playerData.getListTag("EnderItems", CompoundTag.class),
                    dataVersion,
                    PlayerStats.ENDER_CHEST_SIZE
            ));
            profileData.set(Sharables.EXHAUSTION, playerData.getFloat("foodExhaustionLevel"));
            profileData.set(Sharables.EXPERIENCE, playerData.getFloat("XpP"));
            profileData.set(Sharables.FALL_DISTANCE, (float) playerData.getDouble("fall_distance"));
            profileData.set(Sharables.FIRE_TICKS, (int) playerData.getShort("Fire"));
            profileData.set(Sharables.FOOD_LEVEL, playerData.getInt("foodLevel"));
            // GAME_STATISTICS
            profileData.set(Sharables.HEALTH, (double) playerData.getFloat("Health"));
            profileData.set(Sharables.INVENTORY, extractItems(
                    playerData.getListTag("Inventory", CompoundTag.class),
                    dataVersion,
                    PlayerStats.INVENTORY_SIZE
            ));
            // LAST_LOCATION
            profileData.set(Sharables.LEVEL, playerData.getInt("XpLevel"));
            // MAXIMUM_AIR
            // MAX_HEALTH
            profileData.set(Sharables.OFF_HAND, extractItem(equipment.getCompoundTag("offhand"), dataVersion));
            // POTIONS
            // RECIPES
            profileData.set(Sharables.REMAINING_AIR, (int) playerData.getShort("Air"));
            profileData.set(Sharables.SATURATION, playerData.getFloat("foodSaturationLevel"));
            profileData.set(Sharables.TOTAL_EXPERIENCE, playerData.getInt("XpTotal"));

            return profileData;
        }).onFailure(ex -> {
            Logging.warning("Failed to extract player data from %s: %s", path, ex.getMessage());
            ex.printStackTrace();
        });
    }

    private ItemStack[] extractItems(@Nullable ListTag<CompoundTag> inventoryList, int dataVersion, int inventorySize) throws IOException {
        if (inventoryList == null) {
            return new ItemStack[inventorySize];
        }

        ItemStack[] items = new ItemStack[inventorySize];
        for (CompoundTag invData : inventoryList) {
            int slot = invData.getInt("Slot");
            invData.remove("Slot");
            items[slot] = extractItem(invData, dataVersion);
        }
        return items;
    }

    private @Nullable ItemStack extractItem(@Nullable CompoundTag invData, int dataVersion) throws IOException {
        if (invData == null) {
            return null;
        }

        invData.putInt("DataVersion", dataVersion);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream);
        NBTIO.writer().named().write(gzipOutputStream, invData);
        gzipOutputStream.close();
        byteArrayOutputStream.close();

        return Try.of(() -> ItemStack.deserializeBytes(byteArrayOutputStream.toByteArray()))
                .onFailure(throwable -> Logging.warning("Failed to deserialize item: %s", throwable.getMessage()))
                .getOrNull();
    }
}
