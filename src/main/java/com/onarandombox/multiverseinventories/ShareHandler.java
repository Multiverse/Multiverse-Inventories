package com.onarandombox.multiverseinventories;

import com.dumptruckman.minecraft.util.Logging;
import com.onarandombox.multiverseinventories.api.Inventories;
import com.onarandombox.multiverseinventories.api.profile.ContainerType;
import com.onarandombox.multiverseinventories.api.profile.PlayerProfile;
import com.onarandombox.multiverseinventories.api.profile.ProfileContainer;
import com.onarandombox.multiverseinventories.api.share.PersistingProfile;
import com.onarandombox.multiverseinventories.api.share.Sharable;
import com.onarandombox.multiverseinventories.api.share.Shares;
import com.onarandombox.multiverseinventories.event.MVInventoryHandlingEvent;
import com.onarandombox.multiverseinventories.event.MVInventoryHandlingEvent.Cause;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

/**
 * Abstract class for handling sharing of data between worlds and game modes.
 */
abstract class ShareHandler {

    protected final MVInventoryHandlingEvent event;
    protected final Inventories inventories;
    protected boolean hasBypass = false;

    public ShareHandler(Inventories inventories, Player player, Cause cause,
                        String fromWorld, String toWorld,
                        GameMode fromGameMode, GameMode toGameMode) {
        this.event = new MVInventoryHandlingEvent(player, cause, fromWorld, toWorld, fromGameMode, toGameMode);
        this.inventories = inventories;
    }

    /**
     * @param container The group/world the player's data is associated with.
     * @param shares    What from this group needs to be saved.
     * @param profile   The player player that will need data saved to.
     */
    public final void addFromProfile(ProfileContainer container, Shares shares, PlayerProfile profile) {
        event.getFromProfiles().add(new DefaultPersistingProfile(shares, profile));
    }

    /**
     * @param container The group/world the player's data is associated with.
     * @param shares    What from this group needs to be loaded.
     * @param profile   The player player that will need data loaded from.
     */
    public final void addToProfile(ProfileContainer container, Shares shares, PlayerProfile profile) {
        event.getToProfiles().add(new DefaultPersistingProfile(shares, profile));
    }

    /**
     * Finalizes the transfer from one world to another.  This handles the switching
     * inventories/stats for a player and persisting the changes.
     */
    public final void handleSharing() {
        this.handle();

        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            this.completeSharing();
        }
    }

    protected abstract void handle();

    void completeSharing() {
        Logging.finer("Change affected by " + event.getFromProfiles().size() + " fromProfiles and "
                + event.getToProfiles().size() + " toProfiles");
        // This if statement should never happen, really.
        if (event.getToProfiles().isEmpty()) {
            if (hasBypass) {
                Logging.fine(event.getPlayer().getName() + " has bypass permission for 1 or more world/groups!");
            } else {
                Logging.finer("No toProfiles...");
            }
            if (!event.getFromProfiles().isEmpty()) {
                updateProfile(inventories, event.getPlayer(), event.getFromProfiles().get(0));
            } else {
                Logging.warning("No fromWorld to save to");
            }
            Logging.finer("=== " + event.getPlayer().getName() + "'s " + event.getCause() + " handling complete! ===");
            return;
        }

        for (PersistingProfile persistingProfile : event.getFromProfiles()) {
            updateProfile(inventories, event.getPlayer(), persistingProfile);
        }
        for (PersistingProfile persistingProfile : event.getToProfiles()) {
            updatePlayer(inventories, event.getPlayer(), persistingProfile);
        }
        Logging.finer("=== " + event.getPlayer().getName() + "'s " + event.getCause() + " handling complete! ===");
    }

    static void updateProfile(final Inventories inventories, final Player player, final PersistingProfile profile) {
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
                    + " for player " + profile.getProfile().getPlayerName());
        }
        inventories.getData().updatePlayerData(profile.getProfile());
    }

    static void updatePlayer(final Inventories inventories, final Player player, final PersistingProfile profile) {
        StringBuilder defaulted = new StringBuilder();
        StringBuilder loaded = new StringBuilder();
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
                    + profile.getProfile().getPlayerName() + " for "
                    + profile.getProfile().getContainerType() + ":" + profile.getProfile().getContainerName()
                    + " (" + profile.getProfile().getProfileType() + ")");
        }
        if (!defaulted.toString().isEmpty()) {
            Logging.finer("Defaulted: " + defaulted.toString() + " for "
                    + profile.getProfile().getPlayerName() + " for "
                    + profile.getProfile().getContainerType() + ":" + profile.getProfile().getContainerName()
                    + " (" + profile.getProfile().getProfileType() + ")");
        }
    }
}

