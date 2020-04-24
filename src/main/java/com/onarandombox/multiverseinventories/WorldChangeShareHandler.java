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
    private final ReadProfilesAggregator readProfilesAggregator = new ReadProfilesAggregator();

    WorldChangeShareHandler(MultiverseInventories inventories, Player player,
                                   String fromWorld, String toWorld) {
        super(inventories, player);
        this.fromWorld = fromWorld;
        this.toWorld = toWorld;
        // Get any groups we may need to save stuff to.
        this.fromWorldGroups = this.inventories.getGroupManager().getGroupsForWorld(fromWorld);
        // Get any groups we may need to load stuff from.
        this.toWorldGroups = this.inventories.getGroupManager().getGroupsForWorld(toWorld);
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
        readProfilesAggregator.addReadProfiles();
    }

    private void addWriteProfiles() {
        if (hasFromWorldGroups()) {
            fromWorldGroups.forEach(this::addWriteProfilesForWorldGroup);
        } else {
            Logging.finer("No groups for fromWorld.");
        }
    }

    private boolean hasFromWorldGroups() {
        return !fromWorldGroups.isEmpty();
    }

    private void addWriteProfilesForWorldGroup(WorldGroup worldGroup) {
        if (shouldWriteToWorldGroup(worldGroup)) {
            addWorldGroupToWriteProfiles(worldGroup);
        }
    }

    private boolean shouldWriteToWorldGroup(WorldGroup worldGroup) {
        return isToWorldInWorldGroup(worldGroup) || isWorldGroupNotSharingAll(worldGroup);
    }

    private boolean isToWorldInWorldGroup(WorldGroup worldGroup) {
        return !worldGroup.containsWorld(toWorld);
    }

    private boolean isWorldGroupNotSharingAll(WorldGroup worldGroup) {
        return !worldGroup.getShares().isSharing(Sharables.all());
    }

    private void addWorldGroupToWriteProfiles(WorldGroup worldGroup) {
        ProfileContainer container = worldGroup.getGroupProfileContainer();
        addWriteProfile(container.getPlayerData(player), getWorldGroupShares(worldGroup));
    }

    private Shares getWorldGroupShares(WorldGroup worldGroup) {
        return Sharables.fromShares(worldGroup.getShares());
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
                    if (worldGroupIsNotSharingAll(worldGroup)) {
                        addReadProfileForWorldGroup(worldGroup);
                    } else {
                        sharesToRead = Sharables.allOf();
                    }
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
            PlayerProfile playerProfile = getWorldGroupPlayerData(worldGroup);
            Shares sharesToAdd = getWorldGroupShares(worldGroup);

            addReadProfile(playerProfile, sharesToAdd);
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

}
