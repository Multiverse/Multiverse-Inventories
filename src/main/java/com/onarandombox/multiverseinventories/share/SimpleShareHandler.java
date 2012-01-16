package com.onarandombox.multiverseinventories.share;

import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.group.PersistingGroup;
import com.onarandombox.multiverseinventories.group.SimplePersistingGroup;
import com.onarandombox.multiverseinventories.group.WorldGroup;
import com.onarandombox.multiverseinventories.profile.PlayerProfile;
import com.onarandombox.multiverseinventories.profile.WorldProfile;
import com.onarandombox.multiverseinventories.util.MVILog;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple implementation of ShareHandler.
 */
public class SimpleShareHandler implements ShareHandler {

    private List<PersistingGroup> fromGroups;
    private List<PersistingGroup> toGroups;
    private Player player;
    private World fromWorld;
    private World toWorld;
    private MultiverseInventories plugin;

    public SimpleShareHandler(MultiverseInventories plugin, Player player,
                              World fromWorld, World toWorld) {
        this.fromGroups = new ArrayList<PersistingGroup>();
        this.toGroups = new ArrayList<PersistingGroup>();
        this.player = player;
        this.fromWorld = fromWorld;
        this.toWorld = toWorld;
        this.plugin = plugin;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addFromGroup(Sharable sharable, WorldGroup group) {
        this.getFromGroups().add(new SimplePersistingGroup(sharable, group));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addToGroup(Sharable sharable, WorldGroup group) {
        this.getToGroups().add(new SimplePersistingGroup(sharable, group));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PersistingGroup> getFromGroups() {
        return this.fromGroups;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PersistingGroup> getToGroups() {
        return this.toGroups;
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
}
