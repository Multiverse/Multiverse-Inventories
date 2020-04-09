package com.onarandombox.multiverseinventories;

import com.dumptruckman.minecraft.util.Logging;
import com.onarandombox.multiverseinventories.event.ShareHandlingEvent;
import com.onarandombox.multiverseinventories.event.WorldChangeShareHandlingEvent;
import com.onarandombox.multiverseinventories.profile.PlayerProfile;
import com.onarandombox.multiverseinventories.profile.container.ProfileContainer;
import com.onarandombox.multiverseinventories.share.Sharables;
import com.onarandombox.multiverseinventories.share.Shares;
import com.onarandombox.multiverseinventories.util.Perm;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * WorldChange implementation of ShareHandler.
 */
final class WorldChangeShareHandler extends ShareHandler {

    private final String fromWorld;
    private final String toWorld;

    WorldChangeShareHandler(MultiverseInventories inventories, Player player,
                                   String fromWorld, String toWorld) {
        super(inventories, player);
        this.fromWorld = fromWorld;
        this.toWorld = toWorld;
    }

    private PlayerProfile getWorldPlayerProfile(String world, Player player) {
        return getWorldProfile(world).getPlayerData(player);
    }

    private ProfileContainer getWorldProfile(String world) {
        return inventories.getWorldProfileContainerStore().getContainer(world);
    }

    @Override
    protected ShareHandlingEvent createEvent() {
        return new WorldChangeShareHandlingEvent(player, affectedProfiles, fromWorld, toWorld);
    }

    @Override
    protected void prepareProfiles() {
        Logging.finer("=== %s traveling from world: %s to world: %s ===", player.getName(), fromWorld, toWorld);

        // We will always save everything to the world they come from.
        PlayerProfile fromWorldProfile = getWorldPlayerProfile(fromWorld, player);
        setAlwaysWriteProfile(fromWorldProfile);

        if (Perm.BYPASS_WORLD.hasBypass(player, fromWorld)) {
            logBypass();
            return;
        }

        // Get any groups we need to save stuff to.
        List<WorldGroup> fromWorldGroups = this.inventories.getGroupManager()
                .getGroupsForWorld(fromWorld);
        for (WorldGroup fromWorldGroup : fromWorldGroups) {
            ProfileContainer container = fromWorldGroup.getGroupProfileContainer();
            if (!fromWorldGroup.containsWorld(toWorld)) {
                addWriteProfile(container.getPlayerData(player), Sharables.fromShares(fromWorldGroup.getShares()));
            } else {
                if (!fromWorldGroup.getShares().isSharing(Sharables.all())) {
                    addWriteProfile(container.getPlayerData(player), Sharables.fromShares(fromWorldGroup.getShares()));
                }
            }
        }
        if (fromWorldGroups.isEmpty()) {
            Logging.finer("No groups for fromWorld.");
        }
        Shares sharesToUpdate = Sharables.noneOf();
        //Shares optionalSharesToUpdate = Sharables.noneOptional();
        List<WorldGroup> toWorldGroups = this.inventories.getGroupManager()
                .getGroupsForWorld(toWorld);
        if (!toWorldGroups.isEmpty()) {
            // Get groups we need to load from
            for (WorldGroup toWorldGroup : toWorldGroups) {
                if (Perm.BYPASS_GROUP.hasBypass(player, toWorldGroup.getName())) {
                    logBypass();
                } else {
                    ProfileContainer container = toWorldGroup.getGroupProfileContainer();
                    if (!toWorldGroup.containsWorld(fromWorld)) {
                        Shares sharesToAdd = Sharables.fromShares(toWorldGroup.getShares());
                        addReadProfile(container.getPlayerData(player), sharesToAdd);
                        sharesToUpdate.addAll(sharesToAdd);
                    } else {
                        if (!toWorldGroup.getShares().isSharing(Sharables.all())) {
                            Shares sharesToAdd = Sharables.fromShares(toWorldGroup.getShares());
                            addReadProfile(container.getPlayerData(player), sharesToAdd);
                            sharesToUpdate.addAll(sharesToAdd);
                        } else {
                            sharesToUpdate = Sharables.allOf();
                        }
                    }
                }
            }
        } else {
            // Get world we need to load from.
            Logging.finer("No groups for toWorld.");
            ProfileContainer toWorldProfileContainer = this.inventories.getWorldProfileContainerStore()
                    .getContainer(toWorld);
            addReadProfile(toWorldProfileContainer.getPlayerData(player), Sharables.allOf());
            sharesToUpdate = Sharables.allOf();
        }

        // We need to fill in any sharables that are not going to be transferred with what's saved in the world file.
        if (!sharesToUpdate.isSharing(Sharables.all())) {
            sharesToUpdate = Sharables.complimentOf(sharesToUpdate);

            // Get world we need to load from.
            Logging.finer(sharesToUpdate.toString() + " are left unhandled, defaulting to toWorld");
            ProfileContainer toWorldProfileContainer = this.inventories.getWorldProfileContainerStore()
                    .getContainer(toWorld);
            addReadProfile(toWorldProfileContainer.getPlayerData(player), sharesToUpdate);
        }
    }
}
