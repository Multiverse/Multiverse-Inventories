package org.mvplugins.multiverse.inventories.commands;

import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.core.command.MVCommandIssuer;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandPermission;
import org.mvplugins.multiverse.external.acf.commands.annotation.Description;
import org.mvplugins.multiverse.external.acf.commands.annotation.Subcommand;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.inventories.profile.group.GroupingConflictResult;
import org.mvplugins.multiverse.inventories.profile.group.WorldGroupManager;
import org.mvplugins.multiverse.inventories.util.MVInvi18n;

@Service
final class CheckGroupConflictsCommand extends InventoriesCommand {
    private final WorldGroupManager worldGroupManager;

    @Inject
    CheckGroupConflictsCommand(@NotNull WorldGroupManager worldGroupManager) {
        this.worldGroupManager = worldGroupManager;
    }

    @Subcommand("check-group-conflicts")
    @CommandPermission("multiverse.inventories.checkgroupconflict")
    @Description("Check for conflicts in World Groups.")
    void onCommand(@NotNull MVCommandIssuer issuer) {
        issuer.sendInfo(MVInvi18n.CONFLICT_CHECKING);
        GroupingConflictResult groupingConflictResult = worldGroupManager.checkForConflicts();
        if (groupingConflictResult.hasConflict()) {
            groupingConflictResult.sendConflictIssue(issuer);
        } else {
            issuer.sendInfo(MVInvi18n.CONFLICT_NOTFOUND);
        }
    }
}
