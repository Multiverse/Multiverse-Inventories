package com.onarandombox.multiverseinventories;

import com.dumptruckman.minecraft.util.Logging;
import com.onarandombox.multiverseinventories.profile.container.GroupProfileContainer;
import com.onarandombox.multiverseinventories.profile.container.WorldProfileContainer;
import com.onarandombox.multiverseinventories.api.share.Sharables;
import com.onarandombox.multiverseinventories.api.share.Shares;
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
        WorldProfileContainer fromWorldProfileContainer = this.inventories.getWorldManager()
                .getWorldProfileContainer(event.getFromWorld());
        this.addFromProfile(fromWorldProfileContainer, Sharables.allOf(),
                fromWorldProfileContainer.getPlayerData(event.getPlayer()));

        if (Perm.BYPASS_WORLD.hasBypass(event.getPlayer(), event.getToWorld())) {
            this.hasBypass = true;
            return;
        }

        // Get any groups we need to save stuff to.
        List<GroupProfileContainer> fromWorldGroups = this.inventories.getGroupManager()
                .getGroupsForWorld(event.getFromWorld());
        for (GroupProfileContainer fromWorldGroup : fromWorldGroups) {
            if (!fromWorldGroup.containsWorld(event.getToWorld())) {
                this.addFromProfile(fromWorldGroup,
                        Sharables.fromShares(fromWorldGroup.getShares()),
                        fromWorldGroup.getPlayerData(event.getPlayer()));
            } else {
                if (!fromWorldGroup.getShares().isSharing(Sharables.all())) {
                    this.addFromProfile(fromWorldGroup, Sharables.fromShares(fromWorldGroup.getShares()),
                            fromWorldGroup.getPlayerData(event.getPlayer()));
                }
            }
        }
        if (fromWorldGroups.isEmpty()) {
            Logging.finer("No groups for fromWorld.");
        }
        Shares sharesToUpdate = Sharables.noneOf();
        //Shares optionalSharesToUpdate = Sharables.noneOptional();
        List<GroupProfileContainer> toWorldGroups = this.inventories.getGroupManager()
                .getGroupsForWorld(event.getToWorld());
        if (!toWorldGroups.isEmpty()) {
            // Get groups we need to load from
            for (GroupProfileContainer toWorldGroup : toWorldGroups) {
                if (Perm.BYPASS_GROUP.hasBypass(event.getPlayer(), toWorldGroup.getName())) {
                    this.hasBypass = true;
                } else {
                    if (!toWorldGroup.containsWorld(event.getFromWorld())) {
                        Shares sharesToAdd = Sharables.fromShares(toWorldGroup.getShares());
                        this.addToProfile(toWorldGroup,
                                sharesToAdd, toWorldGroup.getPlayerData(event.getPlayer()));
                        sharesToUpdate.addAll(sharesToAdd);
                    } else {
                        if (!toWorldGroup.getShares().isSharing(Sharables.all())) {
                            Shares sharesToAdd = Sharables.fromShares(toWorldGroup.getShares());
                            this.addToProfile(toWorldGroup, sharesToAdd, toWorldGroup.getPlayerData(event.getPlayer()));
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
            WorldProfileContainer toWorldProfileContainer = this.inventories.getWorldManager()
                    .getWorldProfileContainer(event.getToWorld());
            this.addToProfile(toWorldProfileContainer, Sharables.allOf(),
                    toWorldProfileContainer.getPlayerData(event.getPlayer()));
            sharesToUpdate = Sharables.allOf();
        }

        // We need to fill in any sharables that are not going to be transferred with what's saved in the world file.
        if (!sharesToUpdate.isSharing(Sharables.all())) {
            sharesToUpdate = Sharables.complimentOf(sharesToUpdate);

            // Get world we need to load from.
            Logging.finer(sharesToUpdate.toString() + " are left unhandled, defaulting to toWorld");
            WorldProfileContainer toWorldProfileContainer = this.inventories.getWorldManager()
                    .getWorldProfileContainer(event.getToWorld());
            this.addToProfile(toWorldProfileContainer, sharesToUpdate,
                    toWorldProfileContainer.getPlayerData(event.getPlayer()));
        }
    }
}

