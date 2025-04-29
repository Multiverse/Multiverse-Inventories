package org.mvplugins.multiverse.inventories.profile.bulkedit;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.mvplugins.multiverse.inventories.MultiverseInventories;
import org.mvplugins.multiverse.inventories.handleshare.SingleShareReader;
import org.mvplugins.multiverse.inventories.profile.data.ProfileData;
import org.mvplugins.multiverse.inventories.profile.data.SingleSharableData;
import org.mvplugins.multiverse.inventories.profile.group.WorldGroup;
import org.mvplugins.multiverse.inventories.profile.group.WorldGroupManager;
import org.mvplugins.multiverse.inventories.profile.key.ContainerType;
import org.mvplugins.multiverse.inventories.profile.key.ProfileKey;
import org.mvplugins.multiverse.inventories.profile.key.ProfileTypes;
import org.mvplugins.multiverse.inventories.share.Sharable;
import org.mvplugins.multiverse.inventories.util.FutureNow;

import java.util.List;
import java.util.concurrent.CompletableFuture;

final class PlayerProfileDeleteAction extends PlayerProfileAction {


    private final WorldGroupManager worldGroupManager;
    private final Sharable sharable;

    public PlayerProfileDeleteAction(
            MultiverseInventories inventories,
            Sharable sharable,
            PlayerProfilesPayload bulkProfilesPayload
    ) {
        super(inventories, bulkProfilesPayload);
        this.worldGroupManager = inventories.getServiceLocator().getService(WorldGroupManager.class);
        this.sharable = sharable;
    }

    @Override
    protected CompletableFuture<Void> performAction(ProfileKey key) {
        return profileDataSource.getPlayerProfile(key)
                .thenCompose(playerProfile -> {
                    playerProfile.set(sharable, null);
                    return profileDataSource.updatePlayerProfile(playerProfile);
                })
                .thenCompose(ignore -> profileDataSource.modifyGlobalProfile(key, profile -> {
                    profile.setLoadOnLogin(true);
                }));
    }

    @Override
    protected boolean isOnlinePlayerAffected(ProfileKey key, Player player) {
        if (!ProfileTypes.forPlayer(player).equals(key.getProfileType())) {
            return false;
        }

        // Gets groups that share this sharable
        List<WorldGroup> groups = worldGroupManager.getGroupsForWorld(player.getWorld().getName()).stream()
                .filter(group -> group.isSharing(sharable))
                .toList();

        if (groups.isEmpty()) {
            // Using world itself for sharable
            return key.getContainerType() == ContainerType.WORLD && player.getWorld().getName().equals(key.getDataName());
        }

        // Using group for sharable
        return key.getContainerType() == ContainerType.GROUP && groups.stream()
                .anyMatch(group -> group.getName().equals(key.getDataName()));
    }

    @Override
    protected void updateOnlinePlayerNow(Player player) {
        ProfileData sharableData = new SingleSharableData<>(sharable, FutureNow.get(SingleShareReader.of(inventories, player, sharable).read()));
        sharable.getHandler().updatePlayer(player, sharableData);
    }
}
