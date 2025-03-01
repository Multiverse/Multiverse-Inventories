package org.mvplugins.multiverse.inventories.handleshare;

import com.dumptruckman.minecraft.util.Logging;
import org.mvplugins.multiverse.inventories.MultiverseInventories;
import org.mvplugins.multiverse.inventories.config.InventoriesConfig;
import org.mvplugins.multiverse.inventories.profile.ProfileDataSource;
import org.mvplugins.multiverse.inventories.profile.container.ContainerType;
import org.mvplugins.multiverse.inventories.share.Sharable;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class ShareHandlingUpdater {

    public static void updateProfile(final MultiverseInventories inventories,
                              final Player player,
                              final PersistingProfile profile) {
        new ShareHandlingUpdater(inventories, player, profile).updateProfile();
    }

    public static void updatePlayer(final MultiverseInventories inventories,
                             final Player player,
                             final PersistingProfile profile) {
        new ShareHandlingUpdater(inventories, player, profile).updatePlayer();
    }

    private final MultiverseInventories inventories;
    private final Player player;
    private final PersistingProfile profile;

    private ShareHandlingUpdater(MultiverseInventories inventories, Player player, PersistingProfile profile) {
        this.inventories = inventories;
        this.player = player;
        this.profile = profile;
    }

    private void updateProfile() {
        if (profile.shares().isEmpty()) {
            return;
        }
        for (Sharable<?> sharable : profile.shares()) {
            sharable.getHandler().updateProfile(profile.profile(), player);
        }
        Logging.finer("Persisted: " + profile.shares() + " to "
                + profile.profile().getContainerType() + ":" + profile.profile().getContainerName()
                + " (" + profile.profile().getProfileType() + ")"
                + " for player " + profile.profile().getPlayer().getName());
        inventories.getServiceLocator().getService(ProfileDataSource.class).updatePlayerData(profile.profile());
    }

    private void updatePlayer() {
        player.closeInventory();

        final List<Sharable<?>> loaded = new ArrayList<>(profile.shares().size());
        final List<Sharable<?>> defaulted = new ArrayList<>(profile.shares().size());

        for (Sharable<?> sharable : profile.shares()) {
            if (sharable.getHandler().updatePlayer(player, profile.profile())) {
                loaded.add(sharable);
            } else {
                defaulted.add(sharable);
            }
        }
        if (!loaded.isEmpty()) {
            Logging.finer("Updated: " + loaded + " for "
                    + profile.profile().getPlayer().getName() + " for "
                    + profile.profile().getContainerType() + ":" + profile.profile().getContainerName()
                    + " (" + profile.profile().getProfileType() + ")");
        }
        if (!defaulted.isEmpty()) {
            Logging.finer("Defaulted: " + defaulted + " for "
                    + profile.profile().getPlayer().getName() + " for "
                    + profile.profile().getContainerType() + ":" + profile.profile().getContainerName()
                    + " (" + profile.profile().getProfileType() + ")");
        }
    }
}
