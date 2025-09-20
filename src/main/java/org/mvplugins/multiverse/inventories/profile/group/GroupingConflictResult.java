package org.mvplugins.multiverse.inventories.profile.group;

import org.jetbrains.annotations.ApiStatus;
import org.mvplugins.multiverse.core.command.MVCommandIssuer;
import org.mvplugins.multiverse.inventories.util.MVInvi18n;

import java.util.List;

import static org.mvplugins.multiverse.core.locale.message.MessageReplacement.replace;

/**
 * Represents the result of checking for grouping conflicts.
 * This class encapsulates a list of conflicts found between world groups.
 */
@ApiStatus.AvailableSince("5.2")
public final class GroupingConflictResult {
    private final List<GroupingConflict> conflicts;

    GroupingConflictResult(List<GroupingConflict> conflicts) {
        this.conflicts = conflicts;
    }

    /**
     * Checks if there are any conflicts found.
     *
     * @return true if there are conflicts, false otherwise.
     */
    @ApiStatus.AvailableSince("5.2")
    public boolean hasConflict() {
        return !conflicts.isEmpty();
    }

    /**
     * Sends message to the issuer detailing the conflicts found.
     *
     * @param issuer    the sender of the message
     */
    @ApiStatus.AvailableSince("5.2")
    public void sendConflictIssue(MVCommandIssuer issuer) {
        if (conflicts.isEmpty()) {
            return;
        }
        conflicts.forEach(conflict -> issuer.sendInfo(MVInvi18n.CONFLICT_RESULTS,
                replace("{group1}").with(conflict.getFirstGroup().getName()),
                replace("{group2}").with(conflict.getSecondGroup().getName()),
                replace("{shares}").with(conflict.getConflictingShares().toString()),
                replace("{worlds}").with(conflict.getWorldsString())));
        issuer.sendInfo(MVInvi18n.CONFLICT_FOUND);
    }

    /**
     * Gets the list of conflicts found.
     *
     * @return a list of {@link GroupingConflict} objects representing the conflicts.
     */
    @ApiStatus.AvailableSince("5.2")
    public List<GroupingConflict> getConflicts() {
        return conflicts;
    }
}
