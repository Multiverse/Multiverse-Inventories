package com.onarandombox.multiverseinventories.migration.worldinventories;

import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.migration.DataImporter;
import com.onarandombox.multiverseinventories.migration.MigrationException;
import com.onarandombox.multiverseinventories.profile.PlayerProfile;
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
    private MultiverseInventories plugin;

    public WorldInventoriesImporter(MultiverseInventories plugin, WorldInventories wiPlugin) {
        this.plugin = plugin;
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
        }
        for (OfflinePlayer player : Bukkit.getServer().getOfflinePlayers()) {
            MVILog.info("Processing WorldInventories data for player: " + player.getName());
            for (Group wiGroup : wiGroups) {
                WIPlayerInventory wiInventory = this.loadPlayerInventory(player, wiGroup);
                if (wiInventory == null) {
                    continue;
                }
                WIPlayerStats wiStats = this.loadPlayerStats(player, wiGroup);
                if (wiStats == null) {
                    continue;
                }
                for (String worldName : wiGroup.getWorlds()) {
                    PlayerProfile playerProfile = this.plugin.getProfileManager()
                            .getWorldProfile(worldName).getPlayerData(player);
                    playerProfile.setInventoryContents(wiInventory.getItems());
                    playerProfile.setArmorContents(wiInventory.getArmour());
                    playerProfile.setHealth(wiStats.getHealth());
                    playerProfile.setSaturation(wiStats.getSaturation());
                    playerProfile.setExp(wiStats.getExp());
                    playerProfile.setLevel(wiStats.getLevel());
                    playerProfile.setExhaustion(wiStats.getExhaustion());
                    playerProfile.setFoodLevel(wiStats.getFoodLevel());
                    this.plugin.getData().updatePlayerData(worldName, playerProfile);
                }
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
