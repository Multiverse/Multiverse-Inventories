package org.mvplugins.multiverse.inventories.commands.bulkedit.globalprofile;

import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.core.command.MVCommandIssuer;
import org.mvplugins.multiverse.core.command.flag.CommandFlag;
import org.mvplugins.multiverse.core.command.flag.CommandFlagsManager;
import org.mvplugins.multiverse.core.command.flag.FlagBuilder;
import org.mvplugins.multiverse.core.command.flag.ParsedCommandFlags;
import org.mvplugins.multiverse.core.command.queue.CommandQueueManager;
import org.mvplugins.multiverse.core.command.queue.CommandQueuePayload;
import org.mvplugins.multiverse.core.locale.message.Message;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandCompletion;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandPermission;
import org.mvplugins.multiverse.external.acf.commands.annotation.Subcommand;
import org.mvplugins.multiverse.external.acf.commands.annotation.Syntax;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.inventories.commands.InventoriesCommand;
import org.mvplugins.multiverse.inventories.commands.bulkedit.BulkEditCommand;
import org.mvplugins.multiverse.inventories.profile.ProfileDataSource;
import org.mvplugins.multiverse.inventories.profile.bulkedit.BulkEditAction;
import org.mvplugins.multiverse.inventories.profile.bulkedit.BulkEditCreator;
import org.mvplugins.multiverse.inventories.profile.key.GlobalProfileKey;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

@Service
final class ClearCommand extends BulkEditCommand {

    private final CommandQueueManager commandQueueManager;
    private final Flags flags;

    @Inject
    ClearCommand(
            @NotNull BulkEditCreator bulkEditCreator,
            @NotNull CommandQueueManager commandQueueManager,
            @NotNull Flags flags
    ) {
        super(bulkEditCreator);
        this.commandQueueManager = commandQueueManager;
        this.flags = flags;
    }

    @Subcommand("bulkedit globalprofile clear")
    @CommandPermission("multiverse.inventories.bulkedit")
    @CommandCompletion("@mvinvplayernames @flags:groupName=" + Flags.NAME)
    @Syntax("<players> [--clear-all-player-profiles]")
    void onCommand(
            MVCommandIssuer issuer,

            @Syntax("<players>")
            GlobalProfileKey[] globalProfileKeys,

            @Syntax("[--clear-all-playerprofiles]")
            String[] flagArray
    ) {
        ParsedCommandFlags parsedFlags = flags.parse(flagArray);

        BulkEditAction<?> bulkEditAction = bulkEditCreator.globalProfileClear(
                globalProfileKeys,
                parsedFlags.hasFlag(flags.clearAllPlayerProfiles)
        );

        outputActionSummary(issuer, bulkEditAction);

        commandQueueManager.addToQueue(CommandQueuePayload.issuer(issuer)
                .prompt(Message.of("Are you sure you want to clear the selected global profiles?"))
                .action(() -> runBulkEditAction(issuer, bulkEditAction)));
    }

    @Service
    private static final class Flags extends FlagBuilder {
        private static final String NAME = "mvinvbulkeditglobalprofileclear";

        @Inject
        private Flags(@NotNull CommandFlagsManager flagsManager) {
            super(NAME, flagsManager);
        }

        private final CommandFlag clearAllPlayerProfiles = flag(CommandFlag.builder("--clear-all-player-profiles")
                .addAlias("-a")
                .build());
    }
}
