package com.onarandombox.multiverseinventories.dataimport.perworldinventory;

import com.dumptruckman.bukkit.configuration.util.SerializationHelper;
import com.dumptruckman.minecraft.util.Logging;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.WorldGroup;
import com.onarandombox.multiverseinventories.dataimport.DataImportException;
import com.onarandombox.multiverseinventories.profile.ProfileTypes;
import com.onarandombox.multiverseinventories.profile.container.ContainerType;
import com.onarandombox.multiverseinventories.share.Sharables;
import me.ebonjaeger.perworldinventory.Group;
import me.ebonjaeger.perworldinventory.GroupManager;
import me.ebonjaeger.perworldinventory.api.PerWorldInventoryAPI;
import me.ebonjaeger.perworldinventory.configuration.PlayerSettings;
import me.ebonjaeger.perworldinventory.configuration.PluginSettings;
import me.ebonjaeger.perworldinventory.configuration.Settings;
import me.ebonjaeger.perworldinventory.data.FlatFile;
import me.ebonjaeger.perworldinventory.data.PlayerProfile;
import me.ebonjaeger.perworldinventory.data.ProfileKey;
import me.ebonjaeger.perworldinventory.data.ProfileManager;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

class PwiImportHelper {

    private static final JSONParser PARSER = new JSONParser(JSONParser.USE_INTEGER_STORAGE);

    private final MultiverseInventories plugin;
    private final PerWorldInventoryAPI pwiAPI;

    private Settings pwiSettings;
    private GroupManager pwiGroupManager;
    private ProfileManager pwiProfileManager;
    private FlatFile pwiFlatFile;
    private Method getFileMethod;

    PwiImportHelper(MultiverseInventories plugin, PerWorldInventoryAPI pwiAPI) {
        this.plugin = plugin;
        this.pwiAPI = pwiAPI;
    }

    /**
     * The 'Main' method for the import.
     */
    void importData() throws DataImportException {
        pwiSetUp();
        transferConfigOptions();

        // Since there is no such thing as individual world container in PerWorldInventory,
        // everything is just groups. No need for world playerData import.
        for (Group group : getPWIGroups()) {
            createMVGroup(group);
            saveMVDataForGroup(group);
        }
    }

    /**
     * Do the necessary reflection to get access to the classes needed for data import.
     */
    private void pwiSetUp() throws DataImportException {
        this.pwiSettings = PwiReflect.getFieldFromClass(this.pwiAPI, "settings");
        this.pwiGroupManager = PwiReflect.getFieldFromClass(this.pwiAPI, "groupManager");
        this.pwiProfileManager = PwiReflect.getFieldFromClass(this.pwiAPI, "profileManager");
        this.pwiFlatFile = PwiReflect.getFieldFromClass(this.pwiProfileManager, "dataSource");
        this.getFileMethod = PwiReflect.getMethodFromClass(this.pwiFlatFile, "getFile", ProfileKey.class);
    }

    /**
     * Set similar/supported config options in MultiverseInventories with the values used in PerWorldInventory.
     */
    private void transferConfigOptions() {
        this.plugin.getMVIConfig().setUsingGameModeProfiles(this.pwiSettings.getProperty(PluginSettings.SEPARATE_GM_INVENTORIES));
        this.plugin.getMVIConfig().setUsingLoggingSaveLoad(this.pwiSettings.getProperty(PluginSettings.LOAD_DATA_ON_JOIN));
        this.plugin.getMVIConfig().setDefaultingUngroupedWorlds(this.pwiSettings.getProperty(PluginSettings.SHARE_IF_UNCONFIGURED));
        this.plugin.getMVIConfig().getOptionalShares().setSharing(Sharables.ECONOMY, this.pwiSettings.getProperty(PlayerSettings.USE_ECONOMY));
        this.plugin.getMVIConfig().save();
    }

    /**
     * Gets all PerWorldInventory groups based on all the worlds known by Multiverse.
     *
     * @return A collection of PerWorldInventory groups.
     */
    private Collection<Group> getPWIGroups() {
        Set<Group> groups = new HashSet<>(this.pwiGroupManager.getGroups().values());
        for (MultiverseWorld world : this.plugin.getCore().getMVWorldManager().getMVWorlds()) {
            groups.add(this.pwiGroupManager.getGroupFromWorld(world.getName()));
        }
        for (String world : this.plugin.getCore().getMVWorldManager().getUnloadedWorlds()) {
            groups.add(this.pwiGroupManager.getGroupFromWorld(world));
        }
        return groups;
    }

