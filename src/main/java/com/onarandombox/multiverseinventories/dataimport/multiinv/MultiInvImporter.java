package com.onarandombox.multiverseinventories.dataimport.multiinv;

import com.dumptruckman.minecraft.util.Logging;
import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.WorldGroup;
import com.onarandombox.multiverseinventories.dataimport.AbstractDataImporter;
import com.onarandombox.multiverseinventories.dataimport.DataImportException;
import com.onarandombox.multiverseinventories.profile.PlayerProfile;
import com.onarandombox.multiverseinventories.profile.ProfileTypes;
import com.onarandombox.multiverseinventories.profile.container.ContainerType;
import com.onarandombox.multiverseinventories.share.Sharables;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import uk.co.tggl.pluckerpluck.multiinv.MIYamlFiles;
import uk.co.tggl.pluckerpluck.multiinv.MultiInv;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class MultiInvImporter extends AbstractDataImporter<MultiInv> {

    public MultiInvImporter(MultiverseInventories plugin) {
        super(plugin);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doDataImport() throws DataImportException {
        HashMap<String, String> miGroupMap = this.getGroupMap();
        if (miGroupMap == null) {
            throw new DataImportException("There is no data to import from MultiInv!");
        }
        if (!miGroupMap.isEmpty()) {
            WorldGroup defaultWorldGroup = this.plugin.getGroupManager().getDefaultGroup();
            if (defaultWorldGroup != null) {
                this.plugin.getGroupManager().removeGroup(defaultWorldGroup);
                Logging.info("Removed automatically created world group in favor of imported groups.");
            }
        }
        for (Map.Entry<String, String> groupEntry : miGroupMap.entrySet()) {
            WorldGroup worldGroup = this.plugin.getGroupManager().getGroup(groupEntry.getValue());
            if (worldGroup == null) {
                worldGroup = this.plugin.getGroupManager().newEmptyGroup(groupEntry.getValue());
                worldGroup.getShares().mergeShares(Sharables.allOf());
                Logging.info("Importing group: " + groupEntry.getValue());
                this.plugin.getGroupManager().updateGroup(worldGroup);
            }
            worldGroup.addWorld(groupEntry.getValue());
        }
        this.plugin.getMVIConfig().save();

        for (OfflinePlayer player : Bukkit.getServer().getOfflinePlayers()) {
            Logging.info("Processing MultiInv data for player: " + player.getName());
            for (Map.Entry<String, String> entry : miGroupMap.entrySet()) {
                String worldName = entry.getKey();
                String groupName = entry.getValue();
                MIPlayerFileLoader playerFileLoader = new MIPlayerFileLoader(this.importer, player, groupName);
                if (!playerFileLoader.load()) {
                    continue;
                }
                Logging.info("Processing MultiInv data for player: " + player.getName()
                        + " for group: " + groupName);
                mergeData(player, playerFileLoader, groupName, ContainerType.GROUP);
            }
            for (World world : Bukkit.getWorlds()) {
                String worldName = world.getName();
                MIPlayerFileLoader playerFileLoader = new MIPlayerFileLoader(this.importer, player, worldName);
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
            WorldGroup group = this.plugin.getGroupManager()
                    .getGroup(dataName);
            if (group == null) {
                Logging.warning("Could not import player data for group: " + dataName);
                return;
            }
            playerProfile = group.getGroupProfileContainer().getPlayerData(ProfileTypes.SURVIVAL, player);
        } else {
            playerProfile = this.plugin.getWorldProfileContainerStore()
                    .getContainer(dataName).getPlayerData(ProfileTypes.SURVIVAL, player);
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
        this.plugin.getData().updatePlayerData(playerProfile);
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
        } catch (IllegalAccessException iae) {
            throw new DataImportException("The running version of MultiInv is "
                    + "incompatible with the import feature.").setCauseException(iae);
        } catch (ClassCastException cce) {
            throw new DataImportException("The running version of MultiInv is "
                    + "incompatible with the import feature.").setCauseException(cce);
        }
        return miGroupMap;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull String getPluginName() {
        return "MultiInv";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull Class<MultiInv> getPluginClass() {
        return MultiInv.class;
    }
}
