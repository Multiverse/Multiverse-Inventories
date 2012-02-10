package com.onarandombox.multiverseinventories.command;

import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.locale.Messager;
import com.pneumaticraft.commandhandler.multiverse.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * A base command class to easily retrieve the plugin associated.
 */
public abstract class InventoriesCommand extends Command {

    /**
     * Instance of MultiverseInventories.
     */
    protected MultiverseInventories plugin;
    /**
     * Instance of messager used for Inventories.
     */
    protected Messager messager;

    public InventoriesCommand(MultiverseInventories plugin) {
        super(plugin);
        this.plugin = plugin;
        this.messager = plugin.getMessager();
    }

    @Override
    public abstract void runCommand(CommandSender sender, List<String> args);
}


