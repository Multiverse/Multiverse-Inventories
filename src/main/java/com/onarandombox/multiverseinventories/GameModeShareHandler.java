package com.onarandombox.multiverseinventories;

import com.onarandombox.multiverseinventories.api.Inventories;
import com.onarandombox.multiverseinventories.api.profile.ProfileType;
import com.onarandombox.multiverseinventories.api.profile.WorldGroupProfile;
import com.onarandombox.multiverseinventories.api.profile.WorldProfile;
import com.onarandombox.multiverseinventories.api.share.Sharables;
import com.onarandombox.multiverseinventories.event.MVInventoryHandlingEvent.Cause;
import com.onarandombox.multiverseinventories.util.Logging;
import com.onarandombox.multiverseinventories.util.Perm;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * GameMode change implementation of ShareHandler.
 */
final class GameModeShareHandler extends ShareHandler {

    public GameModeShareHandler(Inventories inventories, Player player,
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
        WorldProfile worldProfile = this.inventories.getWorldManager().getWorldProfile(world);
        this.addFromProfile(worldProfile, Sharables.allOf(), worldProfile.getPlayerData(fromType, player));

        if (Perm.BYPASS_WORLD.hasBypass(player, world)) {
            this.hasBypass = true;
            return;
        }

        List<WorldGroupProfile> worldGroups = this.inventories.getGroupManager().getGroupsForWorld(world);
        for (WorldGroupProfile worldGroup : worldGroups) {
            this.addFromProfile(worldGroup, Sharables.allOf(), worldGroup.getPlayerData(fromType, player));
            this.addToProfile(worldGroup, Sharables.allOf(), worldGroup.getPlayerData(toType, player));
        }
        if (worldGroups.isEmpty()) {
            Logging.finer("No groups for world.");
            this.addToProfile(worldProfile, Sharables.allOf(), worldProfile.getPlayerData(toType, player));
        }
    }
}

