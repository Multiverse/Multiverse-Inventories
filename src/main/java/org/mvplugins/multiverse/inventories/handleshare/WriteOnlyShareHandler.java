package org.mvplugins.multiverse.inventories.handleshare;

import org.bukkit.entity.Player;
import org.mvplugins.multiverse.inventories.MultiverseInventories;
import org.mvplugins.multiverse.inventories.event.ShareHandlingEvent;
import org.mvplugins.multiverse.inventories.event.WriteOnlyShareHandlingEvent;
import org.mvplugins.multiverse.inventories.profile.key.ProfileType;
import org.mvplugins.multiverse.inventories.profile.key.ProfileTypes;
import org.mvplugins.multiverse.inventories.profile.group.WorldGroup;
import org.mvplugins.multiverse.inventories.share.Sharables;
import org.mvplugins.multiverse.inventories.share.Shares;

import java.util.List;

public final class WriteOnlyShareHandler extends ShareHandler {

    private final String worldName;
    private final ProfileType profileType;

    public WriteOnlyShareHandler(MultiverseInventories inventories, Player player) {
        this(inventories, player, player.getWorld().getName(), ProfileTypes.forPlayer(player));
    }

    public WriteOnlyShareHandler(MultiverseInventories inventories, Player player, String worldName, ProfileType profileType) {
        super(inventories, player);
        this.worldName = worldName;
        this.profileType = profileType;
    }

    @Override
    protected void prepareProfiles() {
        List<WorldGroup>  worldGroups = worldGroupManager.getGroupsForWorld(worldName);

        Shares unhandledShares = Sharables.enabledOf();
        for (WorldGroup worldGroup : worldGroups) {
            affectedProfiles.addWriteProfile(
                    worldGroup.getGroupProfileContainer().getPlayerData(profileType, player),
                    worldGroup.getApplicableShares()
            );
            unhandledShares.removeAll(worldGroup.getApplicableShares());
        }
        Shares sharesToWrite = inventoriesConfig.getAlwaysWriteWorldProfile()
                ? Sharables.enabled()
                : unhandledShares;
        if (!sharesToWrite.isEmpty()) {
            affectedProfiles.addWriteProfile(
                    worldProfileContainerStore.getContainer(worldName).getPlayerData(profileType, player),
                    sharesToWrite
            );
        }
    }

    @Override
    protected ShareHandlingEvent createEvent() {
        return new WriteOnlyShareHandlingEvent(player, affectedProfiles);
    }
}
