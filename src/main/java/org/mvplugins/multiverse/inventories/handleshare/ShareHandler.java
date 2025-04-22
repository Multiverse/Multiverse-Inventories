package org.mvplugins.multiverse.inventories.handleshare;

import com.dumptruckman.minecraft.util.Logging;
import org.mvplugins.multiverse.inventories.MultiverseInventories;
import org.mvplugins.multiverse.inventories.config.InventoriesConfig;
import org.mvplugins.multiverse.inventories.event.ShareHandlingEvent;
import org.mvplugins.multiverse.inventories.profile.data.ProfileDataSnapshot;
import org.mvplugins.multiverse.inventories.profile.ProfileDataSource;
import org.mvplugins.multiverse.inventories.profile.key.ContainerType;
import org.mvplugins.multiverse.inventories.profile.container.ProfileContainerStore;
import org.mvplugins.multiverse.inventories.profile.container.ProfileContainerStoreProvider;
import org.mvplugins.multiverse.inventories.profile.group.WorldGroupManager;
import org.mvplugins.multiverse.inventories.share.Sharables;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Abstract class for handling sharing of data between worlds and game modes.
 */
sealed abstract class ShareHandler permits GameModeShareHandler, ReadOnlyShareHandler, WorldChangeShareHandler, WriteOnlyShareHandler {

    protected final Player player;
    protected final AffectedProfiles affectedProfiles;

    protected final MultiverseInventories inventories;
    protected final ProfileDataSource profileDataStore;
    protected final InventoriesConfig inventoriesConfig;
    protected final WorldGroupManager worldGroupManager;
    protected final ProfileContainerStore worldProfileContainerStore;

    ShareHandler(MultiverseInventories inventories, Player player) {
        this.player = player;
        this.affectedProfiles = new AffectedProfiles();

        this.inventories = inventories;
        this.profileDataStore = inventories.getServiceLocator().getService(ProfileDataSource.class);
        this.inventoriesConfig = inventories.getServiceLocator().getService(InventoriesConfig.class);
        this.worldGroupManager = inventories.getServiceLocator().getService(WorldGroupManager.class);
        this.worldProfileContainerStore = inventories.getServiceLocator()
                .getService(ProfileContainerStoreProvider.class)
                .getStore(ContainerType.WORLD);
    }

    /**
     * Finalizes the transfer from one world to another.  This handles the switching
     * inventories/stats for a player and persisting the changes.
     */
    public final void handleSharing() {
        long startTime = System.nanoTime();
        this.prepareProfiles();
        ShareHandlingEvent event = this.createEvent();
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            Logging.fine("Share handling has been cancelled by another plugin!");
            return;
        }
        logAffectedProfilesCount();
        ProfileDataSnapshot snapshot = getSnapshot();
        updatePlayer();
        updateProfiles(snapshot);
        double timeTaken = (System.nanoTime() - startTime) / 1000000.0;
        logHandlingComplete(timeTaken, event);
    }

    protected abstract void prepareProfiles();

    protected abstract ShareHandlingEvent createEvent();

    protected void logBypass() {
        Logging.fine(player.getName() + " has bypass permission for 1 or more world/groups!");
    }

    private void logAffectedProfilesCount() {
        int writeProfiles = affectedProfiles.getWriteProfiles().size();

        Logging.finer("Change affected by %d fromProfiles and %d toProfiles", writeProfiles,
                affectedProfiles.getReadProfiles().size());
    }

    private ProfileDataSnapshot getSnapshot() {
        ProfileDataSnapshot profileDataSnapshot = new ProfileDataSnapshot();
        Sharables.enabled().forEach(sharable -> sharable.getHandler().updateProfile(profileDataSnapshot, player));
        return profileDataSnapshot;
    }

    private void updatePlayer() {
        for (PersistingProfile readProfile : affectedProfiles.getReadProfiles()) {
            ShareHandlingUpdater.updatePlayer(inventories, player, readProfile);
        }
    }

    private void updateProfiles(ProfileDataSnapshot snapshot) {
        if (affectedProfiles.getWriteProfiles().isEmpty()) {
            Logging.finest("No profiles to write - nothing more to do.");
            return;
        }
        for (PersistingProfile writeProfile : affectedProfiles.getWriteProfiles()) {
            updatePersistingProfile(writeProfile, snapshot);
        }
    }

    private void updatePersistingProfile(PersistingProfile persistingProfile, ProfileDataSnapshot snapshot) {
        if (persistingProfile.getShares().isEmpty()) {
            Logging.finest("No shares to write - nothing more to do.");
            return;
        }
        profileDataStore.getPlayerProfile(persistingProfile.getProfileKey())
                .thenCompose(playerProfile -> {
                    Logging.finer("Persisted: " + persistingProfile.getShares() + " to "
                            + playerProfile.getContainerType() + ":" + playerProfile.getContainerName()
                            + " (" + playerProfile.getProfileType() + ")"
                            + " for player " + playerProfile.getPlayerName());
                    playerProfile.update(snapshot, persistingProfile.getShares());
                    return profileDataStore.updatePlayerProfile(playerProfile);
                });
    }

    private void logHandlingComplete(double timeTaken, ShareHandlingEvent event) {
        Logging.fine("=== %s complete for %s | \u001B[32mtime taken: %4.4f ms\u001B[0m ===",
                player.getName(), event.getEventName(), timeTaken);
    }
}
