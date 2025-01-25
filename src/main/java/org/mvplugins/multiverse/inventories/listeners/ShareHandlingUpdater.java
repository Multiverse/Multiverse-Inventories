package org.mvplugins.multiverse.inventories.listeners;

import com.dumptruckman.minecraft.util.Logging;
import org.mvplugins.multiverse.inventories.MultiverseInventories;
import org.mvplugins.multiverse.inventories.config.InventoriesConfig;
import org.mvplugins.multiverse.inventories.profile.PersistingProfile;
import org.mvplugins.multiverse.inventories.profile.ProfileDataSource;
import org.mvplugins.multiverse.inventories.profile.container.ContainerType;
import org.mvplugins.multiverse.inventories.share.Sharable;
import org.mvplugins.multiverse.inventories.share.Sharables;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
        if (!saved.isEmpty()) {
            Logging.finer("Persisted: "
                    + saved.stream().map(Objects::toString).collect(Collectors.joining(", ")) + " to "
                    + profile.getProfile().getContainerType() + ":" + profile.getProfile().getContainerName()
                    + " (" + profile.getProfile().getProfileType() + ")"
                    + " for player " + profile.getProfile().getPlayer().getName());
        }
        inventories.getServiceLocator().getService(ProfileDataSource.class).updatePlayerData(profile.getProfile());
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
        var config = inventories.getServiceLocator().getService(InventoriesConfig.class);
        if (sharable.isOptional()) {
            if (!config.getOptionalShares().contains(sharable)) {
                Logging.finest("Ignoring optional share: " + sharable.getNames()[0]);
                return false;
            }
            if (profile.getProfile().getContainerType() == ContainerType.WORLD
                    && !config.usingOptionalsForUngrouped()) {
                Logging.finest("Ignoring optional share '" + sharable.getNames()[0] + "' for ungrouped world!");
                return false;
            }
        }
        return true;
    }
}
