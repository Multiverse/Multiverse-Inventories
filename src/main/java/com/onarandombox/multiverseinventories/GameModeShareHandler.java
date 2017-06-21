package com.onarandombox.multiverseinventories;

import com.dumptruckman.minecraft.util.Logging;
import com.onarandombox.multiverseinventories.profile.ProfileType;
import com.onarandombox.multiverseinventories.profile.ProfileTypes;
import com.onarandombox.multiverseinventories.profile.container.GroupProfileContainer;
import com.onarandombox.multiverseinventories.profile.container.WorldProfileContainer;
import com.onarandombox.multiverseinventories.api.share.Sharables;
import com.onarandombox.multiverseinventories.event.MVInventoryHandlingEvent.Cause;
import com.onarandombox.multiverseinventories.util.Perm;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * GameMode change implementation of ShareHandler.
 */
final class GameModeShareHandler extends ShareHandler {

    public GameModeShareHandler(MultiverseInventories inventories, Player player,
                                GameMode fromGameMode, GameMode toGameMode) {
        super(inventories, player, Cause.GAME_MODE_CHANGE, player.getWorld().getName(),
                player.getWorld().getName(), fromGameMode, toGameMode);
    }

    @Override
    public void handle() {
        Player player = event.getPlayer();
        ProfileType fromType = ProfileTypes.forGameMode(event.getFromGameMode());
        ProfileType toType = ProfileTypes.forGameMode(event.getToGameMode());
        String world = event.getPlayer().getWorld().getName();
        Logging.finer("=== " + player.getName() + " changing game mode from: " + fromType
                + " to: " + toType + " for world: " + world + " ===");
        // Grab the player from the world they're coming from to save their stuff to every time.
        WorldProfileContainer worldProfileContainer = this.inventories.getWorldManager().getWorldProfileContainer(world);
        this.addFromProfile(worldProfileContainer, Sharables.allOf(), worldProfileContainer.getPlayerData(fromType, player));

        if (Perm.BYPASS_WORLD.hasBypass(player, world)) {
            this.hasBypass = true;
            return;
        }

        List<GroupProfileContainer> worldGroups = this.inventories.getGroupManager().getGroupsForWorld(world);
        for (GroupProfileContainer worldGroup : worldGroups) {
            this.addFromProfile(worldGroup, Sharables.allOf(), worldGroup.getPlayerData(fromType, player));
            this.addToProfile(worldGroup, Sharables.allOf(), worldGroup.getPlayerData(toType, player));
        }
        if (worldGroups.isEmpty()) {
            Logging.finer("No groups for world.");
            this.addToProfile(worldProfileContainer, Sharables.allOf(), worldProfileContainer.getPlayerData(toType, player));
        }
    }
}

