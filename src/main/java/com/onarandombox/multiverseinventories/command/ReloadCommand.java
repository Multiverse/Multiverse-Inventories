package com.onarandombox.multiverseinventories.command;

import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.locale.MultiverseMessage;
import com.onarandombox.multiverseinventories.permission.MVIPerms;
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
        this.setPermission(MVIPerms.COMMAND_RELOAD.getPerm());
    }

    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        this.getPlugin().reloadConfig();
        this.getPlugin().getMessager().normal(MultiverseMessage.RELOAD_COMPLETE, sender);
    }
}

