package com.onarandombox.multiverseinventories;

import com.dumptruckman.minecraft.util.Logging;
import com.onarandombox.multiverseinventories.profile.container.ContainerType;
import com.onarandombox.multiverseinventories.share.PersistingProfile;
import com.onarandombox.multiverseinventories.share.Sharable;
import com.onarandombox.multiverseinventories.share.Sharables;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

class ShareHandlingUpdater {

    static void updateProfile(final MultiverseInventories inventories,
                              final Player player,
                              final PersistingProfile profile) {
        new ShareHandlingUpdater(inventories, player, profile).updateProfile();
    }

    static void updatePlayer(final MultiverseInventories inventories,
                             final Player player,
                             final PersistingProfile profile) {
        new ShareHandlingUpdater(inventories, player, profile).updatePlayer();
    }

    private final MultiverseInventories inventories;
    private final Player player;
    private final PersistingProfile profile;

    private final List<Sharable<?>> saved = new ArrayList<>(Sharables.all().size());
    private final List<Sharable<?>> loaded = new ArrayList<>(Sharables.all().size());
    private final List<Sharable<?>> defaulted = new ArrayList<>(Sharables.all().size());

    private ShareHandlingUpdater(MultiverseInventories inventories, Player player, PersistingProfile profile) {
        this.inventories = inventories;
        this.player = player;
        this.profile = profile;
    }

    private void updateProfile() {
        for (Sharable<?> sharable : profile.getShares()) {
            if (isSharableUsed(sharable)) {
                saved.add(sharable);
                sharable.getHandler().updateProfile(profile.getProfile(), player);
            }
        }
        if (saved.size() > 0) {
            Logging.finer("Persisted: " + StringUtils.join(saved, ", ") + " to "
                    + profile.getProfile().getContainerType() + ":" + profile.getProfile().getContainerName()
                    + " (" + profile.getProfile().getProfileType() + ")"
                    + " for player " + profile.getProfile().getPlayer().getName());
        }
        inventories.getData().updatePlayerData(profile.getProfile());
    }

    private void updatePlayer() {
        player.closeInventory();
        for (Sharable<?> sharable : profile.getShares()) {
            if (isSharableUsed(sharable)) {
                if (sharable.getHandler().updatePlayer(player, profile.getProfile())) {
                    loaded.add(sharable);
                } else {
                    defaulted.add(sharable);
                }
            }
        }
        if (!loaded.isEmpty()) {
            Logging.finer("Updated: " + loaded.toString() + " for "
                    + profile.getProfile().getPlayer().getName() + " for "
                    + profile.getProfile().getContainerType() + ":" + profile.getProfile().getContainerName()
                    + " (" + profile.getProfile().getProfileType() + ")");
        }
        if (!defaulted.isEmpty()) {
            Logging.finer("Defaulted: " + defaulted.toString() + " for "
                    + profile.getProfile().getPlayer().getName() + " for "
                    + profile.getProfile().getContainerType() + ":" + profile.getProfile().getContainerName()
                    + " (" + profile.getProfile().getProfileType() + ")");
        }
    }

    private boolean isSharableUsed(Sharable<?> sharable) {
        if (sharable.isOptional()) {
            if (!inventories.getMVIConfig().getOptionalShares().contains(sharable)) {
                Logging.finest("Ignoring optional share: " + sharable.getNames()[0]);
                return false;
            }
            if (profile.getProfile().getContainerType() == ContainerType.WORLD
                    && !inventories.getMVIConfig().usingOptionalsForUngrouped()) {
                Logging.finest("Ignoring optional share '" + sharable.getNames()[0] + "' for ungrouped world!");
                return false;
            }
        }
        return true;
    }
}
