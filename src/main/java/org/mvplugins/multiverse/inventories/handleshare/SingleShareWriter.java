package org.mvplugins.multiverse.inventories.handleshare;

import com.dumptruckman.minecraft.util.Logging;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.mvplugins.multiverse.inventories.MultiverseInventories;
import org.mvplugins.multiverse.inventories.config.InventoriesConfig;
import org.mvplugins.multiverse.inventories.profile.data.PlayerProfile;
import org.mvplugins.multiverse.inventories.profile.ProfileDataSource;
import org.mvplugins.multiverse.inventories.profile.key.ProfileType;
import org.mvplugins.multiverse.inventories.profile.key.ProfileTypes;
import org.mvplugins.multiverse.inventories.profile.key.ContainerType;
import org.mvplugins.multiverse.inventories.profile.container.ProfileContainerStoreProvider;
import org.mvplugins.multiverse.inventories.profile.group.WorldGroupManager;
import org.mvplugins.multiverse.inventories.share.Sharable;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Write a single share to the relevant world and group profiles.
 *
 * @param <T>   The sharable type.
 */
public final class SingleShareWriter<T> {

    public static <T> SingleShareWriter<T> of(MultiverseInventories inventories, Player player, Sharable<T> sharable) {
        return new SingleShareWriter<>(inventories, player, player.getWorld().getName(), ProfileTypes.forPlayer(player), sharable);
    }

    public static <T> SingleShareWriter<T> of(MultiverseInventories inventories, OfflinePlayer player, String worldName, ProfileType profileType, Sharable<T> sharable) {
        return new SingleShareWriter<>(inventories, player, worldName, profileType, sharable);
    }

    private final MultiverseInventories inventories;
    private final OfflinePlayer player;
    private final String worldName;
    private final ProfileType profileType;
    private final Sharable<T> sharable;
    private final ProfileDataSource profileDataSource;

    private SingleShareWriter(MultiverseInventories inventories, OfflinePlayer player, String worldName, ProfileType profileType, Sharable<T> sharable) {
        this.inventories = inventories;
        this.player = player;
        this.worldName = worldName;
        this.profileType = profileType;
        this.sharable = sharable;
        this.profileDataSource = inventories.getServiceLocator().getService(ProfileDataSource.class);
    }

    public void write(T value) {
        write(value, false);
    }

    public CompletableFuture<Void> write(T value, boolean save) {
        if (sharable.isOptional() &&
                !inventories.getServiceLocator().getService(InventoriesConfig.class).getActiveOptionalShares().contains(sharable)) {
            Logging.finer("Skipping write for optional share: " + sharable);
            return CompletableFuture.completedFuture(null);
        }
        Logging.finer("Writing single share: " + sharable.getNames()[0]);
        var profileContainerStoreProvider = this.inventories.getServiceLocator().getService(ProfileContainerStoreProvider.class);
        profileContainerStoreProvider.getStore(ContainerType.WORLD)
                .getContainer(worldName)
                .getPlayerData(profileType, this.player)
                .thenAccept(profile -> writeNewValueToProfile(profile, value, save));

        return CompletableFuture.allOf(this.inventories.getServiceLocator().getService(WorldGroupManager.class)
                .getGroupsForWorld(worldName)
                .stream()
                .map(worldGroup -> {
                    if (!worldGroup.getApplicableShares().contains(sharable)) {
                        return CompletableFuture.completedFuture(null);
                    }
                    return worldGroup.getGroupProfileContainer().getPlayerData(profileType, this.player)
                            .thenCompose(profile -> writeNewValueToProfile(profile, value, save));
                })
                .toArray(CompletableFuture[]::new));
    }

    private CompletableFuture<Void> writeNewValueToProfile(PlayerProfile profile, T value, boolean save) {
        if (Objects.equals(profile.get(sharable), value)) {
            return CompletableFuture.completedFuture(null);
        }
        Logging.finest("Writing %s value: %s for profile %s", sharable, value, profile);
        profile.set(sharable, value);
        if (save) {
            return profileDataSource.updatePlayerProfile(profile);
        }
        return CompletableFuture.completedFuture(null);
    }
}
