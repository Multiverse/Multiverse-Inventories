package com.onarandombox.multiverseinventories.migration.multiinv;

import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.group.SimpleWorldGroup;
import com.onarandombox.multiverseinventories.group.WorldGroup;
import com.onarandombox.multiverseinventories.migration.DataImporter;
import com.onarandombox.multiverseinventories.migration.MigrationException;
import com.onarandombox.multiverseinventories.profile.PlayerProfile;
import com.onarandombox.multiverseinventories.profile.ProfileType;
import com.onarandombox.multiverseinventories.share.Sharable;
import com.onarandombox.multiverseinventories.share.SimpleShares;
import com.onarandombox.multiverseinventories.util.MVILog;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import uk.co.tggl.pluckerpluck.multiinv.MIYamlFiles;
import uk.co.tggl.pluckerpluck.multiinv.MultiInv;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * A class to help with importing data from MultiInv.
 */
public class MultiInvImporter implements DataImporter {

    private MultiInv miPlugin;
    private MultiverseInventories plugin;

    public MultiInvImporter(MultiverseInventories plugin, MultiInv miPlugin) {
        this.plugin = plugin;
        this.miPlugin = miPlugin;
    }

    /**
     * @return The MultiInv plugin hooked to the importer.
     */
    public MultiInv getMIPlugin() {
        return this.miPlugin;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Plugin getPlugin() {
        return this.getMIPlugin();
    }

    /**
     * Imports the data from MultiInv.
     *
     * @throws MigrationException If there was any MAJOR issue loading the data.
     */
    @Override
    public void importData() throws MigrationException {
        HashMap<String, String> miGroupMap = this.getGroupMap();
        if (miGroupMap == null) {
            throw new MigrationException("There is no data to import from MultiInv!");
        }
        if (!miGroupMap.isEmpty()) {
            WorldGroup defaultWorldGroup = this.plugin.getGroupManager().getDefaultGroup();
            if (defaultWorldGroup != null) {
                this.plugin.getGroupManager().removeGroup(defaultWorldGroup);
                MVILog.info("Removed automatically created world group in favor of imported groups.");
            }
        }
        for (Map.Entry<String, String> groupEntry : miGroupMap.entrySet()) {
            WorldGroup worldGroup = this.plugin.getGroupManager().getGroup(groupEntry.getValue());
            if (worldGroup == null) {
                worldGroup = new SimpleWorldGroup(this.plugin, groupEntry.getValue());
                worldGroup.setShares(new SimpleShares(Sharable.all()));
                MVILog.info("Importing group: " + groupEntry.getValue());
                this.plugin.getGroupManager().addGroup(worldGroup, false);
            }
            worldGroup.addWorld(groupEntry.getValue());
        }
        this.plugin.getSettings().save();

        for (OfflinePlayer player : Bukkit.getServer().getOfflinePlayers()) {
            MVILog.info("Processing MultiInv data for player: " + player.getName());
            for (Map.Entry<String, String> entry : miGroupMap.entrySet()) {
                String worldName = entry.getKey();
                String groupName = entry.getValue();
                MIPlayerFileLoader playerFileLoader =
                        new MIPlayerFileLoader(this.getMIPlugin(), player, groupName);
                if (!playerFileLoader.load()) {
                    continue;
                }
                MVILog.info("Processing MultiInv data for player: " + player.getName()
                        + " for group: " + groupName);
                mergeData(player, playerFileLoader, groupName, ProfileType.GROUP);
            }
            for (World world : Bukkit.getWorlds()) {
                String worldName = world.getName();
                MIPlayerFileLoader playerFileLoader =
                        new MIPlayerFileLoader(this.getMIPlugin(), player, worldName);
                if (!playerFileLoader.load()) {
                    continue;
                }
                MVILog.info("Processing MultiInv data for player: " + player.getName()
                        + " for world only: " + worldName);
                mergeData(player, playerFileLoader, worldName, ProfileType.WORLD);
            }
        }

        MVILog.info("Import from MultiInv finished.  Disabling MultiInv.");
        Bukkit.getPluginManager().disablePlugin(this.getMIPlugin());
    }

    private void mergeData(OfflinePlayer player, MIPlayerFileLoader playerFileLoader,
                           String dataName, ProfileType type) {
        PlayerProfile playerProfile;
        if (type.equals(ProfileType.GROUP)) {
            WorldGroup group = this.plugin.getGroupManager()
                    .getGroup(dataName);
            if (group == null) {
                MVILog.warning("Could not import player data for group: " + dataName);
                return;
            }
            playerProfile = group.getPlayerData(player);
        } else {
            playerProfile = this.plugin.getProfileManager()
                    .getWorldProfile(dataName).getPlayerData(player);
        }
        MIInventoryInterface inventoryInterface =
                playerFileLoader.getInventory(GameMode.SURVIVAL.toString());
        playerProfile.setInventoryContents(inventoryInterface.getInventoryContents());
        playerProfile.setArmorContents(inventoryInterface.getArmorContents());
        playerProfile.setHealth(playerFileLoader.getHealth());
        playerProfile.setSaturation(playerFileLoader.getSaturation());
        playerProfile.setExp(playerFileLoader.getExperience());
        playerProfile.setLevel(playerFileLoader.getLevel());
        playerProfile.setTotalExperience(playerFileLoader.getTotalExperience());
        playerProfile.setFoodLevel(playerFileLoader.getHunger());
        this.plugin.getData().updatePlayerData(dataName, playerProfile);
    }

    /**
     * @return The group mapping from MultiInv, where worldName -> groupName.
     * @throws MigrationException If there was any issues getting the data through reflection.
     */
    private HashMap<String, String> getGroupMap() throws MigrationException {
        Field field;
        try {
            field = MIYamlFiles.class.getDeclaredField("groups");
        } catch (NoSuchFieldException nsfe) {
            throw new MigrationException("The running version of MultiInv is "
                    + "incompatible with the import feature.").setCauseException(nsfe);
        }
        field.setAccessible(true);
        HashMap<String, String> miGroupMap = null;
        try {
            miGroupMap = (HashMap<String, String>) field.get(null);
        } catch (IllegalAccessException iae) {
            throw new MigrationException("The running version of MultiInv is "
                    + "incompatible with the import feature.").setCauseException(iae);
        } catch (ClassCastException cce) {
            throw new MigrationException("The running version of MultiInv is "
                    + "incompatible with the import feature.").setCauseException(cce);
        }
        return miGroupMap;
    }
}

