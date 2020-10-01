package com.onarandombox.multiverseinventories;

import com.dumptruckman.minecraft.util.Logging;
import com.onarandombox.multiverseinventories.event.ShareHandlingEvent;
import com.onarandombox.multiverseinventories.event.WorldChangeShareHandlingEvent;
import com.onarandombox.multiverseinventories.profile.PlayerProfile;
import com.onarandombox.multiverseinventories.profile.container.ProfileContainer;
import com.onarandombox.multiverseinventories.share.Sharables;
import com.onarandombox.multiverseinventories.share.Shares;
import com.onarandombox.multiverseinventories.util.Perm;
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
        this.fromWorldGroups = getAffectedWorldGroups(fromWorld);
        // Get any groups we may need to load stuff from.
        this.toWorldGroups = getAffectedWorldGroups(toWorld);

        prepareProfiles();
    }

    private List<WorldGroup> getAffectedWorldGroups(String world) {
        return this.inventories.getGroupManager().getGroupsForWorld(world);
    }

    @Override
    protected ShareHandlingEvent createEvent() {
        return new WorldChangeShareHandlingEvent(player, affectedProfiles, fromWorld, toWorld);
    }

    private void prepareProfiles() {
        Logging.finer("=== %s traveling from world: %s to world: %s ===", player.getName(), fromWorld, toWorld);

        setAlwaysWriteWorldProfile();

        if (isPlayerAffectedByChange()) {
            addProfiles();
        }
    }

    private void setAlwaysWriteWorldProfile() {
        // We will always save everything to the world they come from.
        PlayerProfile fromWorldProfile = getWorldPlayerProfile(fromWorld, player);
        setAlwaysWriteProfile(fromWorldProfile);
    }

    private PlayerProfile getWorldPlayerProfile(String world, Player player) {
        return getWorldProfile(world).getPlayerData(player);
    }

    private ProfileContainer getWorldProfile(String world) {
        return inventories.getWorldProfileContainerStore().getContainer(world);
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

    private void addProfiles() {
        addWriteProfiles();
        new ReadProfilesAggregator().addReadProfiles();
    }

    private void addWriteProfiles() {
        if (hasFromWorldGroups()) {
            fromWorldGroups.forEach(wg -> new WorldGroupWrapper(wg).conditionallyAddWriteProfiles());
        } else {
            Logging.finer("No groups for fromWorld.");
        }
    }

    private boolean hasFromWorldGroups() {
        return !fromWorldGroups.isEmpty();
    }

    private class ReadProfilesAggregator {

        private Shares sharesToRead;

        private void addReadProfiles() {
            sharesToRead = Sharables.noneOf();
            //Shares optionalSharesToUpdate = Sharables.noneOptional();

            addReadProfilesFromToWorldGroups();

            useToWorldForMissingShares();
        }

        private void addReadProfilesFromToWorldGroups() {
            if (hasToWorldGroups()) {
                toWorldGroups.forEach(this::conditionallyAddReadProfileForWorldGroup);
            } else {
                Logging.finer("No groups for toWorld.");
            }
        }

        private boolean hasToWorldGroups() {
            return !toWorldGroups.isEmpty();
        }

        private void conditionallyAddReadProfileForWorldGroup(WorldGroup worldGroup) {
            if (isPlayerAffectedByChange(worldGroup)) {
                if (isFromWorldNotInToWorldGroup(worldGroup)) {
                    addReadProfileForWorldGroup(worldGroup);
                } else {
                    addReadProfileForWorldGroup(worldGroup, true);
                }
            }
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
            return !worldGroup.containsWorld(fromWorld);
        }

        private void addReadProfileForWorldGroup(WorldGroup worldGroup) {
            addReadProfileForWorldGroup(worldGroup, false);
        }

        private void addReadProfileForWorldGroup(WorldGroup worldGroup, boolean compliment) {
            PlayerProfile playerProfile = getWorldGroupPlayerData(worldGroup);
            Shares sharesToAdd = getWorldGroupShares(worldGroup);

            if (compliment) {
                addReadProfile(playerProfile, Sharables.complimentOf(sharesToAdd));
            } else {
                addReadProfile(playerProfile, sharesToAdd);
            }

            sharesToRead.addAll(sharesToAdd);
        }

        private PlayerProfile getWorldGroupPlayerData(WorldGroup worldGroup) {
            return getWorldGroupProfileContainer(worldGroup).getPlayerData(player);
        }

        private ProfileContainer getWorldGroupProfileContainer(WorldGroup worldGroup) {
            return worldGroup.getGroupProfileContainer();
        }

        private Shares getWorldGroupShares(WorldGroup worldGroup) {
            return Sharables.fromShares(worldGroup.getShares());
        }

        private boolean worldGroupIsNotSharingAll(WorldGroup worldGroup) {
            return !worldGroup.getShares().isSharing(Sharables.all());
        }

        private void useToWorldForMissingShares() {
            // We need to fill in any sharables that are not going to be transferred with what's saved in the world file.
            if (hasUnhandedShares()) {
                addUnhandledSharesFromToWorld();
            }
        }

        private boolean hasUnhandedShares() {
            return !sharesToRead.isSharing(Sharables.all());
        }

        private void addUnhandledSharesFromToWorld() {
            Shares unhandledShares = Sharables.complimentOf(sharesToRead);

            Logging.finer("%s are left unhandled, defaulting to toWorld", unhandledShares);

            addReadProfile(getToWorldPlayerData(), unhandledShares);
        }

        private PlayerProfile getToWorldPlayerData() {
            return getToWorldProfileContainer().getPlayerData(player);
        }

        private ProfileContainer getToWorldProfileContainer() {
            return inventories.getWorldProfileContainerStore().getContainer(toWorld);
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
