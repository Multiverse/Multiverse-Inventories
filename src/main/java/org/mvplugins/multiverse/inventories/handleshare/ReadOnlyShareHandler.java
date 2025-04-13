package org.mvplugins.multiverse.inventories.handleshare;

import org.bukkit.entity.Player;
import org.mvplugins.multiverse.inventories.MultiverseInventories;
import org.mvplugins.multiverse.inventories.event.ReadOnlyShareHandlingEvent;
import org.mvplugins.multiverse.inventories.event.ShareHandlingEvent;
import org.mvplugins.multiverse.inventories.profile.group.WorldGroup;
import org.mvplugins.multiverse.inventories.share.Sharables;
import org.mvplugins.multiverse.inventories.share.Shares;

import java.util.List;

final class ReadOnlyShareHandler extends ShareHandler {
    ReadOnlyShareHandler(MultiverseInventories inventories, Player player) {
        super(inventories, player);
    }

    @Override
    protected void prepareProfiles() {
        List<WorldGroup> worldGroups = worldGroupManager.getGroupsForWorld(player.getWorld().getName());
        Shares unhandledShares = Sharables.enabledOf();
        for (WorldGroup worldGroup : worldGroups) {
            affectedProfiles.addReadProfile(worldGroup.getGroupProfileContainer().getProfileKey(player), worldGroup.getApplicableShares());
            unhandledShares.removeAll(worldGroup.getApplicableShares());
        }
        if (!unhandledShares.isEmpty()) {
            affectedProfiles.addReadProfile(
                    worldProfileContainerStore.getContainer(player.getWorld().getName()).getProfileKey(player),
                    unhandledShares
            );
        }
    }

    @Override
    protected ShareHandlingEvent createEvent() {
        return new ReadOnlyShareHandlingEvent(player, affectedProfiles);
    }
}
