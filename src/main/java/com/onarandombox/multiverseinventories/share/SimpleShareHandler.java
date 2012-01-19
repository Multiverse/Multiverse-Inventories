package com.onarandombox.multiverseinventories.share;

import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.group.WorldGroup;
import com.onarandombox.multiverseinventories.permission.MVIPerms;
import com.onarandombox.multiverseinventories.profile.PersistingProfile;
import com.onarandombox.multiverseinventories.profile.PlayerProfile;
import com.onarandombox.multiverseinventories.profile.ProfileContainer;
import com.onarandombox.multiverseinventories.profile.SimplePersistingProfile;
import com.onarandombox.multiverseinventories.profile.WorldProfile;
import com.onarandombox.multiverseinventories.util.MVILog;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * Simple implementation of ShareHandler.
 */
public class SimpleShareHandler implements ShareHandler {

    private List<PersistingProfile> fromProfiles;
    private List<PersistingProfile> toProfiles;
    private Player player;
    private World fromWorld;
    private World toWorld;
    private MultiverseInventories plugin;
    private boolean hasBypass = false;

    public SimpleShareHandler(MultiverseInventories plugin, Player player,
                              World fromWorld, World toWorld) {
        this.fromProfiles = new ArrayList<PersistingProfile>();
        this.toProfiles = new ArrayList<PersistingProfile>();
        this.player = player;
        this.fromWorld = fromWorld;
        this.toWorld = toWorld;
        this.plugin = plugin;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addFromProfile(ProfileContainer container, Shares shares, PlayerProfile profile) {
        this.getFromProfiles().add(new SimplePersistingProfile(container.getDataName(), shares, profile));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addToProfile(ProfileContainer container, Shares shares, PlayerProfile profile) {
        this.getToProfiles().add(new SimplePersistingProfile(container.getDataName(), shares, profile));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PersistingProfile> getFromProfiles() {
        return this.fromProfiles;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PersistingProfile> getToProfiles() {
        return this.toProfiles;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public World getFromWorld() {
        return this.fromWorld;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public World getToWorld() {
        return this.toWorld;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Player getPlayer() {
        return this.player;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleSharing() {
        WorldProfile fromWorldProfile = this.plugin.getProfileManager()
                .getWorldProfile(this.getFromWorld().getName());
        this.addFromProfile(fromWorldProfile, new SimpleShares(Sharable.all()),
                fromWorldProfile.getPlayerData(this.getPlayer()));

        boolean usingBypass = this.plugin.getSettings().isUsingBypassPerms();

        if (usingBypass && MVIPerms.BYPASS_WORLD.hasBypass(this.getPlayer(),
                this.getToWorld().getName())) {
            this.hasBypass = true;
            completeSharing();
            return;
        }

        List<WorldGroup> fromWorldGroups = this.plugin.getGroupManager()
                .getGroupsForWorld(this.getFromWorld().getName());
        for (WorldGroup fromWorldGroup : fromWorldGroups) {
            PlayerProfile profile = fromWorldGroup.getPlayerData(this.getPlayer());
            if (!fromWorldGroup.containsWorld(this.getToWorld().getName())) {
                this.addFromProfile(fromWorldGroup,
                        new SimpleShares(Sharable.all()), profile);
            } else {
                if (!fromWorldGroup.getShares().isSharing(Sharable.all())) {
                    EnumSet<Sharable> sharing =
                            EnumSet.complementOf(fromWorldGroup.getShares().getSharables());
                    this.addFromProfile(fromWorldGroup, new SimpleShares(sharing), profile);
                }
            }
        }

        List<WorldGroup> toWorldGroups = this.plugin.getGroupManager()
                .getGroupsForWorld(this.getToWorld().getName());
        if (!toWorldGroups.isEmpty()) {
            for (WorldGroup toWorldGroup : toWorldGroups) {
                if (usingBypass && MVIPerms.BYPASS_GROUP.hasBypass(this.getPlayer(),
                        toWorldGroup.getName())) {
                    this.hasBypass = true;
                } else {
                    PlayerProfile profile = toWorldGroup.getPlayerData(this.getPlayer());
                    if (!toWorldGroup.containsWorld(this.getFromWorld().getName())) {
                        this.addToProfile(toWorldGroup,
                                new SimpleShares(Sharable.all()), profile);
                    } else {
                        if (!toWorldGroup.getShares().isSharing(Sharable.all())) {
                            EnumSet<Sharable> shares =
                                    EnumSet.complementOf(toWorldGroup.getShares().getSharables());
                            this.addToProfile(toWorldGroup,
                                    new SimpleShares(shares), profile);
                        }
                    }
                }
            }
        } else {
            WorldProfile toWorldProfile = this.plugin.getProfileManager()
                    .getWorldProfile(this.getToWorld().getName());
            this.addToProfile(toWorldProfile, new SimpleShares(Sharable.all()),
                    toWorldProfile.getPlayerData(this.getPlayer()));
        }

        this.completeSharing();
    }

    private void completeSharing() {
        if (this.getToProfiles().isEmpty()) {
            if (hasBypass) {
                MVILog.debug("Player has bypass permission for 1 or more world/groups!");
            } else {
                MVILog.debug("No toProfiles...");
            }
            if (!this.getFromProfiles().isEmpty()) {
                updateProfile(this.getFromProfiles().get(0));
            } else {
                MVILog.warning("No fromWorld to save to");
            }
            return;
        }
        for (PersistingProfile persistingProfile : this.getFromProfiles()) {
            updateProfile(persistingProfile);
        }
        for (PersistingProfile persistingProfile : this.getToProfiles()) {
            updatePlayer(persistingProfile);
        }
    }

    private void updateProfile(PersistingProfile profile) {
        for (Sharable sharable : profile.getShares().getSharables()) {
            sharable.updateProfile(profile.getProfile(), this.getPlayer());
        }
        MVILog.debug("Persisting: " + profile.getShares().toString() + " to "
                + profile.getProfile().getType() + ":" + profile.getDataName()
                + " for player " + profile.getProfile().getPlayer().getName());
        this.plugin.getData().updatePlayerData(profile.getDataName(), profile.getProfile());
    }

    private void updatePlayer(PersistingProfile profile) {
        for (Sharable sharable : profile.getShares().getSharables()) {
            sharable.updatePlayer(this.getPlayer(), profile.getProfile());
        }
        MVILog.debug("Updating " + profile.getShares().toString() + " for "
                + profile.getProfile().getPlayer().getName() + "for "
                + profile.getProfile().getType() + ":" + profile.getDataName());
    }
}
