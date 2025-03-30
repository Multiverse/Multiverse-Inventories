package org.mvplugins.multiverse.inventories.handleshare;

import org.bukkit.OfflinePlayer;
import org.mvplugins.multiverse.inventories.MultiverseInventories;
import org.mvplugins.multiverse.inventories.profile.ProfileType;
import org.mvplugins.multiverse.inventories.profile.container.ContainerType;
import org.mvplugins.multiverse.inventories.profile.container.ProfileContainerStoreProvider;
import org.mvplugins.multiverse.inventories.profile.group.WorldGroup;
import org.mvplugins.multiverse.inventories.profile.group.WorldGroupManager;
import org.mvplugins.multiverse.inventories.share.Sharable;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class SingleShareReader<T> {

    public static <T> SingleShareReader<T> of(MultiverseInventories inventories, OfflinePlayer player, String worldName, ProfileType profileType, Sharable<T> sharable) {
        return new SingleShareReader<>(inventories, player, worldName, profileType, sharable);
    }

    private final MultiverseInventories inventories;
    private final OfflinePlayer player;
    private final String worldName;
    private final ProfileType profileType;
    private final Sharable<T> sharable;

    public SingleShareReader(MultiverseInventories inventories, OfflinePlayer player, String worldName, ProfileType profileType, Sharable<T> sharable) {
        this.inventories = inventories;
        this.player = player;
        this.worldName = worldName;
        this.profileType = profileType;
        this.sharable = sharable;
    }

    public CompletableFuture<T> read() {
        WorldGroupManager worldGroupManager = inventories.getServiceLocator().getService(WorldGroupManager.class);
        List<WorldGroup> worldGroups = worldGroupManager.getGroupsForWorld(worldName);
        for (WorldGroup worldGroup : worldGroups) {
            if (worldGroup.isSharing(sharable)) {
                return getSharableFromProfile(ContainerType.GROUP, worldGroup.getName());
            }
        }
        return getSharableFromProfile(ContainerType.WORLD, worldName);
    }

    private CompletableFuture<T> getSharableFromProfile(ContainerType containerType, String containerName) {
        return this.inventories.getServiceLocator().getService(ProfileContainerStoreProvider.class)
                .getStore(containerType)
                .getContainer(containerName)
                .getPlayerData(profileType, player)
                .thenApply(playerProfile -> playerProfile.get(sharable));
    }
}
