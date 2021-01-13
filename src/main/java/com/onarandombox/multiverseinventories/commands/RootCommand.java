package com.onarandombox.multiverseinventories.commands;

import com.onarandombox.MultiverseCore.commandTools.display.ColorAlternator;
import com.onarandombox.acf.annotation.CommandAlias;
import com.onarandombox.acf.annotation.Description;
import com.onarandombox.multiverseinventories.MultiverseInventories;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class RootCommand extends InventoriesCommand {

    public RootCommand(MultiverseInventories plugin) {
        super(plugin);
    }

    @CommandAlias("mvinv")
    @Description("Multiverse-Inventories")
    public void onRootCommand(@NotNull CommandSender sender) {
        this.plugin.getCore().getMVCommandManager().showPluginInfo(
                sender,
                this.plugin.getDescription(),
                new ColorAlternator(ChatColor.DARK_AQUA, ChatColor.AQUA),
                "mvinv"
        );
    }
}
