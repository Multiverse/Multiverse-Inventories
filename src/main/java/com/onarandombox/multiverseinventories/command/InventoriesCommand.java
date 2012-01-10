package com.onarandombox.multiverseinventories.command;

import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.pneumaticraft.commandhandler.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * A base command class to easily retrieve the plugin associated.
 */
public abstract class InventoriesCommand extends Command {

    private MultiverseInventories plugin;

    public InventoriesCommand(MultiverseInventories plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    /**
     * Returns the instance of MultiverseInventories passed to this command.
     *
     * @return Instance of MultiverseInventories
     */
    protected MultiverseInventories getPlugin() {
        return this.plugin;
    }

    @Override
    public abstract void runCommand(CommandSender sender, List<String> args);
}
