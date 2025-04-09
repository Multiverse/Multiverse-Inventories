package org.mvplugins.multiverse.inventories.commands;

import org.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.core.command.MVCommandManager;
import org.mvplugins.multiverse.external.acf.commands.CommandHelp;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandAlias;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandCompletion;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandPermission;
import org.mvplugins.multiverse.external.acf.commands.annotation.Description;
import org.mvplugins.multiverse.external.acf.commands.annotation.Subcommand;
import org.mvplugins.multiverse.external.acf.commands.annotation.Syntax;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;

@Service

final class HelpCommand extends InventoriesCommand {

    private final MVCommandManager commandManager;

    @Inject
    HelpCommand(@NotNull MVCommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @org.mvplugins.multiverse.external.acf.commands.annotation.HelpCommand
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
