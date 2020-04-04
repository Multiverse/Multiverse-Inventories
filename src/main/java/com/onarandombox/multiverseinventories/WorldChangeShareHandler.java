package com.onarandombox.multiverseinventories;

import com.dumptruckman.minecraft.util.Logging;
import com.onarandombox.multiverseinventories.profile.container.ProfileContainer;
import com.onarandombox.multiverseinventories.share.Sharables;
import com.onarandombox.multiverseinventories.share.Shares;
import com.onarandombox.multiverseinventories.event.MVInventoryHandlingEvent.Cause;
import com.onarandombox.multiverseinventories.util.Perm;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * WorldChange implementation of ShareHandler.
 */
final class WorldChangeShareHandler extends ShareHandler {

    public WorldChangeShareHandler(MultiverseInventories inventories, Player player,
                                   String fromWorld, String toWorld) {
        super(inventories, player, Cause.WORLD_CHANGE, fromWorld, toWorld,
                player.getGameMode(), player.getGameMode());
    }

    @Override
    public void handle() {
        Logging.finer("=== " + event.getPlayer().getName() + " traveling from world: " + event.getFromWorld()
                + " to " + "world: " + event.getToWorld() + " ===");
        // Grab the player from the world they're coming from to save their stuff to every time.
        ProfileContainer fromWorldProfileContainer = inventories.getWorldProfileContainerStore()
                .getContainer(event.getFromWorld());
        this.addFromProfile(fromWorldProfileContainer, Sharables.allOf(),
                fromWorldProfileContainer.getPlayerData(event.getPlayer()));

        if (Perm.BYPASS_WORLD.hasBypass(event.getPlayer(), event.getToWorld())) {
            this.hasBypass = true;
            return;
        }

        // Get any groups we need to save stuff to.
        List<WorldGroup> fromWorldGroups = this.inventories.getGroupManager()
                .getGroupsForWorld(event.getFromWorld());
        for (WorldGroup fromWorldGroup : fromWorldGroups) {
            ProfileContainer container = fromWorldGroup.getGroupProfileContainer();
            if (!fromWorldGroup.containsWorld(event.getToWorld())) {
                addFromProfile(container, Sharables.fromShares(fromWorldGroup.getShares()),
                        container.getPlayerData(event.getPlayer()));
            } else {
                if (!fromWorldGroup.getShares().isSharing(Sharables.all())) {
                    addFromProfile(container, Sharables.fromShares(fromWorldGroup.getShares()),
                            container.getPlayerData(event.getPlayer()));
                }
            }
        }
        if (fromWorldGroups.isEmpty()) {
            Logging.finer("No groups for fromWorld.");
        }
        Shares sharesToUpdate = Sharables.noneOf();
        //Shares optionalSharesToUpdate = Sharables.noneOptional();
        List<WorldGroup> toWorldGroups = this.inventories.getGroupManager()
                .getGroupsForWorld(event.getToWorld());
        if (!toWorldGroups.isEmpty()) {
            // Get groups we need to load from
            for (WorldGroup toWorldGroup : toWorldGroups) {
                if (Perm.BYPASS_GROUP.hasBypass(event.getPlayer(), toWorldGroup.getName())) {
                    this.hasBypass = true;
                } else {
                    ProfileContainer container = toWorldGroup.getGroupProfileContainer();
                    if (!toWorldGroup.containsWorld(event.getFromWorld())) {
                        Shares sharesToAdd = Sharables.fromShares(toWorldGroup.getShares());
                        addToProfile(container, sharesToAdd, container.getPlayerData(event.getPlayer()));
                        sharesToUpdate.addAll(sharesToAdd);
                    } else {
                        if (!toWorldGroup.getShares().isSharing(Sharables.all())) {
                            Shares sharesToAdd = Sharables.fromShares(toWorldGroup.getShares());
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
                    .getContainer(event.getToWorld());
            addToProfile(toWorldProfileContainer, Sharables.allOf(),
                    toWorldProfileContainer.getPlayerData(event.getPlayer()));
            sharesToUpdate = Sharables.allOf();
        }

        // We need to fill in any sharables that are not going to be transferred with what's saved in the world file.
        if (!sharesToUpdate.isSharing(Sharables.all())) {
            sharesToUpdate = Sharables.complimentOf(sharesToUpdate);

            // Get world we need to load from.
            Logging.finer(sharesToUpdate.toString() + " are left unhandled, defaulting to toWorld");
            ProfileContainer toWorldProfileContainer = this.inventories.getWorldProfileContainerStore()
                    .getContainer(event.getToWorld());
            addToProfile(toWorldProfileContainer, sharesToUpdate,
                    toWorldProfileContainer.getPlayerData(event.getPlayer()));
        }
    }
}

