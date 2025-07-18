package org.mvplugins.multiverse.inventories.commands;

import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.core.command.MVCommandIssuer;
import org.mvplugins.multiverse.core.utils.REPatterns;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandCompletion;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandPermission;
import org.mvplugins.multiverse.external.acf.commands.annotation.Conditions;
import org.mvplugins.multiverse.external.acf.commands.annotation.Description;
import org.mvplugins.multiverse.external.acf.commands.annotation.Optional;
import org.mvplugins.multiverse.external.acf.commands.annotation.Subcommand;
import org.mvplugins.multiverse.external.acf.commands.annotation.Syntax;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.inventories.profile.group.WorldGroup;
import org.mvplugins.multiverse.inventories.profile.group.WorldGroupManager;
import org.mvplugins.multiverse.inventories.share.Shares;
import org.mvplugins.multiverse.inventories.util.MVInvi18n;

import java.util.List;

import static org.mvplugins.multiverse.core.locale.message.MessageReplacement.replace;

@Service
final class CreateGroupCommand extends InventoriesCommand {
    private final WorldGroupManager worldGroupManager;

    @Inject
    CreateGroupCommand(@NotNull WorldGroupManager worldGroupManager) {
        this.worldGroupManager = worldGroupManager;
    }

    @Subcommand("create-group")
    @CommandPermission("multiverse.inventories.creategroup")
    @CommandCompletion("@empty @mvworlds:multiple,scope=both @shares")
    @Syntax("<name> [share[,extra]] [world[,extra]]")
    @Description("Creates a new empty World Group with no worlds and no shares.")
    void onCreateGroupCommand(
            MVCommandIssuer issuer,

            @Conditions("newWorldGroupName")
            @Syntax("<name>")
            @Description("New group name to create.")
            @NotNull String groupName,

            @Optional
            @Syntax("[world[,extra]]")
            String worlds,

            @Optional
            @Syntax("[share,[extra]]")
            Shares shares
    ) {
        WorldGroup worldGroup = worldGroupManager.newEmptyGroup(groupName);
        if (worlds != null) {
            worldGroup.addWorlds(List.of(REPatterns.COMMA.split(worlds)), false);
        }
        if (shares != null) {
            worldGroup.getShares().mergeShares(shares);
        }
        worldGroupManager.updateGroup(worldGroup);
        issuer.sendInfo(MVInvi18n.GROUP_CREATIONCOMPLETE, replace("{group}").with(groupName));
        issuer.sendInfo(MVInvi18n.INFO_GROUP_INFO, replace("{worlds}").with(worldGroup.getConfigWorlds()));
        issuer.sendInfo(MVInvi18n.INFO_GROUP_INFOSHARES, replace("{shares}").with(worldGroup.getShares()));
    }
}
