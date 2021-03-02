package com.onarandombox.multiverseinventories.command;

import com.dumptruckman.minecraft.util.Logging;
import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.dataimport.DataImporter;
import com.onarandombox.multiverseinventories.locale.Message;
import com.onarandombox.multiverseinventories.dataimport.DataImportException;
import com.onarandombox.multiverseinventories.util.Perm;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

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
        DataImporter<? extends Plugin> dataImporter = this.plugin.getImportManager().getImporter(args.get(0));
        if (dataImporter == null) {
            this.messager.bad(Message.ERROR_UNSUPPORTED_IMPORT, sender, args.get(0));
            return;
        }
        if (!dataImporter.isEnabled()) {
            this.messager.bad(Message.ERROR_PLUGIN_NOT_ENABLED, sender, dataImporter.getPluginName());
            return;
        }

        this.messager.normal(Message.IMPORT_ATTEMPT, sender, dataImporter.getPluginName());
        if (!dataImporter.importData()) {
            this.messager.bad(Message.IMPORT_FAILED, sender, dataImporter.getPluginName());
            return;
        }

        this.messager.normal(Message.IMPORT_SUCCESSFUL, sender, dataImporter.getPluginName());
    }
}

