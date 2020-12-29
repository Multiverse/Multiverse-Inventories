package com.onarandombox.multiverseinventories.commands_acf;

import com.dumptruckman.minecraft.util.Logging;
import com.onarandombox.acf.annotation.CommandAlias;
import com.onarandombox.acf.annotation.CommandCompletion;
import com.onarandombox.acf.annotation.Description;
import com.onarandombox.acf.annotation.Subcommand;
import com.onarandombox.acf.annotation.Syntax;
import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.locale.Message;
import com.onarandombox.multiverseinventories.migration.DataImporter;
import com.onarandombox.multiverseinventories.migration.MigrationException;
import com.onarandombox.multiverseinventories.util.Perm;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

@CommandAlias("mvinv")
public class ImportCommand extends InventoriesCommand {

    public ImportCommand(MultiverseInventories plugin) {
        super(plugin);
        this.addPermission(Perm.COMMAND_IMPORT);
    }

    @Subcommand("import")
    @Syntax("<MultiInv|WorldInventories>")
    @CommandCompletion("@invPluginImports")
    @Description("Import inventories from MultiInv/WorldInventories plugin.")
    public void onImportCommand(@NotNull CommandSender sender,
                                @NotNull DataImporter importer) {

        try {
            importer.importData();
        }
        catch (MigrationException e) {
            this.messager.normal(Message.IMPORT_FAILED, sender, importer.getPlugin().getName());
            Logging.severe(e.getMessage());
            Logging.severe("Cause: " + e.getCauseException().getMessage());
            return;
        }

        this.messager.normal(Message.IMPORT_SUCCESSFUL, sender, importer.getPlugin().getName());
    }
}
