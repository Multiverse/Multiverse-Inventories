package org.mvplugins.multiverse.inventories.commands.bulkedit;

import org.jetbrains.annotations.ApiStatus;
import org.jvnet.hk2.annotations.Contract;
import org.mvplugins.multiverse.core.command.MVCommandIssuer;
import org.mvplugins.multiverse.core.utils.StringFormatter;
import org.mvplugins.multiverse.external.acf.commands.annotation.Subcommand;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.inventories.commands.InventoriesCommand;
import org.mvplugins.multiverse.inventories.profile.bulkedit.BulkEditAction;
import org.mvplugins.multiverse.inventories.profile.bulkedit.BulkEditCreator;
import org.mvplugins.multiverse.inventories.profile.bulkedit.BulkEditResult;
import org.mvplugins.multiverse.inventories.util.MVInvi18n;

import static org.mvplugins.multiverse.core.locale.message.MessageReplacement.replace;

@Contract
@ApiStatus.Internal
public abstract class BulkEditCommand extends InventoriesCommand {

    protected final BulkEditCreator bulkEditCreator;

    @Inject
    protected BulkEditCommand(BulkEditCreator bulkEditCreator) {
        this.bulkEditCreator = bulkEditCreator;
    }

    protected void outputActionSummary(MVCommandIssuer issuer, BulkEditAction<?> bulkEditAction) {
        issuer.sendMessage(MVInvi18n.BULKEDIT_SUMMARY);
        bulkEditAction.getActionSummary().forEach((key, value) -> {
            Object valueSummary = value.size() > 10
                    ? value.size()
                    : StringFormatter.join(value, ", ");
            issuer.sendMessage(MVInvi18n.BULKEDIT_SUMMARYENTRY,
                    replace("{key}").with(key),
                    replace("{value}").with(valueSummary));
        });

    }

    protected void runBulkEditAction(MVCommandIssuer issuer, BulkEditAction<?> bulkEditAction) {
        issuer.sendMessage(MVInvi18n.BULKEDIT_STARTING);
        bulkEditAction.execute()
                .thenAccept(result -> outputResult(issuer, result));
    }

    protected void outputResult(MVCommandIssuer issuer, BulkEditResult bulkEditResult) {
        issuer.sendMessage(MVInvi18n.BULKEDIT_SUCCESSCOUNT,
                replace("{count}").with(bulkEditResult.getSuccessCount()));
        if (bulkEditResult.getFailureCount() > 0) {
            issuer.sendError(MVInvi18n.BULKEDIT_FAILURECOUNT,
                    replace("{count}").with(bulkEditResult.getFailureCount()));
        }
        issuer.sendMessage(MVInvi18n.BULKEDIT_COMPLETEDTIME,
                replace("{milliseconds}").with("%.4f".formatted(bulkEditResult.getTimeTaken())));
    }
}
