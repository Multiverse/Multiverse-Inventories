package org.mvplugins.multiverse.inventories.dataimport.worldinventories;

import com.dumptruckman.minecraft.util.Logging;
import me.drayshak.WorldInventories.Group;
import me.drayshak.WorldInventories.WIPlayerInventory;
import me.drayshak.WorldInventories.WIPlayerStats;
import me.drayshak.WorldInventories.WorldInventories;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.inventories.config.InventoriesConfig;
import org.mvplugins.multiverse.inventories.dataimport.DataImportException;
import org.mvplugins.multiverse.inventories.profile.PlayerProfile;
import org.mvplugins.multiverse.inventories.profile.ProfileDataSource;
import org.mvplugins.multiverse.inventories.profile.ProfileTypes;
import org.mvplugins.multiverse.inventories.profile.container.ContainerType;
import org.mvplugins.multiverse.inventories.profile.container.ProfileContainer;
import org.mvplugins.multiverse.inventories.profile.container.ProfileContainerStoreProvider;
import org.mvplugins.multiverse.inventories.profile.group.WorldGroup;
import org.mvplugins.multiverse.inventories.profile.group.WorldGroupManager;
import org.mvplugins.multiverse.inventories.share.Sharables;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

final class WorldInventoriesImportHelper {

    @NotNull
    private final WorldInventories worldInventories;
    private final WorldGroupManager worldGroupManager;
    private final InventoriesConfig inventoriesConfig;
    private final ProfileContainerStoreProvider profileContainerStoreProvider;
    private final ProfileDataSource profileDataSource;

    WorldInventoriesImportHelper(
            @NotNull WorldInventories worldInventories,
            @NotNull WorldGroupManager worldGroupManager,
            @NotNull InventoriesConfig inventoriesConfig,
            @NotNull ProfileContainerStoreProvider profileContainerStoreProvider,
            @NotNull ProfileDataSource profileDataSource) {
        super();
        this.worldInventories = worldInventories;
        this.worldGroupManager = worldGroupManager;
        this.inventoriesConfig = inventoriesConfig;
        this.profileContainerStoreProvider = profileContainerStoreProvider;
        this.profileDataSource = profileDataSource;
    }

    void importData() throws DataImportException {
        List<Group> wiGroups;
        try {
            wiGroups = worldInventories.getGroups();
        } catch (Exception e) {
            throw new DataImportException("Unable to import from this version of WorldInventories!")
                    .setCauseException(e);
        } catch (Error e) {
            throw new DataImportException("Unable to import from this version of WorldInventories!");
        }
        if (wiGroups == null) {
            throw new DataImportException("No data to import from WorldInventories!");
        }

        if (!wiGroups.isEmpty()) {
            WorldGroup defaultWorldGroup = worldGroupManager.getDefaultGroup();
            if (defaultWorldGroup != null) {
                worldGroupManager.removeGroup(defaultWorldGroup);
                Logging.info("Removed automatically created world group in favor of imported groups.");
            }
        }

        this.createGroups(wiGroups);
        Set<ProfileContainer> noGroupWorlds = this.getWorldsWithoutGroups();
        inventoriesConfig.save();

        OfflinePlayer[] offlinePlayers = Bukkit.getServer().getOfflinePlayers();
        Logging.info("Processing data for " + offlinePlayers.length + " players.  The larger than number, the longer"
                + " this process will take.  Please be patient. :)  Your server will freeze for the duration.");
        int playerCount = 0;
        for (OfflinePlayer player : offlinePlayers) {
            playerCount++;
            Logging.finer("(" + playerCount + "/" + offlinePlayers.length
                    + ")Processing WorldInventories data for player: " + player.getName());
            for (Group wiGroup : wiGroups) {
                WorldGroup worldGroup = worldGroupManager.getGroup(wiGroup.getName());
                if (worldGroup == null) {
                    Logging.finest("Could not import player data for WorldInventories group: " + wiGroup.getName()
                            + " because there is no Multiverse-Inventories group by that name.");
                    continue;
                }
                this.transferData(player, wiGroup, worldGroup.getGroupProfileContainer());
            }
            for (ProfileContainer container : noGroupWorlds) {
                this.transferData(player, null, container);
            }
        }
    }

    private void createGroups(List<Group> wiGroups) {
        for (Group wiGroup : wiGroups) {
            if (wiGroup.getWorlds().isEmpty()) {
                Logging.warning("Group '" + wiGroup.getName() + "' has no worlds."
                        + "  You may need to add these manually!");
            }
            WorldGroup newGroup = worldGroupManager.newEmptyGroup(wiGroup.getName());
            for (String worldName : wiGroup.getWorlds()) {
                newGroup.addWorld(worldName);
            }

            try {
                if (WorldInventories.doStats) {
                    newGroup.getShares().mergeShares(Sharables.allOf());
                } else {
                    newGroup.getShares().setSharing(Sharables.ALL_INVENTORY, true);
                }
            } catch (Exception ignore) {
                Logging.warning("Group '" + wiGroup.getName() + "' unable to import fully, sharing only inventory.");
                newGroup.getShares().setSharing(Sharables.ALL_INVENTORY, true);
            } catch (Error e) {
                Logging.warning("Group '" + wiGroup.getName() + "' unable to import fully, sharing only inventory.");
                newGroup.getShares().setSharing(Sharables.ALL_INVENTORY, true);
            }
            worldGroupManager.updateGroup(newGroup);
            Logging.info("Created Multiverse-Inventories group: " + wiGroup.getName());
        }
    }

