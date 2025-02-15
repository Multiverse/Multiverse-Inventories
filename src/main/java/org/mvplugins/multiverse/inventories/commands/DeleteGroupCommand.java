package org.mvplugins.multiverse.inventories.commands;

import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.core.commandtools.MVCommandIssuer;
import org.mvplugins.multiverse.core.commandtools.MVCommandManager;
import org.mvplugins.multiverse.core.commandtools.queue.CommandQueueManager;
import org.mvplugins.multiverse.core.commandtools.queue.CommandQueuePayload;
import org.mvplugins.multiverse.core.locale.message.Message;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandAlias;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandCompletion;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandPermission;
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
final class DeleteGroupCommand extends InventoriesCommand {
    private final CommandQueueManager commandQueueManager;
    private final WorldGroupManager worldGroupManager;

    @Inject
    DeleteGroupCommand(@NotNull MVCommandManager commandManager,
                              @NotNull CommandQueueManager commandQueueManager,
                              @NotNull WorldGroupManager worldGroupManager) {
        super(commandManager);
        this.commandQueueManager = commandQueueManager;
        this.worldGroupManager = worldGroupManager;
    }

    @Subcommand("deletegroup")
    @CommandPermission("multiverse.inventories.deletegroup")
    @CommandCompletion("@worldGroups")
    @Syntax("<group>")
    @Description("Deletes a World Group.")
    void onDeleteGroupCommand(
            MVCommandIssuer issuer,

            @Syntax("<group>")
            @Description("Inventories group to delete.")
            WorldGroup group
    ) {
        commandQueueManager.addToQueue(CommandQueuePayload.issuer(issuer)
                .prompt(Message.of(MVInvi18n.DELETEGROUP_CONFIRMPROMPT, replace("{group}").with(group.getName())))
                .action(() -> doDeleteGroup(issuer, group)));
    }

    private void doDeleteGroup(MVCommandIssuer issuer, WorldGroup group) {
        worldGroupManager.removeGroup(group);
        issuer.sendInfo(MVInvi18n.DELETEGROUP_SUCCESS, replace("{group}").with(group.getName()));
    }
}
