package com.onarandombox.multiverseinventories.command;

import com.onarandombox.multiverseinventories.util.Perm;
import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.locale.Message;
import com.onarandombox.multiverseinventories.migration.DataImporter;
import com.onarandombox.multiverseinventories.migration.MigrationException;
import com.onarandombox.multiverseinventories.util.Logging;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * The /mvi info Command.
 */
public class ImportCommand extends InventoriesCommand {

    public ImportCommand(MultiverseInventories plugin) {
        super(plugin);
        this.setName("Import from MultiInv/WorldInventories");
        this.setCommandUsage("/mvinv import " + ChatColor.GREEN + "{MultiInv|WorldInventories}");
        this.setArgRange(1, 1);
        this.addKey("mvinv import");
        this.addKey("mvinvim");
        this.addKey("mvinvimport");
        this.setPermission(Perm.COMMAND_IMPORT.getPermission());
    }

    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        DataImporter importer = null;
        if (args.get(0).equalsIgnoreCase("MultiInv")) {
            importer = this.plugin.getImportManager().getMultiInvImporter();
        } else if (args.get(0).equalsIgnoreCase("WorldInventories")) {
            importer = this.plugin.getImportManager().getWorldInventoriesImporter();
        } else {
            this.messager.bad(Message.ERROR_PLUGIN_NOT_ENABLED,
                    sender, args.get(0));
            return;
        }
        if (importer == null) {
            this.messager.bad(Message.ERROR_PLUGIN_NOT_ENABLED,
                    sender, args.get(0));
        } else {
            try {
                importer.importData();
            } catch (MigrationException e) {
                Logging.severe(e.getMessage());
                Logging.severe("Cause: " + e.getCauseException().getMessage());
            }
        }
    }
}

