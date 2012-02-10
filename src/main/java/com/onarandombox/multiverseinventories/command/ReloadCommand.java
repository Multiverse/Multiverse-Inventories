package com.onarandombox.multiverseinventories.command;

import com.onarandombox.multiverseinventories.util.Perm;
import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.locale.Message;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * The /mvi info Command.
 */
public class ReloadCommand extends InventoriesCommand {

    public ReloadCommand(MultiverseInventories plugin) {
        super(plugin);
        this.setName("Reloads config file");
        this.setCommandUsage("/mvinv reload");
        this.setArgRange(0, 0);
        this.addKey("mvinv reload");
        this.addKey("mvinvreload");
        this.setPermission(Perm.COMMAND_RELOAD.getPermission());
    }

    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        this.plugin.reloadConfig();
        this.messager.normal(Message.RELOAD_COMPLETE, sender);
    }
}

