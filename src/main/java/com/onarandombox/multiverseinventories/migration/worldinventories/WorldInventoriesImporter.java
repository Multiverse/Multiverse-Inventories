package com.onarandombox.multiverseinventories.migration.worldinventories;

import com.dumptruckman.minecraft.util.Logging;
import com.onarandombox.multiverseinventories.ProfileTypes;
import com.onarandombox.multiverseinventories.api.Inventories;
import com.onarandombox.multiverseinventories.api.profile.PlayerProfile;
import com.onarandombox.multiverseinventories.api.profile.ProfileContainer;
import com.onarandombox.multiverseinventories.api.profile.WorldGroupProfile;
import com.onarandombox.multiverseinventories.api.profile.WorldProfile;
import com.onarandombox.multiverseinventories.api.share.Sharables;
import com.onarandombox.multiverseinventories.migration.DataImporter;
import com.onarandombox.multiverseinventories.migration.MigrationException;
import me.drayshak.WorldInventories.Group;
import me.drayshak.WorldInventories.WIPlayerInventory;
import me.drayshak.WorldInventories.WIPlayerStats;
import me.drayshak.WorldInventories.WorldInventories;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Handles the importing of data from WorldInventories.
 */
public class WorldInventoriesImporter implements DataImporter {

    private WorldInventories wiPlugin;
    private Inventories inventories;

    public WorldInventoriesImporter(Inventories inventories, WorldInventories wiPlugin) {
        this.inventories = inventories;
        this.wiPlugin = wiPlugin;
    }

    /**
     * @return The WorldInventories plugin hooked to the importer.
     */
    public WorldInventories getWIPlugin() {
        return this.wiPlugin;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Plugin getPlugin() {
        return this.getWIPlugin();
    }

    /**
     * Imports the data from WorldInventories into MultiverseInventories.
     *
     * @throws MigrationException If there was any MAJOR issues importing the data.
     */
    @Override
    public void importData() throws MigrationException {
        List<Group> wiGroups;
        try {
            wiGroups = this.getWIPlugin().getGroups();
        } catch (Exception e) {
            throw new MigrationException("Unable to import from this version of WorldInventories!")
                    .setCauseException(e);
        } catch (Error e) {
            throw new MigrationException("Unable to import from this version of WorldInventories!");
        }
        if (wiGroups == null) {
            throw new MigrationException("No data to import from WorldInventories!");
        }

        if (!wiGroups.isEmpty()) {
            WorldGroupProfile defaultWorldGroup = this.inventories.getGroupManager().getDefaultGroup();
            if (defaultWorldGroup != null) {
                this.inventories.getGroupManager().removeGroup(defaultWorldGroup);
                Logging.info("Removed automatically created world group in favor of imported groups.");
            }
        }

        this.createGroups(wiGroups);
        Set<WorldProfile> noGroupWorlds = this.getWorldsWithoutGroups();
        this.inventories.getMVIConfig().save();

        OfflinePlayer[] offlinePlayers = Bukkit.getServer().getOfflinePlayers();
        Logging.info("Processing data for " + offlinePlayers.length + " players.  The larger than number, the longer"
                + " this process will take.  Please be patient. :)  Your server will freeze for the duration.");
        int playerCount = 0;
        for (OfflinePlayer player : offlinePlayers) {
            playerCount++;
            Logging.finer("(" + playerCount + "/" + offlinePlayers.length
                    + ")Processing WorldInventories data for player: " + player.getName());
            for (Group wiGroup : wiGroups) {
                WorldGroupProfile worldGroup = this.inventories.getGroupManager().getGroup(wiGroup.getName());
                if (worldGroup == null) {
                    Logging.finest("Could not import player data for WorldInventories group: " + wiGroup.getName()
                            + " because there is no Multiverse-Inventories group by that name.");
                    continue;
                }
                this.transferData(player, wiGroup, worldGroup);
            }
            for (WorldProfile worldProfile : noGroupWorlds) {
                this.transferData(player, null, worldProfile);
            }
        }

        Logging.info("Import from WorldInventories finished.  Disabling WorldInventories.");
        Bukkit.getPluginManager().disablePlugin(this.getWIPlugin());
    }

    private void createGroups(List<Group> wiGroups) {
        for (Group wiGroup : wiGroups) {
            if (wiGroup.getWorlds().isEmpty()) {
                Logging.warning("Group '" + wiGroup.getName() + "' has no worlds."
                        + "  You may need to add these manually!");
            }
            WorldGroupProfile newGroup = this.inventories.getGroupManager().newEmptyGroup(wiGroup.getName());
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
            this.inventories.getGroupManager().updateGroup(newGroup);
            Logging.info("Created Multiverse-Inventories group: " + wiGroup.getName());
        }
    }

    private Set<WorldProfile> getWorldsWithoutGroups() {
        Set<WorldProfile> noGroupWorlds = new LinkedHashSet<WorldProfile>();
        for (World world : Bukkit.getWorlds()) {
            if (this.inventories.getGroupManager().getGroupsForWorld(world.getName()).isEmpty()) {
                Logging.fine("Added ungrouped world for importing.");
                WorldProfile worldProfile = this.inventories.getWorldManager().getWorldProfile(world.getName());
                noGroupWorlds.add(worldProfile);
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
        this.inventories.getData().updatePlayerData(playerProfile);
        Logging.finest("Player's data imported successfully for group: " + profileContainer.getDataName());
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
        path.insert(0, this.getWIPlugin().getDataFolder().getAbsolutePath());
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

