package org.mvplugins.multiverse.inventories.handleshare;

import com.dumptruckman.minecraft.util.Logging;
import org.mvplugins.multiverse.inventories.MultiverseInventories;
import org.mvplugins.multiverse.inventories.profile.group.WorldGroup;
import org.mvplugins.multiverse.inventories.event.ShareHandlingEvent;
import org.mvplugins.multiverse.inventories.event.WorldChangeShareHandlingEvent;
import org.mvplugins.multiverse.inventories.profile.PlayerProfile;
import org.mvplugins.multiverse.inventories.profile.container.ProfileContainer;
import org.mvplugins.multiverse.inventories.share.Sharables;
import org.mvplugins.multiverse.inventories.share.Shares;
import org.mvplugins.multiverse.inventories.util.Perm;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * WorldChange implementation of ShareHandler.
 */
final class WorldChangeShareHandler extends ShareHandler {

    private final String fromWorld;
    private final String toWorld;
    private final List<WorldGroup> fromWorldGroups;
    private final List<WorldGroup> toWorldGroups;

    WorldChangeShareHandler(MultiverseInventories inventories, Player player, String fromWorld, String toWorld) {
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
        Logging.finer("=== %s traveling from world: %s to world: %s ===", player.getName(), fromWorld, toWorld);
        setAlwaysWriteWorldProfile();
        if (isPlayerAffectedByChange()) {
            addWriteProfiles();
            addReadProfiles();
        }
    }

    private void setAlwaysWriteWorldProfile() {
        // We will always save everything to the world they come from.
        PlayerProfile fromWorldProfile = getWorldPlayerProfile(fromWorld, player);
        affectedProfiles.setAlwaysWriteProfile(fromWorldProfile);
    }

    private PlayerProfile getWorldPlayerProfile(String world, Player player) {
        return worldProfileContainerStore.getContainer(world).getPlayerData(player);
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
        if (fromWorldGroups.isEmpty()) {
            Logging.finer("No groups for fromWorld.");
            return;
        }
        fromWorldGroups.forEach(wg -> new WorldGroupWrapper(wg).conditionallyAddWriteProfiles());
    }

    private void addReadProfiles() {
        new ReadProfilesAggregator().addReadProfiles();
    }

    private class ReadProfilesAggregator {

        private final Shares handledShares;

        private ReadProfilesAggregator() {
            this.handledShares = Sharables.noneOf();
        }

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
            handledShares.addAll(worldGroup.getShares());
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
            PlayerProfile playerProfile = getWorldGroupPlayerData(worldGroup);
            affectedProfiles.addReadProfile(playerProfile, worldGroup.getShares());
        }

        private PlayerProfile getWorldGroupPlayerData(WorldGroup worldGroup) {
            return worldGroup.getGroupProfileContainer().getPlayerData(player);
        }

        private void useToWorldForMissingShares() {
            // We need to fill in any sharables that are not going to be transferred with what's saved in the world file.
            Shares unhandledShares = Sharables.complimentOf(handledShares);
            if (unhandledShares.isEmpty()) {
                return;
            }
            Logging.finer("%s are left unhandled, defaulting to toWorld", unhandledShares);
            affectedProfiles.addReadProfile(getToWorldPlayerData(), unhandledShares);
        }

        private PlayerProfile getToWorldPlayerData() {
            return worldProfileContainerStore.getContainer(toWorld).getPlayerData(player);
        }
    }

    private class WorldGroupWrapper {
        private final WorldGroup worldGroup;

        public WorldGroupWrapper(WorldGroup worldGroup) {
            this.worldGroup = worldGroup;
        }

        private void conditionallyAddWriteProfiles() {
            if (isEligibleForWrite()) {
                addWriteProfiles();
            }
        }

        boolean isEligibleForWrite() {
            return groupDoesNotContainWorld(toWorld) || isNotSharingAll();
        }

        private boolean groupDoesNotContainWorld(String world) {
            return !worldGroup.containsWorld(world);
        }

        private boolean isNotSharingAll() {
            return !worldGroup.getShares().isSharing(Sharables.all());
        }

        void addWriteProfiles() {
            ProfileContainer container = worldGroup.getGroupProfileContainer();
            affectedProfiles.addWriteProfile(container.getPlayerData(player), getWorldGroupShares());
        }

        private Shares getWorldGroupShares() {
            return Sharables.fromShares(worldGroup.getShares());
        }
    }
}