    /**
     * Create a MultiverseInventories {@link WorldGroup} based on PerWorldInventory Group.
     *
     * @param group A PerWorldInventory Group.
     */
    private void createMVGroup(Group group) throws DataImportException {
        Logging.finer("PerWorldInventory Group: %s", group);
        WorldGroup worldGroup = this.plugin.getGroupManager().getGroup(group.getName());
        if (worldGroup == null) {
            worldGroup = this.plugin.getGroupManager().newEmptyGroup(group.getName());
        }

        // In PerWorldInventory, shares can only be toggled to be enabled or disabled globally.
        // So setting all shares here then transferring only those enabled later should work enough,
        // since you can't actually disable shares in MultiverseInventories.
        worldGroup.getShares().addAll(Sharables.allOf());
        worldGroup.addWorlds(group.getWorlds());
        if (group.getRespawnWorld() != null) {
            worldGroup.setSpawnWorld(group.getRespawnWorld());
        }
        this.plugin.getGroupManager().updateGroup(worldGroup);
    }

    /**
     * Transfer all player data from PerWorldInventory to MultiverseInventories for a given group.
     *
     * @param group A PerWorldInventory Group.
     */
    private void saveMVDataForGroup(Group group) throws DataImportException {
        for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
            for (GameMode gameMode : GameMode.values()) {
                ProfileKey pwiKey = new ProfileKey(offlinePlayer.getUniqueId(), group, gameMode);
                transferToMVPlayerData(getMVPlayerData(pwiKey), getPWIPlayerData(pwiKey));
            }
        }
    }

    /**
     * Gets MultiverseInventories PlayerProfile based on PerWorldInventory ProfileKey.
     *
     * @param pwiKey    PerWorldInventory profile key.
     * @return A MultiverseInventories PLayerProfile.
     */
    private com.onarandombox.multiverseinventories.profile.PlayerProfile getMVPlayerData(ProfileKey pwiKey) {
        return this.plugin.getData().getPlayerData(
                ContainerType.GROUP,
                pwiKey.getGroup().getName(),
                ProfileTypes.forGameMode(pwiKey.getGameMode()),
                pwiKey.getUuid()
        );
    }

    /**
     * Gets PerWorldInventory PlayerProfile based on PerWorldInventory ProfileKey.
     *
     * @param pwiKey    PerWorldInventory profile key.
     * @return A PerWorldInventory PLayerProfile.
     */
    private PlayerProfile getPWIPlayerData(ProfileKey pwiKey) throws DataImportException {
        File pwiPlayerDataFile = getPWIFile(pwiKey);
        if (!pwiPlayerDataFile.isFile()) {
            Logging.finer("No data for %s.",  pwiKey.toString());
            return null;
        }

        PlayerProfile pwiPlayerProfile;
        try {
            JSONObject jsonObject = (JSONObject) PARSER.parse(new FileInputStream(pwiPlayerDataFile));
            pwiPlayerProfile = (PlayerProfile) SerializationHelper.deserialize(jsonObject);
        } catch (Exception e) {
            e.printStackTrace();
            throw new DataImportException("Unable to parse file into profile: " + pwiPlayerDataFile.getAbsolutePath());
        }

        if (pwiPlayerProfile == null) {
            Logging.warning("Empty serialization for %s.",  pwiKey.toString());
            return null;
        }
        Logging.finer("Got pwiPlayerProfile for %s.",  pwiKey.toString());
        return pwiPlayerProfile;
    }

    /**
     * Gets a PerWorldInventory data file based on it's ProfileKey.
     *
     * @param pwiKey    PerWorldInventory profile key.
     * @return A PerWorldInventory data file.
     */
    private File getPWIFile(ProfileKey pwiKey) throws DataImportException {
        return PwiReflect.invokeMethod(this.pwiFlatFile, this.getFileMethod, pwiKey);
    }

    /**
     * Transfers supported player data from PerWorldInventory to MultiverseInventories.
     *
     * @param mvPlayerProfile   MultiverseInventories PlayerProfile to transfer to.
     * @param pwiPlayerProfile  PerWorldInventory PlayerProfile to transfer from.
     */
    private void transferToMVPlayerData(com.onarandombox.multiverseinventories.profile.PlayerProfile mvPlayerProfile,
                                        PlayerProfile pwiPlayerProfile) throws DataImportException {

        if (pwiPlayerProfile == null || mvPlayerProfile == null) {
            Logging.finer("Null profile(s). No data transferred for %s and %s.", mvPlayerProfile, pwiPlayerProfile);
            return;
        }

        // Move data from PerWorldInventory profile to MultiverseInventories profile
        // Shares that are not available are commented out.
        if (pwiSettings.getProperty(PlayerSettings.LOAD_INVENTORY)) {
            mvPlayerProfile.set(Sharables.ARMOR, pwiPlayerProfile.getArmor());
            mvPlayerProfile.set(Sharables.INVENTORY, pwiPlayerProfile.getInventory());
        }
        if (pwiSettings.getProperty(PlayerSettings.USE_ECONOMY)) {
            mvPlayerProfile.set(Sharables.ECONOMY, pwiPlayerProfile.getBalance());
        }
        if (pwiSettings.getProperty(PlayerSettings.LOAD_ENDER_CHEST)) {
            mvPlayerProfile.set(Sharables.ENDER_CHEST, pwiPlayerProfile.getEnderChest());
        }
        if (pwiSettings.getProperty(PlayerSettings.LOAD_EXHAUSTION)) {
            mvPlayerProfile.set(Sharables.EXHAUSTION, pwiPlayerProfile.getExhaustion());
        }
        if (pwiSettings.getProperty(PlayerSettings.LOAD_EXP)) {
            mvPlayerProfile.set(Sharables.EXPERIENCE, pwiPlayerProfile.getExperience());
        }
        if (pwiSettings.getProperty(PlayerSettings.LOAD_FALL_DISTANCE)) {
            mvPlayerProfile.set(Sharables.FALL_DISTANCE, pwiPlayerProfile.getFallDistance());
        }
        if (pwiSettings.getProperty(PlayerSettings.LOAD_FIRE_TICKS)) {
            mvPlayerProfile.set(Sharables.FIRE_TICKS, pwiPlayerProfile.getFireTicks());
        }
        if (pwiSettings.getProperty(PlayerSettings.LOAD_HUNGER)) {
            mvPlayerProfile.set(Sharables.FOOD_LEVEL, pwiPlayerProfile.getFoodLevel());
        }
        if (pwiSettings.getProperty(PlayerSettings.LOAD_HUNGER)) {
            mvPlayerProfile.set(Sharables.FOOD_LEVEL, pwiPlayerProfile.getFoodLevel());
        }
        if (pwiSettings.getProperty(PlayerSettings.LOAD_HEALTH)) {
            mvPlayerProfile.set(Sharables.HEALTH, pwiPlayerProfile.getHealth());
            // mvPlayerProfile.set(Sharables, pwiPlayerProfile.getMaxHealth());
        }
        if (pwiSettings.getProperty(PlayerSettings.LOAD_LEVEL)) {
            mvPlayerProfile.set(Sharables.LEVEL, pwiPlayerProfile.getLevel());
        }
        if (pwiSettings.getProperty(PlayerSettings.LOAD_MAX_AIR)) {
            mvPlayerProfile.set(Sharables.MAXIMUM_AIR, pwiPlayerProfile.getMaximumAir());
        }
        if (pwiSettings.getProperty(PlayerSettings.LOAD_POTION_EFFECTS)) {
            mvPlayerProfile.set(Sharables.POTIONS, pwiPlayerProfile.getPotionEffects().toArray(new PotionEffect[0]));
        }
        if (pwiSettings.getProperty(PlayerSettings.LOAD_REMAINING_AIR)) {
            mvPlayerProfile.set(Sharables.REMAINING_AIR, pwiPlayerProfile.getRemainingAir());
        }
        if (pwiSettings.getProperty(PlayerSettings.LOAD_SATURATION)) {
            mvPlayerProfile.set(Sharables.REMAINING_AIR, pwiPlayerProfile.getRemainingAir());
        }
        if (pwiSettings.getProperty(PlayerSettings.LOAD_DISPLAY_NAME)) {
            // mvPlayerProfile.set(Sharables, pwiPlayerProfile.getDisplayName());
        }
        if (pwiSettings.getProperty(PlayerSettings.LOAD_FLYING)) {
            // mvPlayerProfile.set(Sharables, pwiPlayerProfile.getAllowFlight());
        }

        // mvPlayerProfile.set(Sharables.BED_SPAWN, pwiPlayerProfile);
        // mvPlayerProfile.set(Sharables.HUNGER, pwiPlayerProfile);
        // mvPlayerProfile.set(Sharables.LAST_LOCATION, pwiPlayerProfile);
        // mvPlayerProfile.set(Sharables.OFF_HAND, pwiPlayerProfile);
        // mvPlayerProfile.set(Sharables.TOTAL_EXPERIENCE, pwiPlayerProfile);

        this.plugin.getData().updatePlayerData(mvPlayerProfile);
    }
}
