package org.mvplugins.multiverse.inventories.profile.bulkedit;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.mvplugins.multiverse.inventories.MultiverseInventories;
import org.mvplugins.multiverse.inventories.handleshare.ReadOnlyShareHandler;
import org.mvplugins.multiverse.inventories.profile.group.WorldGroup;
import org.mvplugins.multiverse.inventories.profile.group.WorldGroupManager;
import org.mvplugins.multiverse.inventories.profile.key.ContainerType;
import org.mvplugins.multiverse.inventories.profile.key.ProfileFileKey;
import org.mvplugins.multiverse.inventories.profile.key.ProfileType;
import org.mvplugins.multiverse.inventories.profile.key.ProfileTypes;
import org.mvplugins.multiverse.inventories.share.Sharables;
import org.mvplugins.multiverse.inventories.share.Shares;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

final class PlayerProfileClearAction extends PlayerFileAction {

    private final WorldGroupManager worldGroupManager;
    private final Set<ProfileType> profileTypesSet;

    public PlayerProfileClearAction(MultiverseInventories inventories, PlayerProfilesPayload bulkProfilesPayload) {
        super(inventories, bulkProfilesPayload);
        this.worldGroupManager = inventories.getServiceLocator().getService(WorldGroupManager.class);
        this.profileTypesSet = Set.of(bulkProfilesPayload.profileTypes());
    }

    @Override
    protected CompletableFuture<Void> performAction(ProfileFileKey key) {
        return profileDataSource.deletePlayerProfiles(key, bulkProfilesPayload.profileTypes())
                .thenCompose(ignore -> profileDataSource.modifyGlobalProfile(
                        key,
                        profile -> profile.setLoadOnLogin(true)
                ));
    }

    @Override
    protected boolean isOnlinePlayerAffected(ProfileFileKey key, Player player) {
        if (!profileTypesSet.contains(ProfileTypes.forPlayer(player))) {
            return false;
        }

        // Gets groups that share this sharable
        List<WorldGroup> groups = worldGroupManager.getGroupsForWorld(player.getWorld().getName());

        Shares unhandledSharables = Sharables.enabledOf();
        for (WorldGroup worldGroup : groups) {
            unhandledSharables.removeAll(worldGroup.getApplicableShares());
        }

        if (!unhandledSharables.isEmpty()) {
            return key.getContainerType() == ContainerType.WORLD && player.getWorld().getName().equals(key.getDataName());
        }

        // Using group for sharable
        return key.getContainerType() == ContainerType.GROUP && groups.stream()
                .anyMatch(group -> group.getName().equals(key.getDataName()));
    }

    @Override
    protected void updateOnlinePlayerNow(Player player) {
        new ReadOnlyShareHandler(inventories, player).handleSharing();
    }
}
