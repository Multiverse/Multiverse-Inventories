package com.onarandombox.multiverseinventories.migration.multiinv;

import com.onarandombox.multiverseinventories.ProfileTypes;
import com.onarandombox.multiverseinventories.api.Inventories;
import com.onarandombox.multiverseinventories.api.profile.ContainerType;
import com.onarandombox.multiverseinventories.api.profile.PlayerProfile;
import com.onarandombox.multiverseinventories.api.profile.WorldGroupProfile;
import com.onarandombox.multiverseinventories.api.share.Sharables;
import com.onarandombox.multiverseinventories.migration.DataImporter;
import com.onarandombox.multiverseinventories.migration.MigrationException;
import com.onarandombox.multiverseinventories.util.Logging;
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
    private Inventories inventories;

    public MultiInvImporter(Inventories inventories, MultiInv miPlugin) {
        this.inventories = inventories;
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
            WorldGroupProfile defaultWorldGroup = this.inventories.getGroupManager().getDefaultGroup();
            if (defaultWorldGroup != null) {
                this.inventories.getGroupManager().removeGroup(defaultWorldGroup);
                Logging.info("Removed automatically created world group in favor of imported groups.");
            }
        }
        for (Map.Entry<String, String> groupEntry : miGroupMap.entrySet()) {
            WorldGroupProfile worldGroup = this.inventories.getGroupManager().getGroup(groupEntry.getValue());
            if (worldGroup == null) {
                worldGroup = this.inventories.getGroupManager().newEmptyGroup(groupEntry.getValue());
                worldGroup.getShares().mergeShares(Sharables.allOf());
                Logging.info("Importing group: " + groupEntry.getValue());
                this.inventories.getGroupManager().addGroup(worldGroup, false);
            }
            worldGroup.addWorld(groupEntry.getValue());
        }
        this.inventories.getMVIConfig().save();

        for (OfflinePlayer player : Bukkit.getServer().getOfflinePlayers()) {
            Logging.info("Processing MultiInv data for player: " + player.getName());
            for (Map.Entry<String, String> entry : miGroupMap.entrySet()) {
                String worldName = entry.getKey();
                String groupName = entry.getValue();
                MIPlayerFileLoader playerFileLoader =
                        new MIPlayerFileLoader(this.getMIPlugin(), player, groupName);
                if (!playerFileLoader.load()) {
                    continue;
                }
                Logging.info("Processing MultiInv data for player: " + player.getName()
                        + " for group: " + groupName);
                mergeData(player, playerFileLoader, groupName, ContainerType.GROUP);
            }
            for (World world : Bukkit.getWorlds()) {
                String worldName = world.getName();
                MIPlayerFileLoader playerFileLoader =
                        new MIPlayerFileLoader(this.getMIPlugin(), player, worldName);
                if (!playerFileLoader.load()) {
                    continue;
                }
                Logging.info("Processing MultiInv data for player: " + player.getName()
                        + " for world only: " + worldName);
                mergeData(player, playerFileLoader, worldName, ContainerType.WORLD);
            }
        }

        Logging.info("Import from MultiInv finished.  Disabling MultiInv.");
        Bukkit.getPluginManager().disablePlugin(this.getMIPlugin());
    }

    private void mergeData(OfflinePlayer player, MIPlayerFileLoader playerFileLoader,
                           String dataName, ContainerType type) {
        PlayerProfile playerProfile;
        if (type.equals(ContainerType.GROUP)) {
            WorldGroupProfile group = this.inventories.getGroupManager()
                    .getGroup(dataName);
            if (group == null) {
                Logging.warning("Could not import player data for group: " + dataName);
                return;
            }
            playerProfile = group.getPlayerData(ProfileTypes.SURVIVAL, player);
        } else {
            playerProfile = this.inventories.getWorldManager()
                    .getWorldProfile(dataName).getPlayerData(ProfileTypes.SURVIVAL, player);
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
        this.inventories.getData().updatePlayerData(playerProfile);
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

