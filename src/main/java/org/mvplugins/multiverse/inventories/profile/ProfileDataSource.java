package org.mvplugins.multiverse.inventories.profile;

import org.jvnet.hk2.annotations.Contract;
import org.mvplugins.multiverse.external.vavr.control.Option;
import org.mvplugins.multiverse.inventories.profile.data.PlayerProfile;
import org.mvplugins.multiverse.inventories.profile.key.ContainerType;
import org.mvplugins.multiverse.inventories.profile.key.GlobalProfileKey;
import org.mvplugins.multiverse.inventories.profile.key.ProfileFileKey;
import org.mvplugins.multiverse.inventories.profile.key.ProfileKey;
import org.mvplugins.multiverse.inventories.profile.key.ProfileType;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * A source for updating and retrieving player profiles via persistence.
 */
@Contract
public sealed interface ProfileDataSource permits FlatFileProfileDataSource {

    /**
     * Retrieves a PlayerProfile from the disk. If no data was found, a new PlayerProfile will be created.
     *
     * @param profileKey The key of the profile to retrieve.
     * @return The player profile data.
     */
    CompletableFuture<PlayerProfile> getPlayerProfile(ProfileKey profileKey);

    /**
     * Updates the persisted data for a player for a specific profile to disk.
     *
     * @param playerProfile The profile for the player that is being updated.
     * @return Future that completes when the profile has been updated.
     */
    CompletableFuture<Void> updatePlayerProfile(PlayerProfile playerProfile);

    /**
     * Clears all data of the specified profile, and deletes profile data from disk.
     * <br />
     * This action is irreversible.
     *
     * @param profileKey    The key of the profile to delete
     * @return Future that completes when the profile has been deleted.
     */
    CompletableFuture<Void> deletePlayerProfile(ProfileKey profileKey);

    /**
     * Clears all data for multiple {@link ProfileType} of the specified profile, and deletes profile data from disk.
     * If all profiles are deleted, the profile file itself will be deleted.
     * <br />
     * This action is irreversible.
     *
     * @param profileKey    The key of the profile to delete
     * @param profileTypes  The list of profile types to delete
     * @return Future that completes when the profile has been deleted.
     */
    CompletableFuture<Void> deletePlayerProfiles(ProfileFileKey profileKey, ProfileType[] profileTypes);

    /**
     * Deletes the profile file for a player's profile from disk. Essentially same as
     * {@link #deletePlayerProfiles(ProfileFileKey, ProfileType[])} with all profile types.
     * <br />
     * This action is irreversible.
     *
     * @param profileKey    The key of the profile to delete
     * @return Future that completes when the profile has been deleted.
     */
    CompletableFuture<Void> deletePlayerFile(ProfileFileKey profileKey);

    /**
     * Copies all the data belonging to oldName to newName and removes the old data.
     *
     * @param oldName the previous name of the player.
     * @param newName the new name of the player.
     * @throws IOException Thrown if something goes wrong while migrating the files.
     */
    void migratePlayerProfileName(String oldName, String newName) throws IOException;

    /**
     * Retrieves the global profile for a player which contains meta-data for the player.
     *
     * @param key The key of the player.
     * @return The global profile for the specified player asynchronously.
     */
    CompletableFuture<GlobalProfile> getGlobalProfile(GlobalProfileKey key);

    /**
     * Retrieves the global profile for a player which contains meta-data for the player if it exists.
     *
     * @param key The key of the player.
     * @return The global profile for the specified player or {@link Option#none} if it does not exist asynchronously.
     */
    CompletableFuture<Option<GlobalProfile>> getExistingGlobalProfile(GlobalProfileKey key);

    /**
     * Modifies the global profile for a player and automatically saves it.
     *
     * @param key The key of the player.
     * @return A CompletableFuture that completes when the global profile has been saved.
     */
    CompletableFuture<Void> modifyGlobalProfile(GlobalProfileKey key, Consumer<GlobalProfile> consumer);

    /**
     * Update the file for a player's global profile to disk.
     *
     * @param globalProfile The GlobalProfile object to update the file for.
     * @return A CompletableFuture that completes when the global profile has been updated.
     */
    CompletableFuture<Void> updateGlobalProfile(GlobalProfile globalProfile);

    /**
     * Deletes the file for a player's global profile from disk. Optionally clears the player's profile data files as well.
     *
     * @param key               The key of the player.
     * @param clearPlayerFiles  Whether to clear the player's profile data files as well.
     * @return A CompletableFuture that completes when the global profile has been deleted.
     */
    CompletableFuture<Void> deleteGlobalProfile(GlobalProfileKey key, boolean clearPlayerFiles);

    /**
     * Lists the names of all available data containers of the specified type.
     *
     * @param containerType The type of the container (e.g., WORLD, GROUP) whose data names are to be listed.
     * @return A collection of strings representing the names of the data containers.
     */
    List<String> listContainerDataNames(ContainerType containerType);

    /**
     * Lists the names of all available player profiles within the given container type and container name.
     *
     * @param containerType The type of the container (e.g., WORLD, GROUP) whose player profiles are to be listed.
     * @param containerName  The name of the container whose player profiles are to be listed.
     * @return A collection of strings representing the names of all available player profiles.
     */
    List<String> listPlayerProfileNames(ContainerType containerType, String containerName);

    /**
     * Retrieves a collection of UUIDs of all players who have a global profile.
     *
     * @return A collection of UUIDs of all players who have a global profile.
     */
    List<UUID> listGlobalProfileUUIDs();
}
