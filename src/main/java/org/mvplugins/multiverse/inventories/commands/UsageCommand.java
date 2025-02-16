package org.mvplugins.multiverse.inventories.commands;

import org.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.core.commandtools.MVCommandManager;
import org.mvplugins.multiverse.external.acf.commands.CommandHelp;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandAlias;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandCompletion;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandPermission;
import org.mvplugins.multiverse.external.acf.commands.annotation.Description;
import org.mvplugins.multiverse.external.acf.commands.annotation.HelpCommand;
import org.mvplugins.multiverse.external.acf.commands.annotation.Subcommand;
import org.mvplugins.multiverse.external.acf.commands.annotation.Syntax;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;

@Service
@CommandAlias("mvinv")
final class UsageCommand extends InventoriesCommand {

    @Inject
    UsageCommand(@NotNull MVCommandManager commandManager) {
        super(commandManager);
    }

    @HelpCommand
    @Subcommand("help")
    @CommandPermission("multiverse.inventories.help")
    @CommandCompletion("@commands:mvinv")
    @Syntax("[filter] [page]")
    @Description("Displays a list of available commands.")
    void onUsageCommand(CommandHelp help) {
        if (help.getIssuer().isPlayer()) {
            // Prevent flooding the chat
            help.setPerPage(4);
        }
        this.commandManager.showUsage(help);
    }
}
