package org.mvplugins.multiverse.inventories.handleshare;

import com.dumptruckman.minecraft.util.Logging;
import org.mvplugins.multiverse.inventories.MultiverseInventories;
import org.mvplugins.multiverse.inventories.profile.group.WorldGroup;
import org.mvplugins.multiverse.inventories.event.ShareHandlingEvent;
import org.mvplugins.multiverse.inventories.event.WorldChangeShareHandlingEvent;
import org.mvplugins.multiverse.inventories.profile.container.ProfileContainer;
import org.mvplugins.multiverse.inventories.share.Sharables;
import org.mvplugins.multiverse.inventories.share.Shares;
import org.mvplugins.multiverse.inventories.util.Perm;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * WorldChange implementation of ShareHandler.
 */
public final class WorldChangeShareHandler extends ShareHandler {

    private final String fromWorld;
    private final String toWorld;
    private final List<WorldGroup> fromWorldGroups;
    private final List<WorldGroup> toWorldGroups;

    public WorldChangeShareHandler(MultiverseInventories inventories, Player player, String fromWorld, String toWorld) {
        super(inventories, player);
        this.fromWorld = fromWorld;
        this.toWorld = toWorld;

        // Get any groups we may need to save stuff to.
        this.fromWorldGroups = worldGroupManager.getGroupsForWorld(fromWorld);
        // Get any groups we may need to load stuff from.
        this.toWorldGroups = worldGroupManager.getGroupsForWorld(toWorld);
    }

    @Override
    protected ShareHandlingEvent createEvent() {
        return new WorldChangeShareHandlingEvent(player, affectedProfiles, fromWorld, toWorld);
    }

    @Override
    protected void prepareProfiles() {
        Logging.fine("=== %s traveling from world: %s to world: %s ===", player.getName(), fromWorld, toWorld);
        if (isPlayerAffectedByChange()) {
            addWriteProfiles();
            addReadProfiles();
        } else if (inventoriesConfig.getAlwaysWriteWorldProfile()) {
            // Write to world profile to ensure data is saved incase bypass is removed
            affectedProfiles.addWriteProfile(
                    worldProfileContainerStore.getContainer(fromWorld).getProfileKey(player),
                    (fromWorldGroups.isEmpty() && !inventoriesConfig.getUseOptionalsForUngroupedWorlds())
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
        return Perm.BYPASS_WORLD.hasBypass(player, fromWorld);
    }

    private void addWriteProfiles() {
        new WriteProfileAggregator().conditionallyAddWriteProfiles();
    }

    private void addReadProfiles() {
        new ReadProfilesAggregator().addReadProfiles();
    }

    private class ReadProfilesAggregator {

        private final Shares handledShares = Sharables.noneOf();

        private void addReadProfiles() {
            addReadProfilesFromToWorldGroups();
            useToWorldForMissingShares();
        }

        private void addReadProfilesFromToWorldGroups() {
            if (toWorldGroups.isEmpty()) {
                Logging.finer("No groups for toWorld.");
                return;
            }
            toWorldGroups.forEach(this::conditionallyAddReadProfileForWorldGroup);
        }

        private void conditionallyAddReadProfileForWorldGroup(WorldGroup worldGroup) {
            if (!isPlayerAffectedByChange(worldGroup)) {
                return;
            }
            if (isFromWorldNotInToWorldGroup(worldGroup)) {
                addReadProfileForWorldGroup(worldGroup);
            }
            handledShares.addAll(worldGroup.getApplicableShares());
            handledShares.addAll(worldGroup.getDisabledShares());
        }

        private boolean isPlayerAffectedByChange(WorldGroup worldGroup) {
            if (isPlayerBypassingChange(worldGroup)) {
                logBypass();
                return false;
            }
            return true;
        }

        private boolean isPlayerBypassingChange(WorldGroup worldGroup) {
            return Perm.BYPASS_GROUP.hasBypass(player, worldGroup.getName());
        }

        private boolean isFromWorldNotInToWorldGroup(WorldGroup worldGroup) {
            if (inventoriesConfig.getDefaultUngroupedWorlds()
                    && !worldGroupManager.hasConfiguredGroup(fromWorld)
                    && worldGroup.equals(worldGroupManager.getDefaultGroup())) {
                return false;
            }
            return !worldGroup.containsWorld(fromWorld);
        }

        private void addReadProfileForWorldGroup(WorldGroup worldGroup) {
            Shares applicableShares = Sharables.fromShares(worldGroup.getApplicableShares());
            if (!inventoriesConfig.getApplyLastLocationForAllTeleports()) {
                Logging.finer("Removing lastLocation from applicableShares as it is not applied for all teleports");
                applicableShares.remove(Sharables.LAST_LOCATION);
            }
            affectedProfiles.addReadProfile(worldGroup.getGroupProfileContainer().getProfileKey(player), applicableShares);
        }

        private void useToWorldForMissingShares() {
            // We need to fill in any sharables that are not going to be transferred with what's saved in the world file.
            Shares unhandledShares = (toWorldGroups.isEmpty() && !inventoriesConfig.getUseOptionalsForUngroupedWorlds())
                    ? Sharables.standardOf() : Sharables.enabledOf();
            unhandledShares.removeAll(handledShares);
            if (!inventoriesConfig.getApplyLastLocationForAllTeleports()) {
                Logging.finer("Removing lastLocation from unhandledShares as it is not applied for all teleports");
                unhandledShares.remove(Sharables.LAST_LOCATION);
            }
            if (unhandledShares.isEmpty()) {
                return;
            }
            Logging.finer("%s are left unhandled, defaulting to toWorld", unhandledShares);
            affectedProfiles.addReadProfile(
                    worldProfileContainerStore.getContainer(toWorld).getProfileKey(player),
                    unhandledShares
            );
        }
    }

    private class WriteProfileAggregator {

        private final Shares handledShares = Sharables.noneOf();

        private void conditionallyAddWriteProfiles() {
            fromWorldGroups.forEach(this::conditionallyAddWriteProfileForGroup);
            Shares sharesToWrite = inventoriesConfig.getAlwaysWriteWorldProfile()
                    ? Sharables.enabled()
                    : Sharables.enabledOf().setSharing(handledShares, false);
            if (!sharesToWrite.isEmpty()) {
                affectedProfiles.addWriteProfile(
                        worldProfileContainerStore.getContainer(fromWorld).getProfileKey(player),
                        sharesToWrite);
            }
        }

        private void conditionallyAddWriteProfileForGroup(WorldGroup worldGroup) {
            if (!worldGroup.containsWorld(toWorld)) {
                addWriteProfileForGroup(worldGroup);
            }
            handledShares.addAll(worldGroup.getApplicableShares());
            handledShares.addAll(worldGroup.getDisabledShares());
        }

        void addWriteProfileForGroup(WorldGroup worldGroup) {
            ProfileContainer container = worldGroup.getGroupProfileContainer();
            affectedProfiles.addWriteProfile(container.getProfileKey(player), worldGroup.getApplicableShares());
        }
    }
}
