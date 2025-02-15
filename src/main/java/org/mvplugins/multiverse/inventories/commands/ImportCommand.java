package org.mvplugins.multiverse.inventories.commands;

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
import org.mvplugins.multiverse.inventories.dataimport.DataImporter;
import org.mvplugins.multiverse.inventories.util.MVInvi18n;

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
    void onImportCommand(
            MVCommandIssuer issuer,

            @Single
            @Syntax("<MultiInv|WorldInventories|PerWorldInventory>")
            String pluginName) {

        dataImportManager.getImporter(pluginName)
                .onEmpty(() -> issuer.sendError(MVInvi18n.IMPORT_UNSUPPORTEDPLUGIN, replace("{plugin}").with(pluginName)))
                .peek(dataImporter -> {
                    if (!dataImporter.isEnabled()) {
                        issuer.sendError(MVInvi18n.IMPORT_PLUGINNOTENABLED, replace("{plugin}").with(pluginName));
                        return;
                    }
                    commandQueueManager.addToQueue(CommandQueuePayload.issuer(issuer)
                            .prompt(Message.of(MVInvi18n.IMPORT_CONFIRMPROMPT, replace("{plugin}").with(pluginName)))
                            .action(() -> doDataImport(issuer, dataImporter)));
                });
    }

    void doDataImport(MVCommandIssuer issuer, DataImporter dataImporter) {
        if (dataImporter.importData()) {
            issuer.sendInfo(MVInvi18n.IMPORT_SUCCESS, replace("{plugin}").with(dataImporter.getPluginName()));
        } else {
            issuer.sendError(MVInvi18n.IMPORT_FAILED, replace("{plugin}").with(dataImporter.getPluginName()));
        }
    }
}
