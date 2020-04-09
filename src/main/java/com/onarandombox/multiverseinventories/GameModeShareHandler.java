package com.onarandombox.multiverseinventories;

import com.dumptruckman.minecraft.util.Logging;
import com.onarandombox.multiverseinventories.event.GameModeChangeShareHandlingEvent;
import com.onarandombox.multiverseinventories.event.ShareHandlingEvent;
import com.onarandombox.multiverseinventories.profile.ProfileType;
import com.onarandombox.multiverseinventories.profile.ProfileTypes;
import com.onarandombox.multiverseinventories.profile.container.ProfileContainer;
import com.onarandombox.multiverseinventories.share.Sharables;
import com.onarandombox.multiverseinventories.util.Perm;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * GameMode change implementation of ShareHandler.
 */
final class GameModeShareHandler extends ShareHandler {

    private final GameMode fromGameMode;
    private final GameMode toGameMode;

    GameModeShareHandler(MultiverseInventories inventories, Player player,
                                GameMode fromGameMode, GameMode toGameMode) {
        super(inventories, player);
        this.fromGameMode = fromGameMode;
        this.toGameMode = toGameMode;
    }

    @Override
    protected ShareHandlingEvent createEvent() {
        return new GameModeChangeShareHandlingEvent(player, affectedProfiles, fromGameMode, toGameMode);
    }

    @Override
    protected void prepareProfiles() {
        ProfileType fromType = ProfileTypes.forGameMode(fromGameMode);
        ProfileType toType = ProfileTypes.forGameMode(toGameMode);
        String world = player.getWorld().getName();
        Logging.finer("=== " + player.getName() + " changing game mode from: " + fromType
                + " to: " + toType + " for world: " + world + " ===");

        // Grab the player from the world they're coming from to save their stuff to every time.
        ProfileContainer worldProfileContainer = this.inventories.getWorldProfileContainerStore().getContainer(world);
        setAlwaysWriteProfile(worldProfileContainer.getPlayerData(fromType, player));

        if (Perm.BYPASS_WORLD.hasBypass(player, world)) {
            logBypass();
            return;
        } else if (Perm.BYPASS_GAME_MODE.hasBypass(player, toGameMode.name().toLowerCase())) {
            logBypass();
            return;
        }

        List<WorldGroup> worldGroups = this.inventories.getGroupManager().getGroupsForWorld(world);
        for (WorldGroup worldGroup : worldGroups) {
            ProfileContainer container = worldGroup.getGroupProfileContainer();
            addWriteProfile(container.getPlayerData(fromType, player), Sharables.allOf());
            addReadProfile(container.getPlayerData(toType, player), Sharables.allOf());
        }
        if (worldGroups.isEmpty()) {
            Logging.finer("No groups for world.");
            addReadProfile(worldProfileContainer.getPlayerData(toType, player), Sharables.allOf());
        }
    }
}

