package org.mvplugins.multiverse.inventories.commands.bulkedit.playerprofile;

import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.core.command.MVCommandIssuer;
import org.mvplugins.multiverse.core.command.flag.ParsedCommandFlags;
import org.mvplugins.multiverse.core.command.queue.CommandQueueManager;
import org.mvplugins.multiverse.core.command.queue.CommandQueuePayload;
import org.mvplugins.multiverse.core.locale.message.Message;
import org.mvplugins.multiverse.core.utils.StringFormatter;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandCompletion;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandPermission;
import org.mvplugins.multiverse.external.acf.commands.annotation.Subcommand;
import org.mvplugins.multiverse.external.acf.commands.annotation.Syntax;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.inventories.MultiverseInventories;
import org.mvplugins.multiverse.inventories.commands.bulkedit.BulkEditCommand;
import org.mvplugins.multiverse.inventories.profile.bulkedit.BulkProfilesPayload;
import org.mvplugins.multiverse.inventories.profile.bulkedit.action.PlayerProfileDeleteAction;
import org.mvplugins.multiverse.inventories.profile.key.ContainerKey;
import org.mvplugins.multiverse.inventories.profile.key.GlobalProfileKey;
import org.mvplugins.multiverse.inventories.profile.key.ProfileType;
import org.mvplugins.multiverse.inventories.share.Sharable;

@Service
final class DeleteCommand extends BulkEditCommand {

    private final MultiverseInventories inventories;
    private final CommandQueueManager commandQueueManager;
    private final IncludeGroupsWorldsFlag flags;

    @Inject
    DeleteCommand(
            @NotNull MultiverseInventories inventories,
            @NotNull CommandQueueManager commandQueueManager,
            @NotNull IncludeGroupsWorldsFlag flags
    ) {
        this.inventories = inventories;
        this.commandQueueManager = commandQueueManager;
        this.flags = flags;
    }

    @Subcommand("bulkedit playerprofile delete")
    @CommandPermission("multiverse.inventories.bulkedit")
    @CommandCompletion("@shares @mvinvplayernames @empty @mvinvprofiletypes @flags:groupName=" + IncludeGroupsWorldsFlag.NAME)
    @Syntax("<sharable> <players> <groups|worlds> [profile-type] [--include-groups-worlds]")
    void onCommand(
            MVCommandIssuer issuer,
            Sharable sharable,
            GlobalProfileKey[] globalProfileKeys,
            ContainerKey[] containerKeys,
            ProfileType[] profileTypes,
            String[] flagArray
    ) {
        ParsedCommandFlags parsedFlags = flags.parse(flagArray);

        PlayerProfileDeleteAction bulkEditAction = new PlayerProfileDeleteAction(
                inventories,
                sharable,
                new BulkProfilesPayload(
                        globalProfileKeys,
                        containerKeys,
                        profileTypes,
                        parsedFlags.hasFlag(flags.includeGroupsWorlds)
                )
        );

        issuer.sendMessage("Summary of affected profiles:");
        bulkEditAction.getActionSummary().forEach((key, value) ->
                issuer.sendMessage("  %s: %s".formatted(key, value.size() > 10 ? value.size() : StringFormatter.join(value, ", "))));

        commandQueueManager.addToQueue(CommandQueuePayload.issuer(issuer)
                .prompt(Message.of("Are you sure you want to delete %s from the selected profiles?".formatted(sharable.getNames()[0])))
                .action(() -> runBulkEditAction(issuer, bulkEditAction)));
    }
}
