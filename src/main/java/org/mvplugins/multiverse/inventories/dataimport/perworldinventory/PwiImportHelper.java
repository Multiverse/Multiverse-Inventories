package org.mvplugins.multiverse.inventories.dataimport.perworldinventory;

import com.dumptruckman.bukkit.configuration.util.SerializationHelper;
import com.dumptruckman.minecraft.util.Logging;
import me.ebonjaeger.perworldinventory.Group;
import me.ebonjaeger.perworldinventory.GroupManager;
import me.ebonjaeger.perworldinventory.api.PerWorldInventoryAPI;
import me.ebonjaeger.perworldinventory.configuration.PlayerSettings;
import me.ebonjaeger.perworldinventory.configuration.PluginSettings;
import me.ebonjaeger.perworldinventory.configuration.Settings;
import me.ebonjaeger.perworldinventory.data.FlatFile;
import me.ebonjaeger.perworldinventory.data.ProfileKey;
import me.ebonjaeger.perworldinventory.data.ProfileManager;
import me.ebonjaeger.perworldinventory.libs.json.JSONObject;
import me.ebonjaeger.perworldinventory.libs.json.parser.JSONParser;
import me.ebonjaeger.perworldinventory.serialization.PlayerSerializer;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mvplugins.multiverse.core.utils.ReflectHelper;
import org.mvplugins.multiverse.core.world.WorldManager;
import org.mvplugins.multiverse.inventories.config.InventoriesConfig;
import org.mvplugins.multiverse.inventories.dataimport.DataImportException;
import org.mvplugins.multiverse.inventories.profile.GlobalProfile;
import org.mvplugins.multiverse.inventories.profile.PlayerProfile;
import org.mvplugins.multiverse.inventories.profile.ProfileDataSource;
import org.mvplugins.multiverse.inventories.profile.ProfileTypes;
import org.mvplugins.multiverse.inventories.profile.container.ContainerType;
import org.mvplugins.multiverse.inventories.profile.group.WorldGroup;
import org.mvplugins.multiverse.inventories.profile.group.WorldGroupManager;
import org.mvplugins.multiverse.inventories.share.Sharables;
import org.mvplugins.multiverse.inventories.util.PlayerStats;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

final class PwiImportHelper {

    private final PerWorldInventoryAPI pwiAPI;
    private final InventoriesConfig inventoriesConfig;
    private final WorldManager worldManager;
    private final WorldGroupManager worldGroupManager;
    private final ProfileDataSource profileDataSource;

    private Settings pwiSettings;
    private GroupManager pwiGroupManager;
    private FlatFile pwiFlatFile;
    private File dataDirectory;
    private Method getFileMethod;
    private Method deserializeMethod;

    private List<OfflinePlayer> playerList;

    PwiImportHelper(
            @NotNull PerWorldInventoryAPI pwiAPI,
            @NotNull InventoriesConfig inventoriesConfig,
            @NotNull WorldManager worldManager,
            @NotNull WorldGroupManager worldGroupManager,
            @NotNull ProfileDataSource profileDataSource) {
        this.pwiAPI = pwiAPI;
        this.inventoriesConfig = inventoriesConfig;
        this.worldManager = worldManager;
        this.worldGroupManager = worldGroupManager;
        this.profileDataSource = profileDataSource;
    }

