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
import org.mvplugins.multiverse.inventories.share.Shares;
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
        Logging.fine("=== " + player.getName() + " changing game mode from: " + fromType
                + " to: " + toType + " for world: " + world + " ===");

        if (isPlayerAffectedByChange()) {
            addProfiles();
        } else if (inventoriesConfig.getAlwaysWriteWorldProfile()) {
            // Write to world profile to ensure data is saved incase bypass is removed
            affectedProfiles.addWriteProfile(
                    worldProfileContainerStore.getContainer(world).getPlayerData(player),
                    (worldGroups.isEmpty() && !inventoriesConfig.getUseOptionalsForUngroupedWorlds())
                            ? Sharables.standard()
                            : Sharables.enabled()
            );
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
        Shares handledShares = Sharables.noneOf();
        worldGroups.forEach(worldGroup -> addProfilesForWorldGroup(handledShares,worldGroup));
        Shares unhandledShares = Sharables.enabledOf().setSharing(handledShares, false);
        if (!unhandledShares.isEmpty()) {
            affectedProfiles.addReadProfile(worldProfileContainerStore.getContainer(world).getPlayerData(fromType, player), unhandledShares);
        }

        if (inventoriesConfig.getAlwaysWriteWorldProfile()) {
            affectedProfiles.addWriteProfile(worldProfileContainerStore.getContainer(world).getPlayerData(toType, player),
                    inventoriesConfig.getUseOptionalsForUngroupedWorlds() ? Sharables.enabled() : Sharables.standard());
        } else {
            if (!unhandledShares.isEmpty()) {
                affectedProfiles.addWriteProfile(worldProfileContainerStore.getContainer(world).getPlayerData(toType, player), unhandledShares);
            }
        }
    }

    private void addProfilesForWorldGroup(Shares handledShares, WorldGroup worldGroup) {
        ProfileContainer container = worldGroup.getGroupProfileContainer();
        affectedProfiles.addWriteProfile(container.getPlayerData(fromType, player), worldGroup.getApplicableShares());
        affectedProfiles.addReadProfile(container.getPlayerData(toType, player), worldGroup.getApplicableShares());
        handledShares.addAll(worldGroup.getApplicableShares());
        handledShares.addAll(worldGroup.getDisabledShares());
    }
}
