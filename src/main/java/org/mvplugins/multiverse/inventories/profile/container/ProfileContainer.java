package org.mvplugins.multiverse.inventories.profile.container;

import org.mvplugins.multiverse.inventories.MultiverseInventories;
import org.mvplugins.multiverse.inventories.profile.ProfileCacheManager;
import org.mvplugins.multiverse.inventories.profile.ProfileDataSource;
import org.mvplugins.multiverse.inventories.profile.key.ContainerType;
import org.mvplugins.multiverse.inventories.profile.key.ProfileFileKey;
import org.mvplugins.multiverse.inventories.profile.key.ProfileKey;
import org.mvplugins.multiverse.inventories.profile.key.ProfileTypes;
import org.mvplugins.multiverse.inventories.profile.PlayerProfile;
import org.mvplugins.multiverse.inventories.profile.key.ProfileType;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.mvplugins.multiverse.inventories.util.FutureNow;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

/**
 * A container for player profiles in a given world or world group (based on {@link #getContainerType()}),
 * <br/>
 * Players may have separate profiles per game mode within this container if game mode profiles are enabled.
 */
public final class ProfileContainer {

    private final String name;
    private final ContainerType type;
    private final ProfileDataSource profileDataSource;
    private final ProfileCacheManager profileCacheManager;

    ProfileContainer(MultiverseInventories inventories, String name, ContainerType type) {
        this.name = name;
        this.type = type;
        this.profileDataSource = inventories.getServiceLocator().getService(ProfileDataSource.class);
        this.profileCacheManager = inventories.getServiceLocator().getService(ProfileCacheManager.class);
    }

    public Collection<String> listPlayerProfileNames() {
        return profileDataSource.listPlayerProfileNames(getContainerType(), getContainerName());
    }

    public CompletableFuture<PlayerProfile> getPlayerData(Player player) {
        return getPlayerData(ProfileTypes.forPlayer(player), player);
    }

    public CompletableFuture<PlayerProfile> getPlayerData(ProfileType profileType, OfflinePlayer player) {
        return profileDataSource.getPlayerProfile(ProfileKey.create(
                getContainerType(),
                getContainerName(),
                profileType,
                player));
    }

    /**
     * Retrieves the profile for the given player.
     * <p>If game mode profiles are enabled, the profile for their current game mode will be returned, otherwise their
     * survival profile will be returned.</p>
     *
     * @param player Player to get profile for.
     * @return The profile for the given player.
     */
    public PlayerProfile getPlayerProfileNow(Player player) {
        return getPlayerProfileNow(ProfileTypes.forPlayer(player), player);
    }

    /**
     * Retrieves the profile of the given type for the given player.
     *
     * @param profileType The type of profile to get data for, typically Survival or Creative.
     * @param player Player to get profile for.
     * @return The profile of the given type for the given player.
     */
    public PlayerProfile getPlayerProfileNow(ProfileType profileType, OfflinePlayer player) {
        return FutureNow.get(profileDataSource.getPlayerProfile(ProfileKey.create(
                getContainerType(),
                getContainerName(),
                profileType,
                player)));
    }

    /**
     * Removes all of the profile data for a given player in this profile container.
     *
     * @param player Player to remove data for.
     * @return
     */
    public CompletableFuture<Void> deletePlayerFile(OfflinePlayer player) {
        return profileDataSource.deletePlayerFile(ProfileFileKey.create(type, name, player));
    }

    /**
     * Removes the profile data for a specific type of profile in this profile container.
     *
     * @param profileType The type of profile to remove data for.
     * @param player      Player to remove data for.
     * @return
     */
    public CompletableFuture<Void> deletePlayerProfile(ProfileType profileType, OfflinePlayer player) {
        return profileDataSource.deletePlayerProfile(ProfileKey.create(type, name, profileType, player));
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
    public void clearContainerCache() {
        profileCacheManager.clearPlayerProfileCache(key ->
                key.getContainerType().equals(type) && key.getDataName().equals(name));
    }
}
