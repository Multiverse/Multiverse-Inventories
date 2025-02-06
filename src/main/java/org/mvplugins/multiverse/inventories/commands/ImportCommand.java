package org.mvplugins.multiverse.inventories.commands;

import com.dumptruckman.minecraft.util.Logging;
import org.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.core.commandtools.MVCommandIssuer;
import org.mvplugins.multiverse.core.commandtools.MVCommandManager;
import org.mvplugins.multiverse.core.commandtools.queue.CommandQueueManager;
import org.mvplugins.multiverse.core.commandtools.queue.CommandQueuePayload;
import org.mvplugins.multiverse.core.locale.message.Message;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandAlias;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandCompletion;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandPermission;
import org.mvplugins.multiverse.external.acf.commands.annotation.Description;
import org.mvplugins.multiverse.external.acf.commands.annotation.Single;
import org.mvplugins.multiverse.external.acf.commands.annotation.Subcommand;
import org.mvplugins.multiverse.external.acf.commands.annotation.Syntax;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.inventories.dataimport.DataImportManager;

import static org.mvplugins.multiverse.core.locale.message.MessageReplacement.replace;

@Service
@CommandAlias("mvinv")
final class ImportCommand extends InventoriesCommand {

    private final DataImportManager dataImportManager;
    private final CommandQueueManager commandQueueManager;

    @Inject
    ImportCommand(
            @NotNull MVCommandManager commandManager,
            @NotNull DataImportManager dataImportManager,
            @NotNull CommandQueueManager commandQueueManager) {
        super(commandManager);
        this.dataImportManager = dataImportManager;
        this.commandQueueManager = commandQueueManager;
    }

    @Subcommand("import")
    @Syntax("<MultiInv|WorldInventories|PerWorldInventory>")
    @CommandPermission("multiverse.inventories.import")
    @CommandCompletion("MultiInv|WorldInventories|PerWorldInventory")
    @Description("Import inventories from MultiInv/WorldInventories/PerWorldInventory plugin.")
    public void onImportCommand(
            MVCommandIssuer issuer,

            @Single
            @Syntax("<MultiInv|WorldInventories|PerWorldInventory>")
            String pluginName) {
        commandQueueManager.addToQueue(CommandQueuePayload.issuer(issuer)
                .prompt(Message.of("Are you sure you want to import data from {plugin}? This will override existing Multiverse-Inventories playerdata!!!",
                                replace("{plugin}").with(pluginName)))
                .action(() -> doDataImport(issuer, pluginName)));
    }

    void doDataImport(MVCommandIssuer issuer, String pluginName) {
        dataImportManager.getImporter(pluginName)
                .onEmpty(() -> issuer.sendMessage("No importer found for " + pluginName))
                .peek(dataImporter -> {
                    if (!dataImporter.isEnabled()) {
                        issuer.sendMessage("Plugin " + pluginName + " is not running on your server!");
                        return;
                    }
                    if (dataImporter.importData()) {
                        issuer.sendMessage("Successfully to imported data from " + pluginName + "!");
                    } else {
                        issuer.sendMessage("Failed to import data from " + pluginName + ".");
                    }
                });
    }
}
