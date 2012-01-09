package com.onarandombox.multiverseinventories.command;

import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.pneumaticraft.commandhandler.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * @author dumptruckman, SwearWord
 */
public abstract class InventoriesCommand extends Command {

    private MultiverseInventories plugin;

    public InventoriesCommand(MultiverseInventories plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    protected MultiverseInventories getPlugin() {
        return this.plugin;
    }

    @Override
    public abstract void runCommand(CommandSender sender, List<String> args);
}
