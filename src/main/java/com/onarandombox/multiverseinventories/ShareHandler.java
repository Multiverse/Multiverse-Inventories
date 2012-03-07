package com.onarandombox.multiverseinventories;

import com.onarandombox.multiverseinventories.api.Inventories;
import com.onarandombox.multiverseinventories.api.share.PersistingProfile;
import com.onarandombox.multiverseinventories.api.profile.PlayerProfile;
import com.onarandombox.multiverseinventories.api.profile.ProfileContainer;
import com.onarandombox.multiverseinventories.api.profile.WorldGroupProfile;
import com.onarandombox.multiverseinventories.api.profile.WorldProfile;
import com.onarandombox.multiverseinventories.event.MVInventoryHandlingEvent;
import com.onarandombox.multiverseinventories.share.Sharable;
import com.onarandombox.multiverseinventories.share.Sharables;
import com.onarandombox.multiverseinventories.share.Shares;
import com.onarandombox.multiverseinventories.util.Logging;
import com.onarandombox.multiverseinventories.util.Perm;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple implementation of ShareHandler.
 */
final class ShareHandler {

    private final MVInventoryHandlingEvent event;
    private final Inventories inventories;
    private boolean hasBypass = false;

    public ShareHandler(Inventories inventories, Player player,
                        World fromWorld, World toWorld) {
        this.event = new MVInventoryHandlingEvent(player, fromWorld, toWorld);
        this.inventories = inventories;
    }

    /**
     * @param container The group/world the player's data is associated with.
     * @param shares    What from this group needs to be saved.
     * @param profile   The player player that will need data saved to.
     */
    public void addFromProfile(ProfileContainer container, Shares shares, PlayerProfile profile) {
        event.getFromProfiles().add(new DefaultPersistingProfile(container.getDataName(), shares, profile));
    }

    /**
     * @param container The group/world the player's data is associated with.
     * @param shares    What from this group needs to be loaded.
     * @param profile   The player player that will need data loaded from.
     */
    public void addToProfile(ProfileContainer container, Shares shares, PlayerProfile profile) {
        event.getToProfiles().add(new DefaultPersistingProfile(container.getDataName(), shares, profile));
    }

