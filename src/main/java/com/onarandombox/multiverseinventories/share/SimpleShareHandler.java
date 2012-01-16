package com.onarandombox.multiverseinventories.share;

import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.profile.PersistingProfile;
import com.onarandombox.multiverseinventories.profile.SimplePersistingProfile;
import com.onarandombox.multiverseinventories.group.WorldGroup;
import com.onarandombox.multiverseinventories.permission.MVIPerms;
import com.onarandombox.multiverseinventories.profile.PlayerProfile;
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
    public void addFromProfile(String dataName, Shares shares, PlayerProfile profile) {
        this.getFromProfiles().add(new SimplePersistingProfile(dataName, shares, profile));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addToProfile(String dataName, Shares shares, PlayerProfile profile) {
        this.getToProfiles().add(new SimplePersistingProfile(dataName, shares, profile));
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
    public void handleShares(Shares shares) {
        WorldProfile fromWorldProfile = this.plugin.getProfileManager()
                .getWorldProfile(this.getFromWorld().getName());
        PlayerProfile fromWorldPlayerProfile = fromWorldProfile.getPlayerData(this.getPlayer());
        WorldProfile toWorldProfile = this.plugin.getProfileManager()
                .getWorldProfile(this.getToWorld().getName());
        PlayerProfile toWorldPlayerProfile = toWorldProfile.getPlayerData(this.getPlayer());

        MVILog.debug(this.getPlayer().getName() + " switching from " + this.getFromWorld().getName()
                + " to " + this.getToWorld().getName() + " with shares: " + shares.toString());
        // persist current stats for previous world if not sharing
        // then load any saved data
        if (!shares.isSharing(Sharable.INVENTORY)) {
            fromWorldPlayerProfile.setInventoryContents(this.getPlayer().getInventory().getContents());
            fromWorldPlayerProfile.setArmorContents(this.getPlayer().getInventory().getArmorContents());
            this.getPlayer().getInventory().clear();
            this.getPlayer().getInventory().setContents(toWorldPlayerProfile.getInventoryContents());
            this.getPlayer().getInventory().setArmorContents(toWorldPlayerProfile.getArmorContents());
        }
        if (!shares.isSharing(Sharable.HEALTH)) {
            fromWorldPlayerProfile.setHealth(this.getPlayer().getHealth());
            this.getPlayer().setHealth(toWorldPlayerProfile.getHealth());
        }
        if (!shares.isSharing(Sharable.HUNGER)) {
            fromWorldPlayerProfile.setFoodLevel(this.getPlayer().getFoodLevel());
            fromWorldPlayerProfile.setExhaustion(this.getPlayer().getExhaustion());
            fromWorldPlayerProfile.setSaturation(this.getPlayer().getSaturation());
            this.getPlayer().setFoodLevel(toWorldPlayerProfile.getFoodLevel());
            this.getPlayer().setExhaustion(toWorldPlayerProfile.getExhaustion());
            this.getPlayer().setSaturation(toWorldPlayerProfile.getSaturation());
        }
        if (!shares.isSharing(Sharable.EXPERIENCE)) {
            fromWorldPlayerProfile.setExp(this.getPlayer().getExp());
            fromWorldPlayerProfile.setLevel(this.getPlayer().getLevel());
            fromWorldPlayerProfile.setTotalExperience(this.getPlayer().getTotalExperience());
            this.getPlayer().setExp(toWorldPlayerProfile.getExp());
            this.getPlayer().setLevel(toWorldPlayerProfile.getLevel());
            this.getPlayer().setTotalExperience(toWorldPlayerProfile.getTotalExperience());
        }
        /*
        if (!shares.isSharing(Sharable.EFFECTS)) {
            // Where is the effects API??
        }
        */

        this.plugin.getData().updatePlayerData(this.getFromWorld().getName(), fromWorldPlayerProfile);
        this.plugin.getData().updatePlayerData(this.getToWorld().getName(), toWorldPlayerProfile);
    }

    public void handleSharing() {
        boolean usingBypass = this.plugin.getSettings().isUsingBypassPerms();

        if (usingBypass && MVIPerms.BYPASS_WORLD.hasBypass(this.getPlayer(),
                this.getToWorld().getName())) {
            return;
        }

        List<WorldGroup> fromWorldGroups = this.plugin.getGroupManager()
                .getWorldGroups(this.getFromWorld().getName());
        for (WorldGroup fromWorldGroup : fromWorldGroups) {
            PlayerProfile profile = fromWorldGroup.getPlayerData(player);
            if (!fromWorldGroup.containsWorld(this.getToWorld().getName())) {
                this.addFromProfile(fromWorldGroup.getName(),
                        new SimpleShares(Sharable.ALL), profile);
            } else {
                if (!fromWorldGroup.getShares().isSharing(Sharable.ALL)) {
                    EnumSet<Sharable> sharing =
                            EnumSet.complementOf(fromWorldGroup.getShares().getSharables());
                    sharing.remove(Sharable.ALL);
                    this.addFromProfile(fromWorldGroup.getName(), new SimpleShares(sharing), profile);
                }
            }
        }
        WorldProfile fromWorldProfile = this.plugin.getProfileManager()
                .getWorldProfile(this.getFromWorld().getName());
        this.addFromProfile(this.getFromWorld().getName(), new SimpleShares(Sharable.ALL),
                fromWorldProfile.getPlayerData(this.getPlayer()));

        List<WorldGroup> toWorldGroups = this.plugin.getGroupManager()
                .getWorldGroups(this.getToWorld().getName());
        if (!toWorldGroups.isEmpty()) {
            for (WorldGroup toWorldGroup : toWorldGroups) {
                if (!usingBypass || !MVIPerms.BYPASS_GROUP.hasBypass(this.getPlayer(),
                        toWorldGroup.getName())) {
                    PlayerProfile profile = toWorldGroup.getPlayerData(player);
                    if (!toWorldGroup.containsWorld(this.getFromWorld().getName())) {
                        this.addToProfile(toWorldGroup.getName(),
                                new SimpleShares(Sharable.ALL), profile);
                    } else {
                        if (!toWorldGroup.getShares().isSharing(Sharable.ALL)) {
                            EnumSet<Sharable> shares =
                                    EnumSet.complementOf(toWorldGroup.getShares().getSharables());
                            shares.remove(Sharable.ALL);
                            this.addToProfile(toWorldGroup.getName(),
                                    new SimpleShares(shares), profile);
                        }
                    }
                }
            }
        } else {
            WorldProfile toWorldProfile = this.plugin.getProfileManager()
                    .getWorldProfile(this.getToWorld().getName());
            this.addToProfile(this.getToWorld().getName(), new SimpleShares(Sharable.ALL),
                    toWorldProfile.getPlayerData(this.getPlayer()));
        }

        this.completeSharing();
    }

    private void completeSharing() {
        if (this.getToProfiles().isEmpty()) {
            MVILog.debug("No toProfiles, nothing to process.");
            return;
        }
        for (PersistingProfile persistingProfile : this.getFromProfiles()) {
            //TODO FINISH
        }
    }

}
