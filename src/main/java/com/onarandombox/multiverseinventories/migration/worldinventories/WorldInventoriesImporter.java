package com.onarandombox.multiverseinventories.migration.worldinventories;

import com.onarandombox.multiverseinventories.api.Inventories;
import com.onarandombox.multiverseinventories.api.profile.WorldGroupProfile;
import com.onarandombox.multiverseinventories.api.profile.PlayerProfile;
import com.onarandombox.multiverseinventories.migration.DataImporter;
import com.onarandombox.multiverseinventories.migration.MigrationException;
import com.onarandombox.multiverseinventories.api.share.Sharable;
import com.onarandombox.multiverseinventories.api.share.SimpleShares;
import com.onarandombox.multiverseinventories.util.MVILog;
import me.drayshak.WorldInventories.Group;
import me.drayshak.WorldInventories.WIPlayerInventory;
import me.drayshak.WorldInventories.WIPlayerStats;
import me.drayshak.WorldInventories.WorldInventories;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;

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
                MVILog.info("Removed automatically created world group in favor of imported groups.");
            }
        }
        for (Group wiGroup : wiGroups) {
            if (wiGroup.getWorlds().isEmpty()) {
                MVILog.warning("Group '" + wiGroup.getName() + "' has no worlds.  It will not be imported!");
                continue;
            }
            WorldGroupProfile newGroup = this.inventories.getGroupManager().newEmptyGroup(wiGroup.getName());
            for (String worldName : wiGroup.getWorlds()) {
                newGroup.addWorld(worldName);
            }

            try {
                if (WorldInventories.doStats) {
                    newGroup.setShares(new SimpleShares(Sharable.all()));
                } else {
                    newGroup.getShares().setSharing(Sharable.INVENTORY, true);
                }
            } catch (Exception ignore) {
                MVILog.warning("Group '" + wiGroup.getName() + "' unable to import fully, sharing only inventory.");
                newGroup.getShares().setSharing(Sharable.INVENTORY, true);
            } catch (Error e) {
                MVILog.warning("Group '" + wiGroup.getName() + "' unable to import fully, sharing only inventory.");
                newGroup.getShares().setSharing(Sharable.INVENTORY, true);
            }
            this.inventories.getGroupManager().addGroup(newGroup, true);
            MVILog.info("Imported group: " + wiGroup.getName());
        }
        this.inventories.getMVIConfig().save();
        for (OfflinePlayer player : Bukkit.getServer().getOfflinePlayers()) {
            MVILog.info("Processing WorldInventories data for player: " + player.getName());
            for (Group wiGroup : wiGroups) {
                WorldGroupProfile worldGroup = this.inventories.getGroupManager().getGroup(wiGroup.getName());
                if (worldGroup == null) {
                    MVILog.warning("Could not import player data for group: " + wiGroup.getName());
                    continue;
                }
                WIPlayerInventory wiInventory = this.loadPlayerInventory(player, wiGroup);
                if (wiInventory == null) {
                    continue;
                }
                WIPlayerStats wiStats = this.loadPlayerStats(player, wiGroup);
                if (wiStats == null) {
                    continue;
                }
                PlayerProfile playerProfile = worldGroup.getPlayerData(player);
                playerProfile.setInventoryContents(wiInventory.getItems());
                playerProfile.setArmorContents(wiInventory.getArmour());
                playerProfile.setHealth(wiStats.getHealth());
                playerProfile.setSaturation(wiStats.getSaturation());
                playerProfile.setExp(wiStats.getExp());
                playerProfile.setLevel(wiStats.getLevel());
                playerProfile.setExhaustion(wiStats.getExhaustion());
                playerProfile.setFoodLevel(wiStats.getFoodLevel());
                this.inventories.getData().updatePlayerData(worldGroup.getDataName(), playerProfile);
                MVILog.info("Player's data imported successfully from group: " + wiGroup.getName());
            }
        }

        MVILog.info("Import from WorldInventories finished.  Disabling WorldInventories.");
        Bukkit.getPluginManager().disablePlugin(this.getWIPlugin());
    }

    // Copied and modified from WorldInventories
    private WIPlayerInventory loadPlayerInventory(OfflinePlayer player, Group group) {
        WIPlayerInventory playerInventory = null;
        FileInputStream fIS = null;
        ObjectInputStream obIn = null;

        String path = File.separator;

        // Use default group
        if (group == null) {
            path += "default";
        } else {
            path += group.getName();
        }

        path = this.getWIPlugin().getDataFolder().getAbsolutePath() + path;
        File file = new File(path);
        if (!file.exists()) {
            return null;
        }

        path += File.separator + player.getName() + ".inventory";
        try {
            fIS = new FileInputStream(path);
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
        WIPlayerStats playerstats = null;
        FileInputStream fIS = null;
        ObjectInputStream obIn = null;

        String path = File.separator;

        // Use default group
        if (group == null) {
            path += "default";
        } else {
            path += group.getName();
        }

        path = this.getWIPlugin().getDataFolder().getAbsolutePath() + path;
        File file = new File(path);
        if (!file.exists()) {
            return null;
        }

        path += File.separator + player.getName() + ".stats";

        try {
            fIS = new FileInputStream(path);
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
}

