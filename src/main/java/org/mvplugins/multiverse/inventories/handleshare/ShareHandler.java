package org.mvplugins.multiverse.inventories.handleshare;

import com.dumptruckman.minecraft.util.Logging;
import org.mvplugins.multiverse.external.jetbrains.annotations.Nullable;
import org.mvplugins.multiverse.inventories.MultiverseInventories;
import org.mvplugins.multiverse.inventories.config.InventoriesConfig;
import org.mvplugins.multiverse.inventories.event.ShareHandlingEvent;
import org.mvplugins.multiverse.inventories.profile.PlayerProfile;
import org.mvplugins.multiverse.inventories.profile.container.ContainerType;
import org.mvplugins.multiverse.inventories.profile.container.ProfileContainerStore;
import org.mvplugins.multiverse.inventories.profile.container.ProfileContainerStoreProvider;
import org.mvplugins.multiverse.inventories.profile.group.WorldGroupManager;
import org.mvplugins.multiverse.inventories.share.Shares;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Abstract class for handling sharing of data between worlds and game modes.
 */
sealed abstract class ShareHandler permits WorldChangeShareHandler, GameModeShareHandler {

    protected final MultiverseInventories inventories;
    protected final Player player;
    protected final InventoriesConfig inventoriesConfig;
    protected final WorldGroupManager worldGroupManager;
    protected final ProfileContainerStore worldProfileContainerStore;
    final AffectedProfiles affectedProfiles;

    ShareHandler(MultiverseInventories inventories, Player player) {
        this.inventories = inventories;
        this.player = player;
        this.affectedProfiles = new AffectedProfiles();
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
    final void handleSharing() {
        ShareHandlingEvent event = this.createEvent();

        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            Logging.fine("Share handling has been cancelled by another plugin!");
            return;
        }
        this.completeSharing(event);
    }

    protected final void setAlwaysWriteProfile(PlayerProfile profile) {
        affectedProfiles.setAlwaysWriteProfile(profile);
    }

    /**
     * @param profile   The player profile that will need data saved to.
     * @param shares    What from this group needs to be saved.
     */
    protected final void addWriteProfile(PlayerProfile profile, Shares shares) {
        affectedProfiles.addWriteProfile(profile, shares);
    }

    /**
     * Finalizes the transfer from one world to another. This handles the switching
     * inventories/stats for a player and persisting the changes.
     *
     * @param profile   The player profile that will need data loaded from.
     * @param shares    What from this group needs to be loaded.
     */
    protected final void addReadProfile(PlayerProfile profile, Shares shares) {
        affectedProfiles.addReadProfile(profile, shares);
    }

    protected abstract ShareHandlingEvent createEvent();

    protected void logBypass() {
        Logging.fine(player.getName() + " has bypass permission for 1 or more world/groups!");
    }

    private void completeSharing(ShareHandlingEvent event) {
        logAffectedProfilesCount(event);
        saveAlwaysWriteProfile(event);
        handleProfileChanges(event);
        logHandlingComplete(event);
    }

    private void logAffectedProfilesCount(ShareHandlingEvent event) {
        PersistingProfile alwaysWriteProfile = event.getAlwaysWriteProfile();
        int writeProfiles = event.getWriteProfiles().size() + (alwaysWriteProfile != null ? 1 : 0);

        Logging.finer("Change affected by %d fromProfiles and %d toProfiles", writeProfiles,
                event.getReadProfiles().size());
    }

    private void saveAlwaysWriteProfile(ShareHandlingEvent event) {
        if (event.getAlwaysWriteProfile() != null) {
            ShareHandlingUpdater.updateProfile(inventories, event.getPlayer(), event.getAlwaysWriteProfile());
        } else {
            Logging.warning("No fromWorld to save to");
        }
    }

    private void handleProfileChanges(ShareHandlingEvent event) {
        if (event.getReadProfiles().isEmpty()) {
            Logging.finest("No profiles to read from - nothing more to do.");
        } else {
            updateProfiles(event.getPlayer(), event.getWriteProfiles());
            updatePlayer(event.getPlayer(), event.getReadProfiles());
        }
    }

    private void updateProfiles(Player player, List<PersistingProfile> writeProfiles) {
        for (PersistingProfile writeProfile : writeProfiles) {
            ShareHandlingUpdater.updateProfile(inventories, player, writeProfile);
        }
    }

    private void updatePlayer(Player player, List<PersistingProfile> readProfiles) {
        for (PersistingProfile readProfile : readProfiles) {
            ShareHandlingUpdater.updatePlayer(inventories, player, readProfile);
        }
    }

    private void logHandlingComplete(ShareHandlingEvent event) {
        Logging.finer("=== %s complete for %s ===", event.getPlayer().getName(), event.getEventName());
    }

}
