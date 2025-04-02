package org.mvplugins.multiverse.inventories.handleshare;

import com.dumptruckman.minecraft.util.Logging;
import org.mvplugins.multiverse.external.vavr.control.Try;
import org.mvplugins.multiverse.inventories.MultiverseInventories;
import org.mvplugins.multiverse.inventories.profile.PlayerProfile;
import org.mvplugins.multiverse.inventories.profile.ProfileDataSource;
import org.mvplugins.multiverse.inventories.share.Sharable;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
        if (profile.getShares().isEmpty()) {
            return;
        }
        Try.of(() -> profile.getProfile().get(10, TimeUnit.SECONDS))
                .peek(playerProfile -> {
                    for (Sharable<?> sharable : profile.getShares()) {
                        sharable.getHandler().updateProfile(playerProfile, player);
                    }
                    Logging.finer("Persisted: " + profile.getShares() + " to "
                            + playerProfile.getContainerType() + ":" + playerProfile.getContainerName()
                            + " (" + playerProfile.getProfileType() + ")"
                            + " for player " + playerProfile.getPlayerName());
                    inventories.getServiceLocator().getService(ProfileDataSource.class).updatePlayerData(playerProfile);
                })
                .onFailure(e -> Logging.severe("Error getting playerdata: " + e.getMessage()));
    }

    private void updatePlayer() {
        player.closeInventory();
        Try.of(() -> profile.getProfile().get(10, TimeUnit.SECONDS))
                .peek(playerProfile -> {
                    List<Sharable<?>> loaded = new ArrayList<>(profile.getShares().size());
                    List<Sharable<?>> defaulted = new ArrayList<>(profile.getShares().size());

                    for (Sharable<?> sharable : profile.getShares()) {
                        if (sharable.getHandler().updatePlayer(player, playerProfile)) {
                            loaded.add(sharable);
                        } else {
                            defaulted.add(sharable);
                        }
                    }
                    if (!loaded.isEmpty()) {
                        Logging.finer("Updated: " + loaded + " for "
                                + playerProfile.getPlayerName() + " for "
                                + playerProfile.getContainerType() + ":" + playerProfile.getContainerName()
                                + " (" + playerProfile.getProfileType() + ")");
                    }
                    if (!defaulted.isEmpty()) {
                        Logging.finer("Defaulted: " + defaulted + " for "
                                + playerProfile.getPlayerName() + " for "
                                + playerProfile.getContainerType() + ":" + playerProfile.getContainerName()
                                + " (" + playerProfile.getProfileType() + ")");
                    }
                })
                .onFailure(e -> Logging.severe("Error getting playerdata: " + e.getMessage()));;
    }
}