    /**
     * The 'Main' method for the import.
     */
    void importData() throws DataImportException {
        pwiSetUp();
        transferConfigOptions();
        findPlayersWithData();
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
    private void pwiSetUp() {
        this.pwiSettings = ReflectHelper.getFieldValue(pwiAPI, "settings", Settings.class);
        this.pwiGroupManager = ReflectHelper.getFieldValue(this.pwiAPI, "groupManager", GroupManager.class);
        ProfileManager pwiProfileManager = ReflectHelper.getFieldValue(this.pwiAPI, "profileManager", ProfileManager.class);
        this.pwiFlatFile = ReflectHelper.getFieldValue(pwiProfileManager, "dataSource", FlatFile.class);
        this.getFileMethod = ReflectHelper.getMethod(this.pwiFlatFile, "getFile", ProfileKey.class);
        this.dataDirectory = ReflectHelper.getFieldValue(this.pwiFlatFile, "dataDirectory", File.class);
        this.deserializeMethod = ReflectHelper.getMethod(SerializationHelper.class, "deserialize", Map.class, boolean.class);
    }

    /**
     * Set similar/supported config options in MultiverseInventories with the values used in PerWorldInventory.
     */
    private void transferConfigOptions() {
        inventoriesConfig.setEnableGamemodeShareHandling(this.pwiSettings.getProperty(PluginSettings.SEPARATE_GM_INVENTORIES));
        inventoriesConfig.setSavePlayerdataOnQuit(this.pwiSettings.getProperty(PluginSettings.LOAD_DATA_ON_JOIN));
        inventoriesConfig.setDefaultUngroupedWorlds(this.pwiSettings.getProperty(PluginSettings.SHARE_IF_UNCONFIGURED));
        inventoriesConfig.getActiveOptionalShares().setSharing(Sharables.ECONOMY, this.pwiSettings.getProperty(PlayerSettings.USE_ECONOMY));
        inventoriesConfig.save();
    }

    private void findPlayersWithData() throws DataImportException {
        if (dataDirectory == null) {
            throw new DataImportException("PerWorldInventory data directory not found!");
        }
        File[] playerFolders = dataDirectory.listFiles();
        if (playerFolders == null) {
            throw new DataImportException("Unable to traverse PerWorldInventory data directory!");
        }
        this.playerList = Arrays.stream(playerFolders)
                .filter(File::isDirectory)
                .map(file -> UUID.fromString(file.getName()))
                .map(Bukkit::getOfflinePlayer)
                .toList();
    }

    /**
     * Gets all PerWorldInventory groups based on all the worlds known by Multiverse.
     *
     * @return A collection of PerWorldInventory groups.
     */
    private Collection<Group> getPWIGroups() {
        Set<Group> groups = new HashSet<>(this.pwiGroupManager.getGroups().values());
        if (!inventoriesConfig.getDefaultUngroupedWorlds()) {
            worldManager.getWorlds().forEach(world ->
                    groups.add(this.pwiGroupManager.getGroupFromWorld(world.getName())));
        }
        return groups;
    }

    /**
     * Create a MultiverseInventories {@link WorldGroup} based on PerWorldInventory Group.
     *
     * @param group A PerWorldInventory Group.
     */
    private void createMVGroup(Group group) {
        Logging.finer("PerWorldInventory Group: %s", group);
        WorldGroup worldGroup = worldGroupManager.getGroup(group.getName());
        if (worldGroup == null) {
            worldGroup = worldGroupManager.newEmptyGroup(group.getName());
        }

        // In PerWorldInventory, shares can only be toggled to be enabled or disabled globally.
        // So setting all shares here then transferring only those enabled later should work enough,
        // since you can't actually disable shares in MultiverseInventories.
        worldGroup.getShares().addAll(Sharables.allOf());
        worldGroup.addWorlds(group.getWorlds());
        if (group.getRespawnWorld() != null) {
            worldGroup.setSpawnWorld(group.getRespawnWorld());
        }
        worldGroupManager.updateGroup(worldGroup);
    }

    /**
     * Transfer all player data from PerWorldInventory to MultiverseInventories for a given group.
     *
     * @param group A PerWorldInventory Group.
     */
    private void saveMVDataForGroup(Group group) throws DataImportException {
        for (OfflinePlayer offlinePlayer : this.playerList) {
            saveMVDataForPlayer(group, offlinePlayer);
        }
    }

    private void saveMVDataForPlayer(Group group, OfflinePlayer offlinePlayer) throws DataImportException {
        GlobalProfile globalProfile = profileDataSource.getGlobalProfileNow(offlinePlayer);
        globalProfile.setLoadOnLogin(pwiSettings.getProperty(PluginSettings.LOAD_DATA_ON_JOIN));
        profileDataSource.updateGlobalProfile(globalProfile);
        for (GameMode gameMode : GameMode.values()) {
            me.ebonjaeger.perworldinventory.data.PlayerProfile pwiPlayerData = getPWIPlayerData(offlinePlayer, group, gameMode);
            if (pwiPlayerData == null) {
                continue;
            }
            for (var mvProfile : getMVPlayerData(offlinePlayer, group, gameMode)) {
                transferToMVPlayerData(mvProfile, pwiPlayerData);
            }
        }
    }

    /**
     * Gets MultiverseInventories PlayerProfile based on PerWorldInventory ProfileKey.
     *
     * @param offlinePlayer OfflinePlayer to get data for.
     * @param group         Group to get data for.
     * @param gameMode      GameMode to get data for.
     * @return A MultiverseInventories PLayerProfile.
     */
    private List<PlayerProfile> getMVPlayerData(
            @NotNull OfflinePlayer offlinePlayer, @NotNull Group group, @NotNull GameMode gameMode) {
        List<PlayerProfile> profiles = new ArrayList<>();
        profiles.add(profileDataSource.getPlayerDataNow(org.mvplugins.multiverse.inventories.profile.ProfileKey
                        .create(ContainerType.GROUP, group.getName(), ProfileTypes.forGameMode(gameMode), offlinePlayer.getUniqueId())));
        for (var worldName : group.getWorlds()) {
            profiles.add(profileDataSource.getPlayerDataNow(org.mvplugins.multiverse.inventories.profile.ProfileKey
                            .create(ContainerType.WORLD, worldName, ProfileTypes.forGameMode(gameMode), offlinePlayer.getUniqueId())));
        }
        return profiles;
    }

    /**
     * Gets PerWorldInventory PlayerProfile based on PerWorldInventory ProfileKey.
     *
     * @param offlinePlayer OfflinePlayer to get data for.
     * @param group         Group to get data for.
     * @param gameMode      GameMode to get data for.
     * @return A PerWorldInventory PLayerProfile.
     */
    private @Nullable me.ebonjaeger.perworldinventory.data.PlayerProfile getPWIPlayerData(
            @NotNull OfflinePlayer offlinePlayer, @NotNull Group group, @NotNull GameMode gameMode) throws DataImportException {
        ProfileKey pwiKey = new ProfileKey(offlinePlayer.getUniqueId(), group, gameMode);
        File pwiPlayerDataFile = getPWIFile(pwiKey);
        if (!pwiPlayerDataFile.isFile()) {
            Logging.finer("No data for %s.",  pwiKey.toString());
            return null;
        }

        me.ebonjaeger.perworldinventory.data.PlayerProfile pwiPlayerProfile;
        try {
            JSONParser parser = new JSONParser(JSONParser.USE_INTEGER_STORAGE);
            JSONObject jsonObject = (JSONObject) parser.parse(new FileInputStream(pwiPlayerDataFile));
            if (jsonObject.containsKey("==")) {
                pwiPlayerProfile = ReflectHelper.invokeMethod(null, deserializeMethod, jsonObject, true);
            } else {
                // Use legacy serialization that doesn't use ConfigurationSerializable
                pwiPlayerProfile = PlayerSerializer.INSTANCE.deserialize(
                        jsonObject,
                        Objects.requireNonNull(offlinePlayer.getName()),
                        PlayerStats.INVENTORY_SIZE,
                        PlayerStats.ENDER_CHEST_SIZE);
            }
        } catch (Exception e) {
            Logging.severe("Unable to parse file into profile: " + pwiPlayerDataFile.getAbsolutePath());
            e.printStackTrace();
            return null;
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
        return ReflectHelper.invokeMethod(this.pwiFlatFile, this.getFileMethod, pwiKey);
    }

    /**
     * Transfers supported player data from PerWorldInventory to MultiverseInventories.
     *
     * @param mvPlayerProfile   MultiverseInventories PlayerProfile to transfer to.
     * @param pwiPlayerProfile  PerWorldInventory PlayerProfile to transfer from.
     */
    private void transferToMVPlayerData(
            PlayerProfile mvPlayerProfile,
            me.ebonjaeger.perworldinventory.data.PlayerProfile pwiPlayerProfile
    ) throws DataImportException {
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
        // if (pwiSettings.getProperty(PlayerSettings.LOAD_DISPLAY_NAME)) {
        //     mvPlayerProfile.set(Sharables, pwiPlayerProfile.getDisplayName());
        // }
        //  if (pwiSettings.getProperty(PlayerSettings.LOAD_FLYING)) {
        //     mvPlayerProfile.set(Sharables, pwiPlayerProfile.getAllowFlight());
        //  }
        // mvPlayerProfile.set(Sharables.BED_SPAWN, pwiPlayerProfile);
        // mvPlayerProfile.set(Sharables.HUNGER, pwiPlayerProfile);
        // mvPlayerProfile.set(Sharables.LAST_LOCATION, pwiPlayerProfile);
        // mvPlayerProfile.set(Sharables.OFF_HAND, pwiPlayerProfile);
        // mvPlayerProfile.set(Sharables.TOTAL_EXPERIENCE, pwiPlayerProfile);

        profileDataSource.updatePlayerData(mvPlayerProfile);
    }
}
