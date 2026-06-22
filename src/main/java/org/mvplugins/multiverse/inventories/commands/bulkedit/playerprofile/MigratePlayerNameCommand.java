package org.mvplugins.multiverse.inventories.commands.bulkedit.playerprofile;

import org.mvplugins.multiverse.core.command.MVCommandIssuer;
import org.mvplugins.multiverse.core.command.queue.CommandQueueManager;
import org.mvplugins.multiverse.core.command.queue.CommandQueuePayload;
import org.mvplugins.multiverse.core.locale.message.Message;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandPermission;
import org.mvplugins.multiverse.external.acf.commands.annotation.Description;
import org.mvplugins.multiverse.external.acf.commands.annotation.Subcommand;
import org.mvplugins.multiverse.external.acf.commands.annotation.Syntax;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.external.vavr.control.Try;
import org.mvplugins.multiverse.inventories.commands.InventoriesCommand;
import org.mvplugins.multiverse.inventories.profile.ProfileDataSource;
import org.mvplugins.multiverse.inventories.util.MVInvi18n;

import static org.mvplugins.multiverse.core.locale.message.MessageReplacement.replace;

final class MigratePlayerNameCommand extends InventoriesCommand {

    private final CommandQueueManager commandQueueManager;
    private final ProfileDataSource profileDataSource;

    MigratePlayerNameCommand(
            @NotNull CommandQueueManager commandQueueManager,
            @NotNull ProfileDataSource profileDataSource) {
        this.commandQueueManager = commandQueueManager;
        this.profileDataSource = profileDataSource;
    }

    @Subcommand("bulkedit migrate player-name")
    @CommandPermission("multiverse.inventories.bulkedit")
    @Syntax("<current-name> <new-name>")
    @Description("Only use this if automatic migration failed for some reason.")
    void onCommand(
            MVCommandIssuer issuer,
            String oldName,
            String newName
    ) {
        commandQueueManager.addToQueue(CommandQueuePayload.issuer(issuer)
                .prompt(Message.of(MVInvi18n.BULKEDIT_PLAYERPROFILE_MIGRATEPLAYERNAME_CONFIRMPROMPT,
                        replace("{oldname}").with(oldName),
                        replace("{newname}").with(newName)))
                .action(() -> doMigration(issuer, oldName, newName)));
    }

    private void doMigration(MVCommandIssuer issuer, String oldName, String newName) {
        Try.run(() -> profileDataSource.migratePlayerProfileName(oldName, newName))
                .onFailure(e -> issuer.sendMessage(MVInvi18n.BULKEDIT_PLAYERPROFILE_MIGRATEPLAYERNAME_FAILED,
                        replace("{player}").with(oldName),
                        replace("{error}").with(e)));
    }
}
