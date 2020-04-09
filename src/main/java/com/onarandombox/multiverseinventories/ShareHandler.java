package com.onarandombox.multiverseinventories;

import com.dumptruckman.minecraft.util.Logging;
import com.onarandombox.multiverseinventories.event.ShareHandlingEvent;
import com.onarandombox.multiverseinventories.profile.PlayerProfile;
import com.onarandombox.multiverseinventories.profile.container.ContainerType;
import com.onarandombox.multiverseinventories.share.PersistingProfile;
import com.onarandombox.multiverseinventories.share.Sharable;
import com.onarandombox.multiverseinventories.share.Shares;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.LinkedList;
import java.util.List;

import static com.onarandombox.multiverseinventories.share.Sharables.allOf;

/**
 * Abstract class for handling sharing of data between worlds and game modes.
 */
public abstract class ShareHandler {

    protected final MultiverseInventories inventories;
    protected final Player player;
    final AffectedProfiles affectedProfiles;

    ShareHandler(MultiverseInventories inventories, Player player) {
        this.inventories = inventories;
        this.player = player;
        this.affectedProfiles = new AffectedProfiles();
    }

    final void setAlwaysWriteProfile(PlayerProfile profile) {
        affectedProfiles.setAlwaysWriteProfile(profile);
    }

    /**
     * @param profile   The player profile that will need data saved to.
     * @param shares    What from this group needs to be saved.
     */
    final void addWriteProfile(PlayerProfile profile, Shares shares) {
        affectedProfiles.addWriteProfile(profile, shares);
    }

    /**
     * @param profile   The player profile that will need data loaded from.
     * @param shares    What from this group needs to be loaded.
     */
    final void addReadProfile(PlayerProfile profile, Shares shares) {
        affectedProfiles.addReadProfile(profile, shares);
    }

    /**
     * Finalizes the transfer from one world to another.  This handles the switching
     * inventories/stats for a player and persisting the changes.
     */
    final void handleSharing() {
        prepareProfiles();
        ShareHandlingEvent event = this.createEvent();

        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            this.completeSharing(event);
        }
    }

    protected abstract void prepareProfiles();

    protected abstract ShareHandlingEvent createEvent();

    void logBypass() {
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
            updateProfile(inventories, event.getPlayer(), event.getAlwaysWriteProfile());
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
            updateProfile(inventories, player, writeProfile);
        }
    }

    private void updatePlayer(Player player, List<PersistingProfile> readProfiles) {
        for (PersistingProfile readProfile : readProfiles) {
            updatePlayer(inventories, player, readProfile);
        }
    }

    private void logHandlingComplete(ShareHandlingEvent event) {
        Logging.finer("=== %s complete for %s ===", event.getPlayer().getName(), event.getEventName());
    }

    static void updateProfile(final MultiverseInventories inventories, final Player player, final PersistingProfile profile) {
        int debug = inventories.getMVIConfig().getGlobalDebug();
        StringBuilder persisted = new StringBuilder();
        for (Sharable sharable : profile.getShares()) {
            if (sharable.isOptional()) {
                if (!inventories.getMVIConfig().getOptionalShares().contains(sharable)) {
                    Logging.finest("Ignoring optional share: " + sharable.getNames()[0]);
                    continue;
                }
                if (profile.getProfile().getContainerType() == ContainerType.WORLD && !inventories.getMVIConfig().usingOptionalsForUngrouped()) {
                    Logging.finest("Ignoring optional share '" + sharable.getNames()[0] + "' for ungrouped world!");
                    continue;
                }
            }
            if (debug > 0) {
                if (persisted.length() > 0) {
                    persisted.append(", ");
                }
                persisted.append(sharable.getNames()[0]);
            }
            sharable.getHandler().updateProfile(profile.getProfile(), player);
        }
        if (debug > 0) {
            Logging.finer("Persisted: " + persisted.toString() + " to "
                    + profile.getProfile().getContainerType() + ":" + profile.getProfile().getContainerName()
                    + " (" + profile.getProfile().getProfileType() + ")"
                    + " for player " + profile.getProfile().getPlayer().getName());
        }
        inventories.getData().updatePlayerData(profile.getProfile());
    }

    static void updatePlayer(final MultiverseInventories inventories, final Player player, final PersistingProfile profile) {
        StringBuilder defaulted = new StringBuilder();
        StringBuilder loaded = new StringBuilder();
        player.closeInventory();
        for (Sharable sharable : profile.getShares()) {
            if (sharable.isOptional()) {
                if (!inventories.getMVIConfig().getOptionalShares().contains(sharable)) {
                    Logging.finest("Ignoring optional share: " + sharable.getNames()[0]);
                    continue;
                }
                if (profile.getProfile().getContainerType() == ContainerType.WORLD && !inventories.getMVIConfig().usingOptionalsForUngrouped()) {
                    Logging.finest("Ignoring optional share '" + sharable.getNames()[0] + "' for ungrouped world!");
                    continue;
                }
            }
            if (sharable.getHandler().updatePlayer(player, profile.getProfile())) {
                if (loaded.length() > 0) {
                    loaded.append(", ");
                }
                loaded.append(sharable.getNames()[0]);
            } else {
                if (defaulted.length() > 0) {
                    defaulted.append(", ");
                }
                defaulted.append(sharable.getNames()[0]);
            }
        }
        if (!loaded.toString().isEmpty()) {
            Logging.finer("Updated: " + loaded.toString() + " for "
                    + profile.getProfile().getPlayer().getName() + " for "
                    + profile.getProfile().getContainerType() + ":" + profile.getProfile().getContainerName()
                    + " (" + profile.getProfile().getProfileType() + ")");
        }
        if (!defaulted.toString().isEmpty()) {
            Logging.finer("Defaulted: " + defaulted.toString() + " for "
                    + profile.getProfile().getPlayer().getName() + " for "
                    + profile.getProfile().getContainerType() + ":" + profile.getProfile().getContainerName()
                    + " (" + profile.getProfile().getProfileType() + ")");
        }
    }

    public static class AffectedProfiles {

        private PersistingProfile alwaysWriteProfile;
        private final List<PersistingProfile> writeProfiles = new LinkedList<>();
        private final List<PersistingProfile> readProfiles = new LinkedList<>();

        AffectedProfiles() { }

        final void setAlwaysWriteProfile(PlayerProfile profile) {
            alwaysWriteProfile = new DefaultPersistingProfile(allOf(), profile);
        }

        /**
         * @param profile   The player profile that will need data saved to.
         * @param shares    What from this group needs to be saved.
         */
        final void addWriteProfile(PlayerProfile profile, Shares shares) {
            writeProfiles.add(new DefaultPersistingProfile(shares, profile));
        }

        /**
         * @param profile   The player profile that will need data loaded from.
         * @param shares    What from this group needs to be loaded.
         */
        final void addReadProfile(PlayerProfile profile, Shares shares) {
            readProfiles.add(new DefaultPersistingProfile(shares, profile));
        }

        public PersistingProfile getAlwaysWriteProfile() {
            return alwaysWriteProfile;
        }

        public List<PersistingProfile> getWriteProfiles() {
            return writeProfiles;
        }

        public List<PersistingProfile> getReadProfiles() {
            return readProfiles;
        }
    }

}
