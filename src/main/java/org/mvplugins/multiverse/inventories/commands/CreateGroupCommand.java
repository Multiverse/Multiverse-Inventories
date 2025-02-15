package org.mvplugins.multiverse.inventories.commands;

import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.core.commandtools.MVCommandIssuer;
import org.mvplugins.multiverse.core.commandtools.MVCommandManager;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandAlias;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandPermission;
import org.mvplugins.multiverse.external.acf.commands.annotation.Conditions;
import org.mvplugins.multiverse.external.acf.commands.annotation.Description;
import org.mvplugins.multiverse.external.acf.commands.annotation.Subcommand;
import org.mvplugins.multiverse.external.acf.commands.annotation.Syntax;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.inventories.profile.group.WorldGroup;
import org.mvplugins.multiverse.inventories.profile.group.WorldGroupManager;
import org.mvplugins.multiverse.inventories.util.MVInvi18n;

import static org.mvplugins.multiverse.core.locale.message.MessageReplacement.replace;

@Service
@CommandAlias("mvinv")
final class CreateGroupCommand extends InventoriesCommand {
    private final WorldGroupManager worldGroupManager;

    @Inject
    CreateGroupCommand(@NotNull MVCommandManager commandManager, @NotNull WorldGroupManager worldGroupManager) {
        super(commandManager);
        this.worldGroupManager = worldGroupManager;
    }

    @Subcommand("creategroup")
    @CommandPermission("multiverse.inventories.creategroup")
    @Syntax("<name>")
    @Description("Creates a new empty World Group with no worlds and no shares.")
    void onCreateGroupCommand(
            MVCommandIssuer issuer,

            @Conditions("newWorldGroupName")
            @Syntax("<name>")
            @Description("New group name to create.")
            @NotNull String groupName
    ) {
        WorldGroup worldGroup = worldGroupManager.newEmptyGroup(groupName);
        worldGroupManager.updateGroup(worldGroup);
        issuer.sendInfo(MVInvi18n.GROUP_CREATIONCOMPLETE, replace("{group}").with(groupName));
    }
}
