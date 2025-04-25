package org.mvplugins.multiverse.inventories.commands;

import org.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.core.command.MVCommandIssuer;
import org.mvplugins.multiverse.core.command.queue.CommandQueueManager;
import org.mvplugins.multiverse.core.command.queue.CommandQueuePayload;
import org.mvplugins.multiverse.core.locale.message.Message;
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
final class MigrateCommand extends InventoriesCommand {

    private final DataImportManager dataImportManager;
    private final CommandQueueManager commandQueueManager;

    @Inject
    MigrateCommand(
            @NotNull DataImportManager dataImportManager,
            @NotNull CommandQueueManager commandQueueManager
    ) {
        this.dataImportManager = dataImportManager;
        this.commandQueueManager = commandQueueManager;
    }

    @Subcommand("migrate")
    @Syntax("<MultiInv|WorldInventories|PerWorldInventory>")
    @CommandPermission("multiverse.inventories.import")
    @CommandCompletion("@dataimporters")
    @Description("Import inventories from MultiInv/WorldInventories/PerWorldInventory plugin.")
    void onMigrateCommand(
            MVCommandIssuer issuer,

            @Single
            @Syntax("<MultiInv|WorldInventories|PerWorldInventory>")
            String pluginName) {

        dataImportManager.getImporter(pluginName)
                .onEmpty(() -> issuer.sendError(MVInvi18n.MIGRATE_UNSUPPORTEDPLUGIN, replace("{plugin}").with(pluginName)))
                .peek(dataImporter -> {
                    if (!dataImporter.isEnabled()) {
                        issuer.sendError(MVInvi18n.MIGRATE_PLUGINNOTENABLED, replace("{plugin}").with(pluginName));
                        return;
                    }
                    commandQueueManager.addToQueue(CommandQueuePayload.issuer(issuer)
                            .prompt(Message.of(MVInvi18n.MIGRATE_CONFIRMPROMPT, replace("{plugin}").with(pluginName)))
                            .action(() -> doDataImport(issuer, dataImporter)));
                });
    }

    void doDataImport(MVCommandIssuer issuer, DataImporter dataImporter) {
        if (dataImporter.importData()) {
            issuer.sendInfo(MVInvi18n.MIGRATE_SUCCESS, replace("{plugin}").with(dataImporter.getPluginName()));
        } else {
            issuer.sendError(MVInvi18n.MIGRATE_FAILED, replace("{plugin}").with(dataImporter.getPluginName()));
        }
    }
}
