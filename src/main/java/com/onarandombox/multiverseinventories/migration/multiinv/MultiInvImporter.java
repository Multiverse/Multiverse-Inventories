package com.onarandombox.multiverseinventories.migration.multiinv;

import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.migration.MigrationException;
import com.onarandombox.multiverseinventories.profile.PlayerProfile;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import uk.co.tggl.pluckerpluck.multiinv.MIYamlFiles;
import uk.co.tggl.pluckerpluck.multiinv.MultiInv;
import uk.co.tggl.pluckerpluck.multiinv.listener.MIPlayerListener;
import uk.co.tggl.pluckerpluck.multiinv.player.MIPlayer;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class MultiInvImporter {

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

    public void importData() throws MigrationException {
        HashMap<String, String> miGroupMap = this.getGroupMap();
        for (OfflinePlayer player : Bukkit.getServer().getOfflinePlayers()) {
            for (Map.Entry<String, String> entry : miGroupMap.entrySet()) {
                String worldName = entry.getKey();
                String groupName = entry.getValue();
                MIPlayerFileLoader playerFileLoader =
                        new MIPlayerFileLoader(this.miPlugin, player, groupName);
                if (!playerFileLoader.load()) {
                    continue;
                }
                PlayerProfile playerProfile = this.plugin.getProfileManager()
                        .getWorldProfile(worldName).getPlayerData(player);
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
                this.plugin.getData().updatePlayerData(worldName, playerProfile);
            }
        }
    }

    private HashMap<String, String> getGroupMap() throws MigrationException {
        Field field;
        try {
            field = MIYamlFiles.class.getDeclaredField("groups");
        } catch(NoSuchFieldException ignore) {
            throw new MigrationException("The running version of MultiInv is " +
                    "incompatible with the import feature.");
        }
        field.setAccessible(true);
        HashMap<String, String> miGroupMap = null;
        try {
            miGroupMap = (HashMap<String, String>) field.get(null);
        } catch (IllegalAccessException iae) {
            iae.printStackTrace();
        } catch (ClassCastException cce) {
            cce.printStackTrace();
        }
        return miGroupMap;
    }
}
