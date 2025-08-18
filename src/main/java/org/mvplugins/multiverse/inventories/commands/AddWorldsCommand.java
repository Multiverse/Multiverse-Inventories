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
final class AddWorldsCommand extends InventoriesCommand {

    private final WorldGroupManager worldGroupManager;

    @Inject
    AddWorldsCommand(@NotNull WorldGroupManager worldGroupManager) {
        this.worldGroupManager = worldGroupManager;
    }

    @Subcommand("add-worlds")
    @CommandPermission("multiverse.inventories.addworlds")
    @CommandCompletion("@worldGroups @mvworlds:multiple,scope=both")
    @Syntax("<group> <world[,extra]>")
    @Description("Adds a World to a World Group.")
    void onAddWorldCommand(
            MVCommandIssuer issuer,

            @Syntax("<group>")
            @Description("Group you want to add the world to.")
            WorldGroup group,

            @Single
            @Syntax("<world>")
            @Description("World name to add.")
            String worlds
    ) {
        List<String> worldNames = Arrays.stream(REPatterns.COMMA.split(worlds))
                .map(String::toLowerCase)
                .toList();
        String worldNamesString = String.join(", ", worldNames);
        if (!group.getConfigWorlds().addAll(worldNames)) {
            issuer.sendError(MVInvi18n.ADDWORLD_WORLDALREADYEXISTS,
                    replace("{group}").with(group.getName()),
                    replace("{world}").with(worldNamesString));
            return;
        }
        worldGroupManager.updateGroup(group);
        issuer.sendInfo(MVInvi18n.ADDWORLD_WORLDADDED,
                replace("{group}").with(group.getName()),
                replace("{world}").with(worldNamesString));
        worldGroupManager.checkForConflicts().sendConflictIssue(issuer);
    }
}
