package com.onarandombox.multiverseinventories.command;

import com.dumptruckman.minecraft.util.Logging;
import com.onarandombox.acf.BukkitCommandIssuer;
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
import org.jetbrains.annotations.NotNull;

@CommandAlias("mvinv")
public class ImportCommand extends InventoriesCommand {
    public ImportCommand(@NotNull MultiverseInventories plugin) {
        super(plugin);
        setPerm(Perm.COMMAND_IMPORT);
    }

    @Subcommand("import")
    @CommandCompletion("MultiInv|WorldInventories")
    @Syntax("<MultiInv|WorldInventories>")
    @Description("Imports data from another inventory plugin.")
    public void onImportCommand(BukkitCommandIssuer issuer,

                                @Syntax("<MultiInv|WorldInventories>")
                                @Description("The plugin to import data from.")
                                DataImporter importer
    ) {
        try {
            importer.importData();
        } catch (MigrationException e) {
            messager.bad(Message.IMPORT_FAILED, issuer.getIssuer(), importer.getPlugin().getName());
            Logging.severe(e.getMessage());
            e.printStackTrace();
        }
    }
}