    /**
     * Finalizes the transfer from one world to another.  This handles the switching
     * inventories/stats for a player and persisting the changes.
     */
    public void handleSharing() {
        Logging.finer("=== " + event.getPlayer().getName() + " traveling from world: " + event.getFromWorld().getName()
                + " to " + "world: " + event.getToWorld().getName() + " ===");
        // Grab the player from the world they're coming from to save their stuff to every time.
        WorldProfile fromWorldProfile = this.inventories.getWorldManager()
                .getWorldProfile(event.getFromWorld().getName());
        this.addFromProfile(fromWorldProfile, Sharables.allOf(),
                fromWorldProfile.getPlayerData(event.getPlayer()));

        if (Perm.BYPASS_WORLD.hasBypass(event.getPlayer(), event.getToWorld().getName())) {
            this.hasBypass = true;
            completeSharing();
            return;
        }

        // Get any groups we need to save stuff to.
        List<WorldGroupProfile> fromWorldGroups = this.inventories.getGroupManager()
                .getGroupsForWorld(event.getFromWorld().getName());
        for (WorldGroupProfile fromWorldGroup : fromWorldGroups) {
            PlayerProfile profile = fromWorldGroup.getPlayerData(event.getPlayer());
            if (!fromWorldGroup.containsWorld(event.getToWorld().getName())) {
                this.addFromProfile(fromWorldGroup,
                        Sharables.allOf(), profile);
            } else {
                if (!fromWorldGroup.getShares().isSharing(Sharables.all())) {
                    this.addFromProfile(fromWorldGroup, Sharables.fromShares(fromWorldGroup.getShares()), profile);
                }
            }
        }
        if (fromWorldGroups.isEmpty()) {
            Logging.finer("No groups for fromWorld.");
        }
        Shares sharesToUpdate = Sharables.noneOf();
        List<WorldGroupProfile> toWorldGroups = this.inventories.getGroupManager()
                .getGroupsForWorld(event.getToWorld().getName());
        if (!toWorldGroups.isEmpty()) {
            // Get groups we need to load from
            for (WorldGroupProfile toWorldGroup : toWorldGroups) {
                if (Perm.BYPASS_GROUP.hasBypass(event.getPlayer(), toWorldGroup.getName())) {
                    this.hasBypass = true;
                } else {
                    PlayerProfile profile = toWorldGroup.getPlayerData(event.getPlayer());
                    if (!toWorldGroup.containsWorld(event.getFromWorld().getName())) {
                        Shares sharesToAdd = Sharables.allOf();
                        sharesToUpdate.addAll(sharesToAdd);
                        this.addToProfile(toWorldGroup,
                                sharesToAdd, profile);
                    } else {
                        if (!toWorldGroup.getShares().isSharing(Sharables.all())) {
                            Shares sharesToAdd = Sharables.fromShares(toWorldGroup.getShares());
                            sharesToUpdate.addAll(sharesToAdd);
                            this.addToProfile(toWorldGroup, sharesToAdd, profile);
                        } else {
                            sharesToUpdate = Sharables.allOf();
                        }
                    }
                }
            }
        } else {
            // Get world we need to load from.
            Logging.finer("No groups for toWorld.");
            WorldProfile toWorldProfile = this.inventories.getWorldManager()
                    .getWorldProfile(event.getToWorld().getName());
            this.addToProfile(toWorldProfile, Sharables.allOf(),
                    toWorldProfile.getPlayerData(event.getPlayer()));
            sharesToUpdate = Sharables.allOf();
        }
        // We need to fill in any sharables that are not going to be transferred with what's saved in the world file.
        if (!sharesToUpdate.isSharing(Sharables.all())) {
            sharesToUpdate = Sharables.complementOf(sharesToUpdate);
            // Get world we need to load from.
            Logging.finer("No groups for toWorld.");
            WorldProfile toWorldProfile = this.inventories.getWorldManager()
                    .getWorldProfile(event.getToWorld().getName());
            this.addToProfile(toWorldProfile, sharesToUpdate,
                    toWorldProfile.getPlayerData(event.getPlayer()));
        }

        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            this.completeSharing();
        }
    }

    private void completeSharing() {
        Logging.finer("Travel affected by " + event.getFromProfiles().size() + " fromProfiles and "
                + event.getToProfiles().size() + " toProfiles");
        // This if statement should never happen, really.
        if (event.getToProfiles().isEmpty()) {
            if (hasBypass) {
                Logging.fine(event.getPlayer().getName() + " has bypass permission for 1 or more world/groups!");
            } else {
                Logging.finer("No toProfiles...");
            }
            if (!event.getFromProfiles().isEmpty()) {
                updateProfile(event.getFromProfiles().get(0));
            } else {
                Logging.warning("No fromWorld to save to");
            }
            Logging.finer("=== " + event.getPlayer().getName() + "'s travel handling complete! ===");
            return;
        }

        for (PersistingProfile persistingProfile : event.getFromProfiles()) {
            updateProfile(persistingProfile);
        }
        for (PersistingProfile persistingProfile : event.getToProfiles()) {
            updatePlayer(persistingProfile);
        }
        Logging.finer("=== " + event.getPlayer().getName() + "'s travel handling complete! ===");
    }

    private void updateProfile(PersistingProfile profile) {
        for (Sharable sharable : profile.getShares()) {
            sharable.updateProfile(profile.getProfile(), event.getPlayer());
        }
        Logging.finest("Persisting: " + profile.getShares().toString() + " to "
                + profile.getProfile().getType() + ":" + profile.getDataName()
                + " for player " + profile.getProfile().getPlayer().getName());
        this.inventories.getData().updatePlayerData(profile.getDataName(), profile.getProfile());
    }

    private void updatePlayer(PersistingProfile profile) {
        for (Sharable sharable : profile.getShares()) {
            sharable.updatePlayer(event.getPlayer(), profile.getProfile());
        }
        Logging.finest("Updating " + profile.getShares().toString() + " for "
                + profile.getProfile().getPlayer().getName() + "for "
                + profile.getProfile().getType() + ":" + profile.getDataName());
    }
}

