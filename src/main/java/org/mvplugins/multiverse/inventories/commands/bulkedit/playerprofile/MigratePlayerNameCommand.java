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

final class MigratePlayerNameCommand extends InventoriesCommand {

    private final CommandQueueManager commandQueueManager;
    private final ProfileDataSource profileDataSource;

    MigratePlayerNameCommand(
            @NotNull CommandQueueManager commandQueueManager,
            @NotNull ProfileDataSource profileDataSource) {
        this.commandQueueManager = commandQueueManager;
        this.profileDataSource = profileDataSource;
    }

    @Subcommand("migrate player-name")
    @CommandPermission("multiverse.inventories.bulkedit")
    @Syntax("<current-name> <new-name>")
    @Description("Only use this if automatic migration failed for some reason.")
    void onCommand(
            MVCommandIssuer issuer,
            String oldName,
            String newName
    ) {
        commandQueueManager.addToQueue(CommandQueuePayload.issuer(issuer)
                .prompt(Message.of("Are you sure you want to migrate all player data for %s to %s? This action cannot be undone."
                        .formatted(oldName, newName)))
                .action(() -> doMigration(issuer, oldName, newName)));
    }

    private void doMigration(MVCommandIssuer issuer, String oldName, String newName) {
        Try.run(() -> profileDataSource.migratePlayerProfileName(oldName, newName))
                .onFailure(e -> issuer.sendMessage("Failed to migrate player data for " + oldName + ". " + e.getMessage()));
    }
}
