package org.mvplugins.multiverse.inventories.profile.container;

import com.dumptruckman.minecraft.util.Logging;
import org.mvplugins.multiverse.inventories.MultiverseInventories;
import org.mvplugins.multiverse.inventories.config.InventoriesConfig;
import org.mvplugins.multiverse.inventories.profile.ProfileDataSource;
import org.mvplugins.multiverse.inventories.profile.ProfileKey;
import org.mvplugins.multiverse.inventories.profile.ProfileTypes;
import org.mvplugins.multiverse.inventories.profile.PlayerProfile;
import org.mvplugins.multiverse.inventories.profile.ProfileType;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
/**
 * A container for player profiles in a given world or world group (based on {@link #getContainerType()}),
 * using WeakHashMaps to keep memory usage to a minimum.
 * <br/>
 * Players may have separate profiles per game mode within this container if game mode profiles are enabled.
 */
public final class ProfileContainer {

    private final Map<String, Map<ProfileType, PlayerProfile>> playerData = new WeakHashMap<>();
    private final MultiverseInventories inventories;
    private final String name;
    private final ContainerType type;
    private final ProfileDataSource profileDataSource;
    private final InventoriesConfig config;

    ProfileContainer(MultiverseInventories inventories, String name, ContainerType type) {
        this.inventories = inventories;
        this.name = name;
        this.type = type;
        this.profileDataSource = inventories.getServiceLocator().getService(ProfileDataSource.class);
        this.config = inventories.getServiceLocator().getService(InventoriesConfig.class);
    }

    /**
     * Gets the stored profiles for this player, mapped by ProfileType.
     *
     * @param name The name of player to get profile map for.
     * @return The profile map for the given player.
     */
    private Map<ProfileType, PlayerProfile> getPlayerData(String name) {
        return this.playerData.computeIfAbsent(name, k -> new HashMap<>());
    }

    /**
     * Retrieves the profile for the given player.
     * <p>If game mode profiles are enabled, the profile for their current game mode will be returned, otherwise their
     * survival profile will be returned.</p>
     *
     * @param player Player to get profile for.
     * @return The profile for the given player.
     */
    public PlayerProfile getPlayerData(Player player) {
        ProfileType type;
        if (config.isUsingGameModeProfiles()) {
            type = ProfileTypes.forGameMode(player.getGameMode());
        } else {
            type = ProfileTypes.SURVIVAL;
        }
        return getPlayerData(type, player);
    }

    /**
     * Retrieves the profile of the given type for the given player.
     *
     * @param profileType The type of profile to get data for, typically Survival or Creative.
     * @param player Player to get profile for.
     * @return The profile of the given type for the given player.
     */
    public PlayerProfile getPlayerData(ProfileType profileType, OfflinePlayer player) {
        Map<ProfileType, PlayerProfile> profileMap = this.getPlayerData(player.getName());
        PlayerProfile playerProfile = profileMap.get(profileType);
        if (playerProfile == null) {
            playerProfile = profileDataSource.getPlayerData(getContainerType(),
                    getContainerName(), profileType, player.getUniqueId());
            Logging.finer("[%s - %s - %s - %s] not cached, loading from disk...",
                    profileType, getContainerType(), playerProfile.getContainerName(), player.getName());
            profileMap.put(profileType, playerProfile);
        }
        return playerProfile;
    }

    /**
     * Adds a player profile to this profile container.
     *
     * @param playerProfile Player player to add.
     */
    public void addPlayerData(PlayerProfile playerProfile) {
        this.getPlayerData(playerProfile.getPlayer().getName()).put(playerProfile.getProfileType(), playerProfile);
    }

    /**
     * Removes all of the profile data for a given player in this profile container.
     *
     * @param player Player to remove data for.
     */
    public void removeAllPlayerData(OfflinePlayer player) {
        this.getPlayerData(player.getName()).clear();
        profileDataSource.removePlayerData(getContainerType(), getContainerName(), null, player.getUniqueId());
    }

    /**
     * Removes the profile data for a specific type of profile in this profile container.
     *
     * @param profileType The type of profile to remove data for.
     * @param player Player to remove data for.
     */
    public void removePlayerData(ProfileType profileType, OfflinePlayer player) {
        this.getPlayerData(player.getName()).remove(profileType);
        profileDataSource.removePlayerData(getContainerType(), getContainerName(), profileType, player.getUniqueId());
    }

    /**
     * Returns the name of this profile container which is primarily used for persistence purposes.
     * <p>The name reflects the world name if this is a world profile container, or the arbitrary group name if
     * this is a world group profile container.</p>
     *
     * @return The name to use to look up Data.
     */
    public String getContainerName() {
        return name;
    }

    /**
     * Returns the container type for this container.
     *
     * @return the container type.
     */
    public ContainerType getContainerType() {
        return type;
    }

    /**
     * Clears all cached data in the container.
     */
    public void clearContainer() {
        for (Map<ProfileType, PlayerProfile> profiles : playerData.values()) {
            for (PlayerProfile profile : profiles.values()) {
                profileDataSource.clearProfileCache(ProfileKey.createProfileKey(profile));
            }
        }
        this.playerData.clear();
    }
}
