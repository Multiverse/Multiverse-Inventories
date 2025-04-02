package org.mvplugins.multiverse.inventories.dataimport.multiinv;

import com.dumptruckman.minecraft.util.Logging;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.inventories.config.InventoriesConfig;
import org.mvplugins.multiverse.inventories.dataimport.DataImportException;
import org.mvplugins.multiverse.inventories.profile.PlayerProfile;
import org.mvplugins.multiverse.inventories.profile.ProfileDataSource;
import org.mvplugins.multiverse.inventories.profile.key.ProfileTypes;
import org.mvplugins.multiverse.inventories.profile.key.ContainerType;
import org.mvplugins.multiverse.inventories.profile.container.ProfileContainerStoreProvider;
import org.mvplugins.multiverse.inventories.profile.group.WorldGroup;
import org.mvplugins.multiverse.inventories.profile.group.WorldGroupManager;
import org.mvplugins.multiverse.inventories.share.Sharables;
import uk.co.tggl.pluckerpluck.multiinv.MIYamlFiles;
import uk.co.tggl.pluckerpluck.multiinv.MultiInv;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

final class MultiInvImportHelper {

    @NotNull
    private final MultiInv multiInv;
    private final WorldGroupManager worldGroupManager;
    private final InventoriesConfig inventoriesConfig;
    private final ProfileContainerStoreProvider profileContainerStoreProvider;
    private final ProfileDataSource profileDataSource;

    MultiInvImportHelper(
            @NotNull MultiInv multiInv,
            @NotNull WorldGroupManager worldGroupManager,
            @NotNull InventoriesConfig inventoriesConfig,
            @NotNull ProfileContainerStoreProvider profileContainerStoreProvider,
            @NotNull ProfileDataSource profileDataSource) {
        super();
        this.multiInv = multiInv;
        this.worldGroupManager = worldGroupManager;
        this.inventoriesConfig = inventoriesConfig;
        this.profileContainerStoreProvider = profileContainerStoreProvider;
        this.profileDataSource = profileDataSource;
    }

    void importData() throws DataImportException {
        HashMap<String, String> miGroupMap = this.getGroupMap();
        if (miGroupMap == null) {
            throw new DataImportException("There is no data to import from MultiInv!");
        }
        if (!miGroupMap.isEmpty()) {
            WorldGroup defaultWorldGroup = worldGroupManager.getDefaultGroup();
            if (defaultWorldGroup != null) {
                worldGroupManager.removeGroup(defaultWorldGroup);
                Logging.info("Removed automatically created world group in favor of imported groups.");
            }
        }
        for (Map.Entry<String, String> groupEntry : miGroupMap.entrySet()) {
            WorldGroup worldGroup = worldGroupManager.getGroup(groupEntry.getValue());
            if (worldGroup == null) {
                worldGroup = worldGroupManager.newEmptyGroup(groupEntry.getValue());
                worldGroup.getShares().mergeShares(Sharables.allOf());
                Logging.info("Importing group: " + groupEntry.getValue());
                worldGroupManager.updateGroup(worldGroup);
            }
            worldGroup.addWorld(groupEntry.getValue());
        }
        inventoriesConfig.save();

        for (OfflinePlayer player : Bukkit.getServer().getOfflinePlayers()) {
            Logging.info("Processing MultiInv data for player: " + player.getName());
            for (Map.Entry<String, String> entry : miGroupMap.entrySet()) {
                String worldName = entry.getKey();
                String groupName = entry.getValue();
                MIPlayerFileLoader playerFileLoader = new MIPlayerFileLoader(multiInv, player, groupName);
                if (!playerFileLoader.load()) {
                    continue;
                }
                Logging.info("Processing MultiInv data for player: " + player.getName()
                        + " for group: " + groupName);
                mergeData(player, playerFileLoader, groupName, ContainerType.GROUP);
            }
            for (World world : Bukkit.getWorlds()) {
                String worldName = world.getName();
                MIPlayerFileLoader playerFileLoader = new MIPlayerFileLoader(multiInv, player, worldName);
                if (!playerFileLoader.load()) {
                    continue;
                }
                Logging.info("Processing MultiInv data for player: " + player.getName()
                        + " for world only: " + worldName);
                mergeData(player, playerFileLoader, worldName, ContainerType.WORLD);
            }
        }
    }

    private void mergeData(OfflinePlayer player, MIPlayerFileLoader playerFileLoader,
                           String dataName, ContainerType type) {
        PlayerProfile playerProfile;
        if (type.equals(ContainerType.GROUP)) {
            WorldGroup group = worldGroupManager
                    .getGroup(dataName);
            if (group == null) {
                Logging.warning("Could not import player data for group: " + dataName);
                return;
            }
            playerProfile = group.getGroupProfileContainer().getPlayerDataNow(ProfileTypes.SURVIVAL, player);
        } else {
            playerProfile = profileContainerStoreProvider.getStore(type)
                    .getContainer(dataName).getPlayerDataNow(ProfileTypes.SURVIVAL, player);
        }
        MIInventoryInterface inventoryInterface =
                playerFileLoader.getInventory(GameMode.SURVIVAL.toString());
        playerProfile.set(Sharables.INVENTORY, inventoryInterface.getInventoryContents());
        playerProfile.set(Sharables.ARMOR, inventoryInterface.getArmorContents());
        playerProfile.set(Sharables.HEALTH, playerFileLoader.getHealth());
        playerProfile.set(Sharables.SATURATION, playerFileLoader.getSaturation());
        playerProfile.set(Sharables.EXPERIENCE, playerFileLoader.getExperience());
        playerProfile.set(Sharables.TOTAL_EXPERIENCE, playerFileLoader.getTotalExperience());
        playerProfile.set(Sharables.LEVEL, playerFileLoader.getLevel());
        playerProfile.set(Sharables.FOOD_LEVEL, playerFileLoader.getHunger());
        profileDataSource.updatePlayerData(playerProfile);
    }

    /**
     * @return The group mapping from MultiInv, where worldName -> groupName.
     * @throws DataImportException If there was any issues getting the data through reflection.
     */
    private HashMap<String, String> getGroupMap() throws DataImportException {
        Field field;
        try {
            field = MIYamlFiles.class.getDeclaredField("groups");
        } catch (NoSuchFieldException nsfe) {
            throw new DataImportException("The running version of MultiInv is "
                    + "incompatible with the import feature.").setCauseException(nsfe);
        }
        field.setAccessible(true);
        HashMap<String, String> miGroupMap = null;
        try {
            miGroupMap = (HashMap<String, String>) field.get(null);
        } catch (IllegalAccessException | ClassCastException iae) {
            throw new DataImportException("The running version of MultiInv is "
                    + "incompatible with the import feature.").setCauseException(iae);
        }
        return miGroupMap;
    }
}
