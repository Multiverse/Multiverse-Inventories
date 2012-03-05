package com.onarandombox.multiverseinventories;

import com.onarandombox.multiverseinventories.api.Inventories;
import com.onarandombox.multiverseinventories.api.profile.PlayerProfile;
import com.onarandombox.multiverseinventories.api.profile.ProfileContainer;
import com.onarandombox.multiverseinventories.api.profile.WorldGroupProfile;
import com.onarandombox.multiverseinventories.api.profile.WorldProfile;
import com.onarandombox.multiverseinventories.share.Sharable;
import com.onarandombox.multiverseinventories.share.Sharables;
import com.onarandombox.multiverseinventories.share.Shares;
import com.onarandombox.multiverseinventories.util.Logging;
import com.onarandombox.multiverseinventories.util.Perm;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple implementation of ShareHandler.
 */
final class ShareHandler {

    private List<PersistingProfile> fromProfiles;
    private List<PersistingProfile> toProfiles;
    private Player player;
    private World fromWorld;
    private World toWorld;
    private Inventories inventories;
    private boolean hasBypass = false;

    public ShareHandler(Inventories inventories, Player player,
                        World fromWorld, World toWorld) {
        this.fromProfiles = new ArrayList<PersistingProfile>();
        this.toProfiles = new ArrayList<PersistingProfile>();
        this.player = player;
        this.fromWorld = fromWorld;
        this.toWorld = toWorld;
        this.inventories = inventories;
    }

    /**
     * @return The profiles for the world/groups the player is coming from.
     */
    public List<PersistingProfile> getFromProfiles() {
        return this.fromProfiles;
    }

    /**
     * @return The profiles for the world/groups the player is going to.
     */
    public List<PersistingProfile> getToProfiles() {
        return this.toProfiles;
    }

    /**
     * @return The world travelling from.
     */
    public World getFromWorld() {
        return this.fromWorld;
    }

    /**
     * @return The world travelling to.
     */
    public World getToWorld() {
        return this.toWorld;
    }

    /**
     * @return The player involved in this sharing transaction.
     */
    public Player getPlayer() {
        return this.player;
    }

    /**
     * @param container The group/world the player's data is associated with.
     * @param shares    What from this group needs to be saved.
     * @param profile   The player player that will need data saved to.
     */
    public void addFromProfile(ProfileContainer container, Shares shares, PlayerProfile profile) {
        this.getFromProfiles().add(new DefaultPersistingProfile(container.getDataName(), shares, profile));
    }

    /**
     * @param container The group/world the player's data is associated with.
     * @param shares    What from this group needs to be loaded.
     * @param profile   The player player that will need data loaded from.
     */
    public void addToProfile(ProfileContainer container, Shares shares, PlayerProfile profile) {
        this.getToProfiles().add(new DefaultPersistingProfile(container.getDataName(), shares, profile));
    }

    /**
     * Finalizes the transfer from one world to another.  This handles the switching
     * inventories/stats for a player and persisting the changes.
     */
    public void handleSharing() {
        Logging.finer("=== " + this.getPlayer().getName() + " traveling from world: " + this.getFromWorld().getName()
                + " to " + "world: " + this.getToWorld().getName() + " ===");
        // Grab the player from the world they're coming from to save their stuff to every time.
        WorldProfile fromWorldProfile = this.inventories.getWorldManager()
                .getWorldProfile(this.getFromWorld().getName());
        this.addFromProfile(fromWorldProfile, Sharables.allOf(),
                fromWorldProfile.getPlayerData(this.getPlayer()));

        if (Perm.BYPASS_WORLD.hasBypass(this.getPlayer(), this.getToWorld().getName())) {
            this.hasBypass = true;
            completeSharing();
            return;
        }

        // Get any groups we need to save stuff to.
        List<WorldGroupProfile> fromWorldGroups = this.inventories.getGroupManager()
                .getGroupsForWorld(this.getFromWorld().getName());
        for (WorldGroupProfile fromWorldGroup : fromWorldGroups) {
            PlayerProfile profile = fromWorldGroup.getPlayerData(this.getPlayer());
            if (!fromWorldGroup.containsWorld(this.getToWorld().getName())) {
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
                .getGroupsForWorld(this.getToWorld().getName());;
        if (!toWorldGroups.isEmpty()) {
            // Get groups we need to load from
            for (WorldGroupProfile toWorldGroup : toWorldGroups) {
                if (Perm.BYPASS_GROUP.hasBypass(this.getPlayer(), toWorldGroup.getName())) {
                    this.hasBypass = true;
                } else {
                    PlayerProfile profile = toWorldGroup.getPlayerData(this.getPlayer());
                    if (!toWorldGroup.containsWorld(this.getFromWorld().getName())) {
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
                    .getWorldProfile(this.getToWorld().getName());
            this.addToProfile(toWorldProfile, Sharables.allOf(),
                    toWorldProfile.getPlayerData(this.getPlayer()));
            sharesToUpdate = Sharables.allOf();
        }
        // We need to fill in any sharables that are not going to be transferred with what's saved in the world file.
        if (!sharesToUpdate.isSharing(Sharables.all())) {
            sharesToUpdate = Sharables.complementOf(sharesToUpdate);
            // Get world we need to load from.
            Logging.finer("No groups for toWorld.");
            WorldProfile toWorldProfile = this.inventories.getWorldManager()
                    .getWorldProfile(this.getToWorld().getName());
            this.addToProfile(toWorldProfile, sharesToUpdate,
                    toWorldProfile.getPlayerData(this.getPlayer()));
        }

        this.completeSharing();
    }

    private void completeSharing() {
        Logging.finer("Travel affected by " + this.getFromProfiles().size() + " fromProfiles and "
                + this.getToProfiles().size() + " toProfiles");
        // This if statement should never happen, really.
        if (this.getToProfiles().isEmpty()) {
            if (hasBypass) {
                Logging.fine(this.getPlayer().getName() + " has bypass permission for 1 or more world/groups!");
            } else {
                Logging.finer("No toProfiles...");
            }
            if (!this.getFromProfiles().isEmpty()) {
                updateProfile(this.getFromProfiles().get(0));
            } else {
                Logging.warning("No fromWorld to save to");
            }
            Logging.finer("=== " + this.getPlayer().getName() + "'s travel handling complete! ===");
            return;
        }

        for (PersistingProfile persistingProfile : this.getFromProfiles()) {
            updateProfile(persistingProfile);
        }
        for (PersistingProfile persistingProfile : this.getToProfiles()) {
            updatePlayer(persistingProfile);
        }
        Logging.finer("=== " + this.getPlayer().getName() + "'s travel handling complete! ===");
    }

    private void updateProfile(PersistingProfile profile) {
        for (Sharable sharable : profile.getShares()) {
            sharable.updateProfile(profile.getProfile(), this.getPlayer());
        }
        Logging.finest("Persisting: " + profile.getShares().toString() + " to "
                + profile.getProfile().getType() + ":" + profile.getDataName()
                + " for player " + profile.getProfile().getPlayer().getName());
        this.inventories.getData().updatePlayerData(profile.getDataName(), profile.getProfile());
    }

    private void updatePlayer(PersistingProfile profile) {
        for (Sharable sharable : profile.getShares()) {
            sharable.updatePlayer(this.getPlayer(), profile.getProfile());
        }
        Logging.finest("Updating " + profile.getShares().toString() + " for "
                + profile.getProfile().getPlayer().getName() + "for "
                + profile.getProfile().getType() + ":" + profile.getDataName());
    }
}

