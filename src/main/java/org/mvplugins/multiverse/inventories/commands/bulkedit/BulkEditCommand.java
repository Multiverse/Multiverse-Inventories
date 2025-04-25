package org.mvplugins.multiverse.inventories.commands.bulkedit;

import org.jetbrains.annotations.ApiStatus;
import org.jvnet.hk2.annotations.Contract;
import org.mvplugins.multiverse.core.command.MVCommandIssuer;
import org.mvplugins.multiverse.core.utils.StringFormatter;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.inventories.commands.InventoriesCommand;
import org.mvplugins.multiverse.inventories.profile.bulkedit.BulkEditAction;
import org.mvplugins.multiverse.inventories.profile.bulkedit.BulkEditCreator;
import org.mvplugins.multiverse.inventories.profile.bulkedit.BulkEditResult;

@Contract
@ApiStatus.Internal
public abstract class BulkEditCommand extends InventoriesCommand {

    protected final BulkEditCreator bulkEditCreator;

    @Inject
    protected BulkEditCommand(BulkEditCreator bulkEditCreator) {
        this.bulkEditCreator = bulkEditCreator;
    }

    protected void outputActionSummary(MVCommandIssuer issuer, BulkEditAction<?> bulkEditAction) {
        issuer.sendMessage("Summary of affected profiles:");
        bulkEditAction.getActionSummary().forEach((key, value) ->
                issuer.sendMessage("  %s: %s".formatted(key, value.size() > 10
                        ? value.size()
                        : StringFormatter.join(value, ", "))));

    }

    protected void runBulkEditAction(MVCommandIssuer issuer, BulkEditAction<?> bulkEditAction) {
        issuer.sendMessage("Starting bulk edit action...");
        bulkEditAction.execute()
                .thenAccept(result -> outputResult(issuer, result));
    }

    protected void outputResult(MVCommandIssuer issuer, BulkEditResult bulkEditResult) {
        issuer.sendMessage("Successfully processed %d profiles!".formatted(bulkEditResult.getSuccessCount()));
        if (bulkEditResult.getFailureCount() > 0) {
            issuer.sendError("Failed to process %d profiles! See log for details.".formatted(bulkEditResult.getFailureCount()));
        }
        issuer.sendMessage("Bulk edit action completed in %.4f ms.".formatted(bulkEditResult.getTimeTaken()));
    }
}
