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
    private final ProfileType fromType;
    private final ProfileType toType;
    private final String world;
    private final ProfileContainer worldProfileContainer;
    private final List<WorldGroup> worldGroups;

    GameModeShareHandler(MultiverseInventories inventories, Player player,
                                GameMode fromGameMode, GameMode toGameMode) {
        super(inventories, player);
        this.fromGameMode = fromGameMode;
        this.toGameMode = toGameMode;
        this.fromType = ProfileTypes.forGameMode(fromGameMode);
        this.toType = ProfileTypes.forGameMode(toGameMode);
        this.world = player.getWorld().getName();
        this.worldProfileContainer = inventories.getWorldProfileContainerStore().getContainer(world);
        this.worldGroups = getAffectedWorldGroups();
    }

    private List<WorldGroup> getAffectedWorldGroups() {
        return this.inventories.getGroupManager().getGroupsForWorld(world);
    }

    @Override
    protected ShareHandlingEvent createEvent() {
        return new GameModeChangeShareHandlingEvent(player, affectedProfiles, fromGameMode, toGameMode);
    }

    @Override
    protected void prepareProfiles() {
        Logging.finer("=== " + player.getName() + " changing game mode from: " + fromType
                + " to: " + toType + " for world: " + world + " ===");

        setAlwaysWriteProfile(worldProfileContainer.getPlayerData(fromType, player));

        if (isPlayerAffectedByChange()) {
            addProfiles();
        }
    }

    private boolean isPlayerAffectedByChange() {
        if (isPlayerBypassingChange()) {
            logBypass();
            return false;
        }
        return true;
    }

    private boolean isPlayerBypassingChange() {
        return Perm.BYPASS_WORLD.hasBypass(player, world)
                || Perm.BYPASS_GAME_MODE.hasBypass(player, toGameMode.name().toLowerCase());
    }

    private void addProfiles() {
        if (hasWorldGroups()) {
            worldGroups.forEach(this::addProfilesForWorldGroup);
        } else {
            Logging.finer("No groups for world.");
            addReadProfile(worldProfileContainer.getPlayerData(toType, player), Sharables.allOf());
        }
    }

    private boolean hasWorldGroups() {
        return !worldGroups.isEmpty();
    }

    private void addProfilesForWorldGroup(WorldGroup worldGroup) {
        ProfileContainer container = worldGroup.getGroupProfileContainer();
        addWriteProfile(container.getPlayerData(fromType, player), Sharables.allOf());
        addReadProfile(container.getPlayerData(toType, player), Sharables.allOf());
    }
}