    private Set<ProfileContainer> getWorldsWithoutGroups() {
        Set<ProfileContainer> noGroupWorlds = new LinkedHashSet<>();
        for (World world : Bukkit.getWorlds()) {
            if (worldGroupManager.getGroupsForWorld(world.getName()).isEmpty()) {
                Logging.fine("Added ungrouped world for importing.");
                ProfileContainer container = profileContainerStoreProvider.getStore(ContainerType.WORLD)
                        .getContainer(world.getName());
                noGroupWorlds.add(container);
            }
        }
        return noGroupWorlds;
    }

    private void transferData(OfflinePlayer player, Group wiGroup, ProfileContainer profileContainer) {
        PlayerProfile playerProfile = profileContainer.getPlayerData(ProfileTypes.SURVIVAL, player);
        WIPlayerInventory wiInventory = this.loadPlayerInventory(player, wiGroup);
        WIPlayerStats wiStats = this.loadPlayerStats(player, wiGroup);
        if (wiInventory != null) {
            playerProfile.set(Sharables.INVENTORY, wiInventory.getItems());
            playerProfile.set(Sharables.ARMOR, wiInventory.getArmour());
        }
        if (wiStats != null) {
            playerProfile.set(Sharables.HEALTH, (double) wiStats.getHealth());
            playerProfile.set(Sharables.SATURATION, wiStats.getSaturation());
            playerProfile.set(Sharables.EXPERIENCE, wiStats.getExp());
            playerProfile.set(Sharables.LEVEL, wiStats.getLevel());
            playerProfile.set(Sharables.EXHAUSTION, wiStats.getExhaustion());
            playerProfile.set(Sharables.FOOD_LEVEL, wiStats.getFoodLevel());
        }
        profileDataSource.updatePlayerData(playerProfile);
        Logging.finest("Player's data imported successfully for group: " + profileContainer.getContainerName());
    }

    private File getFile(OfflinePlayer player, Group group, DataType dataType) {
        StringBuilder path = new StringBuilder();
        path.append(File.separator);

        // Use default group
        if (group == null) {
            path.append("default");
        } else {
            path.append(group.getName());
        }
        path.insert(0, worldInventories.getDataFolder().getAbsolutePath());
        path.append(File.separator).append(player.getName()).append(dataType.fileExtension);

        File file = new File(path.toString());
        if (!file.exists()) {
            file = null;
        }
        return file;
    }

    // Copied and modified from WorldInventories
    private WIPlayerInventory loadPlayerInventory(OfflinePlayer player, Group group) {
        File file = this.getFile(player, group, DataType.INVENTORY);
        if (file == null) {
            return null;
        }
        WIPlayerInventory playerInventory = null;
        FileInputStream fIS = null;
        ObjectInputStream obIn = null;
        try {
            fIS = new FileInputStream(file);
            obIn = new ObjectInputStream(fIS);
            playerInventory = (WIPlayerInventory) obIn.readObject();
        } catch (Exception ignore) {
        } finally {
            if (obIn != null) {
                try {
                    obIn.close();
                } catch (IOException ignore) {
                }
            }
            if (fIS != null) {
                try {
                    fIS.close();
                } catch (IOException ignore) {
                }
            }
        }

        return playerInventory;
    }

    // Copied and modified from WorldInventories
    private WIPlayerStats loadPlayerStats(OfflinePlayer player, Group group) {
        File file = this.getFile(player, group, DataType.STATS);
        if (file == null) {
            return null;
        }
        WIPlayerStats playerstats = null;
        FileInputStream fIS = null;
        ObjectInputStream obIn = null;
        try {
            fIS = new FileInputStream(file);
            obIn = new ObjectInputStream(fIS);
            playerstats = (WIPlayerStats) obIn.readObject();
        } catch (Exception ignore) {
        } finally {
            if (obIn != null) {
                try {
                    obIn.close();
                } catch (IOException ignore) {
                }
            }
            if (fIS != null) {
                try {
                    fIS.close();
                } catch (IOException ignore) {
                }
            }
        }

        return playerstats;
    }


    /**
     * Indicates the type of data we're importing for.
     */
    private enum DataType {
        INVENTORY(".inventory"),
        STATS(".stats");

        private String fileExtension;

        DataType(String fileExtension) {
            this.fileExtension = fileExtension;
        }
    }
}
