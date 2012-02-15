package com.onarandombox.multiverseinventories.command;

import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.locale.Message;
import com.onarandombox.multiverseinventories.util.Logging;
import com.onarandombox.multiverseinventories.util.Perm;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * Enables debug-information.
 */
public class DebugCommand extends InventoriesCommand {

    public DebugCommand(MultiverseInventories plugin) {
        super(plugin);
        this.setName("Turn Debug on/off?");
        this.setCommandUsage("/mvinv debug" + ChatColor.GOLD + " [1|2|3|off]");
        this.setArgRange(0, 1);
        this.addKey("mvinv debug");
        this.addKey("mvinv d");
        this.addKey("mvinvdebug");
        this.addKey("mvinvd");
        this.addCommandExample("/mvinv debug " + ChatColor.GOLD + "2");
        this.setPermission(Perm.COMMAND_DEBUG.getPermission());
    }

    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        if (args.size() == 1) {
            if (args.get(0).equalsIgnoreCase("off")) {
                this.plugin.getMVIConfig().setGlobalDebug(0);
                this.plugin.getMVIConfig().save();
            } else {
                try {
                    int debugLevel = Integer.parseInt(args.get(0));
                    if (debugLevel > 3 || debugLevel < 0) {
                        throw new NumberFormatException();
                    }
                    this.plugin.getMVIConfig().setGlobalDebug(debugLevel);
                    this.plugin.getMVIConfig().save();
                } catch (NumberFormatException e) {
                    messager.bad(Message.INVALID_DEBUG, sender);
                }
            }
        }
        this.displayDebugMode(sender);
    }

    private void displayDebugMode(CommandSender sender) {
        if (this.plugin.getMVIConfig().getGlobalDebug() == 0) {
            messager.normal(Message.DEBUG_SET, sender, ChatColor.RED + messager.getMessage(Message.GENERIC_OFF));
        } else {
            messager.normal(Message.DEBUG_SET, sender, ChatColor.GREEN
                    + Integer.toString(this.plugin.getMVIConfig().getGlobalDebug()));
            Logging.fine("Multiverse-Inventories Debug ENABLED");
        }
    }
}
