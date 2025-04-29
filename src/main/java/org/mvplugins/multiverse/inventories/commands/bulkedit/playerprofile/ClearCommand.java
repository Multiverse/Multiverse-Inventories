package org.mvplugins.multiverse.inventories.commands.bulkedit.playerprofile;

import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.core.command.MVCommandIssuer;
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
import org.mvplugins.multiverse.inventories.commands.bulkedit.BulkEditCommand;
import org.mvplugins.multiverse.inventories.profile.bulkedit.BulkEditAction;
import org.mvplugins.multiverse.inventories.profile.bulkedit.BulkEditCreator;
import org.mvplugins.multiverse.inventories.profile.bulkedit.PlayerProfilesPayload;
import org.mvplugins.multiverse.inventories.profile.key.ContainerKey;
import org.mvplugins.multiverse.inventories.profile.key.GlobalProfileKey;
import org.mvplugins.multiverse.inventories.profile.key.ProfileType;

@Service
final class ClearCommand extends BulkEditCommand {

    private final CommandQueueManager commandQueueManager;
    private final IncludeGroupsWorldsFlag flags;

    @Inject
    ClearCommand(
            @NotNull BulkEditCreator bulkEditCreator,
            @NotNull CommandQueueManager commandQueueManager,
            @NotNull IncludeGroupsWorldsFlag flags
    ) {
        super(bulkEditCreator);
        this.commandQueueManager = commandQueueManager;
        this.flags = flags;
    }

    @Subcommand("bulkedit playerprofile clear")
    @CommandPermission("multiverse.inventories.bulkedit")
    @CommandCompletion("@mvinvplayernames @empty @mvinvprofiletypes:multiple @flags:groupName=" + IncludeGroupsWorldsFlag.NAME)
    @Syntax("<players> <groups|worlds> [profile-type] [--include-groups-worlds]")
    void onCommand(
            MVCommandIssuer issuer,
            GlobalProfileKey[] globalProfileKeys,
            ContainerKey[] containerKeys,
            ProfileType[] profileTypes,
            String[] flagArray
    ) {
        ParsedCommandFlags parsedFlags = flags.parse(flagArray);

        BulkEditAction<?> bulkEditAction = bulkEditCreator.playerProfileClear(
                new PlayerProfilesPayload(
                        globalProfileKeys,
                        containerKeys,
                        profileTypes,
                        parsedFlags.hasFlag(flags.includeGroupsWorlds)
                )
        );

        outputActionSummary(issuer, bulkEditAction);

        commandQueueManager.addToQueue(CommandQueuePayload.issuer(issuer)
                .prompt(Message.of("Are you sure you want to clear the selected profiles?"))
                .action(() -> runBulkEditAction(issuer, bulkEditAction)));
    }
}
