package org.mvplugins.multiverse.inventories.commands;

import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.core.command.MVCommandIssuer;
import org.mvplugins.multiverse.core.utils.REPatterns;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandCompletion;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandPermission;
import org.mvplugins.multiverse.external.acf.commands.annotation.Description;
import org.mvplugins.multiverse.external.acf.commands.annotation.Single;
import org.mvplugins.multiverse.external.acf.commands.annotation.Subcommand;
import org.mvplugins.multiverse.external.acf.commands.annotation.Syntax;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.inventories.profile.group.WorldGroup;
import org.mvplugins.multiverse.inventories.profile.group.WorldGroupManager;
import org.mvplugins.multiverse.inventories.util.MVInvi18n;

import java.util.Arrays;
import java.util.List;

import static org.mvplugins.multiverse.core.locale.message.MessageReplacement.replace;


@Service
final class RemoveWorldsCommand extends InventoriesCommand {

    private final WorldGroupManager worldGroupManager;

    @Inject
    RemoveWorldsCommand(@NotNull WorldGroupManager worldGroupManager) {
        this.worldGroupManager = worldGroupManager;
    }

    @Subcommand("remove-worlds")
    @CommandPermission("multiverse.inventories.removeworlds")
    @CommandCompletion("@worldGroups @worldGroupWorlds")
    @Syntax("<group>, <world[,extra]>")
    @Description("Adds a World to a World Group.")
    void onRemoveWorldCommand(
            MVCommandIssuer issuer,

            @Syntax("<group>")
            @Description("Group you want to remove the world from.")
            @NotNull WorldGroup group,

            @Single
            @Syntax("<world[,extra]>")
            @Description("World name to remove.")
            String worldNames
    ) {
        List<String> worldNamesArr = Arrays.stream(REPatterns.COMMA.split(worldNames)).toList();
        if (!group.removeWorlds(worldNamesArr)) {
            issuer.sendError(MVInvi18n.REMOVEWORLD_WORLDNOTINGROUP,
                    replace("{group}").with(group.getName()),
                    replace("{world}").with(worldNames));
            return;
        }
        issuer.sendInfo(MVInvi18n.REMOVEWORLD_WORLDREMOVED,
                replace("{group}").with(group.getName()),
                replace("{world}").with(worldNames));
        worldGroupManager.checkForConflicts().sendConflictIssue(issuer);
    }
}
