package org.mvplugins.multiverse.inventories.handleshare;

import com.dumptruckman.minecraft.util.Logging;
import org.mvplugins.multiverse.inventories.MultiverseInventories;
import org.mvplugins.multiverse.inventories.profile.group.WorldGroup;
import org.mvplugins.multiverse.inventories.event.GameModeChangeShareHandlingEvent;
import org.mvplugins.multiverse.inventories.event.ShareHandlingEvent;
import org.mvplugins.multiverse.inventories.profile.ProfileType;
import org.mvplugins.multiverse.inventories.profile.ProfileTypes;
import org.mvplugins.multiverse.inventories.profile.container.ProfileContainer;
import org.mvplugins.multiverse.inventories.share.Sharables;
import org.mvplugins.multiverse.inventories.util.Perm;
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
    private final List<WorldGroup> worldGroups;

    GameModeShareHandler(MultiverseInventories inventories, Player player,
                         GameMode fromGameMode, GameMode toGameMode) {
        super(inventories, player);
        this.fromGameMode = fromGameMode;
        this.toGameMode = toGameMode;
        this.fromType = ProfileTypes.forGameMode(fromGameMode);
        this.toType = ProfileTypes.forGameMode(toGameMode);
        this.world = player.getWorld().getName();
        this.worldGroups = getAffectedWorldGroups();
    }

    private List<WorldGroup> getAffectedWorldGroups() {
        return worldGroupManager.getGroupsForWorld(world);
    }

    @Override
    protected ShareHandlingEvent createEvent() {
        return new GameModeChangeShareHandlingEvent(player, affectedProfiles, fromGameMode, toGameMode);
    }

    @Override
    protected void prepareProfiles() {
        Logging.finer("=== " + player.getName() + " changing game mode from: " + fromType
                + " to: " + toType + " for world: " + world + " ===");

        affectedProfiles.setAlwaysWriteProfile(worldProfileContainerStore.getContainer(world).getPlayerData(fromType, player));

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
            affectedProfiles.addReadProfile(worldProfileContainerStore.getContainer(world).getPlayerData(toType, player),
                    inventoriesConfig.getUseOptionalsForUngroupedWorlds() ? Sharables.enabled() : Sharables.standardOf());
        }
    }

    private boolean hasWorldGroups() {
        return !worldGroups.isEmpty();
    }

    private void addProfilesForWorldGroup(WorldGroup worldGroup) {
        ProfileContainer container = worldGroup.getGroupProfileContainer();
        affectedProfiles.addWriteProfile(container.getPlayerData(fromType, player), Sharables.enabled());
        affectedProfiles.addReadProfile(container.getPlayerData(toType, player), Sharables.enabled());
    }
}
