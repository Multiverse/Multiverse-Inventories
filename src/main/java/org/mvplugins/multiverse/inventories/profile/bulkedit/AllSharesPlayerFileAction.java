package org.mvplugins.multiverse.inventories.profile.bulkedit;

import com.dumptruckman.minecraft.util.Logging;
import org.bukkit.entity.Player;
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

abstract sealed class AllSharesPlayerFileAction extends PlayerFileAction
        permits PlayerProfileClearAction, PlayerProfileCloneWorldGroupAction, PlayerProfileClonePlayerAction {

    private final WorldGroupManager worldGroupManager;
    private final Set<ProfileType> profileTypesSet;

    AllSharesPlayerFileAction(MultiverseInventories inventories, PlayerProfilesPayload bulkProfilesPayload) {
        super(inventories, bulkProfilesPayload);
        this.worldGroupManager = inventories.getServiceLocator().getService(WorldGroupManager.class);
        this.profileTypesSet = Set.of(bulkProfilesPayload.profileTypes());
    }

    @Override
    protected boolean isOnlinePlayerAffected(ProfileFileKey key, Player player) {
        if (!profileTypesSet.contains(ProfileTypes.forPlayer(player))) {
            return false;
        }

        // Gets groups that share this sharable
        String playerWorldName = player.getWorld().getName();
        List<WorldGroup> groups = worldGroupManager.getGroupsForWorld(playerWorldName);

        Shares unhandledSharables = Sharables.enabledOf();
        for (WorldGroup worldGroup : groups) {
            unhandledSharables.removeAll(worldGroup.getApplicableShares());
        }

        if (!unhandledSharables.isEmpty()) {
            return key.getContainerType() == ContainerType.WORLD && playerWorldName.equals(key.getDataName());
        }

        // Using group for sharable
        return key.getContainerType() == ContainerType.GROUP && groups.stream()
                .anyMatch(group -> group.getName().equals(key.getDataName()));
    }

    @Override
    protected void updateOnlinePlayerNow(Player player) {
        Logging.finer("Updating online player after bulkedit: " + player.getName());
        new ReadOnlyShareHandler(inventories, player).handleSharing();
    }
}
