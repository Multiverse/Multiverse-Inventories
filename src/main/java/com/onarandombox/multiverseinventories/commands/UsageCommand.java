package com.onarandombox.multiverseinventories.commands;

import com.onarandombox.acf.CommandHelp;
import com.onarandombox.acf.annotation.CommandAlias;
import com.onarandombox.acf.annotation.CommandCompletion;
import com.onarandombox.acf.annotation.Description;
import com.onarandombox.acf.annotation.HelpCommand;
import com.onarandombox.acf.annotation.Subcommand;
import com.onarandombox.acf.annotation.Syntax;
import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.util.Perm;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

@CommandAlias("mvinv")
public class UsageCommand extends InventoriesCommand {

    public UsageCommand(MultiverseInventories plugin) {
        super(plugin);
        this.addPermission(Perm.COMMAND_HELP);
    }

    @HelpCommand
    @Subcommand("help")
    @Syntax("[filter] [page]")
    @CommandCompletion("@subCommands:mvinv")
    @Description("Show Multiverse-Inventories Command usage.")
    public void onUsageCommand(@NotNull CommandSender sender,
                               @NotNull CommandHelp help) {

        this.plugin.getCore().getMVCommandManager().showUsage(help);
    }
}
