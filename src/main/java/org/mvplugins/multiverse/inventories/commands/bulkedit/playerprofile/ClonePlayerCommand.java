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
public class ClonePlayerCommand extends BulkEditCommand {

    private final @NotNull CommandQueueManager commandQueueManager;
    private final @NotNull IncludeGroupsWorldsFlag flags;

    @Inject
    ClonePlayerCommand(@NotNull BulkEditCreator bulkEditCreator,
                       @NotNull CommandQueueManager commandQueueManager,
                       @NotNull IncludeGroupsWorldsFlag flags
    ) {
        super(bulkEditCreator);
        this.commandQueueManager = commandQueueManager;
        this.flags = flags;
    }

    @Subcommand("bulkedit playerprofile clone-player")
    @CommandPermission("multiverse.inventories.bulkedit")
    @CommandCompletion("@mvinvplayername @mvinvplayername @mvinvcontainerkeys @mvinvprofiletypes:multiple " +
            "@flags:groupName=" + IncludeGroupsWorldsFlag.NAME)
    @Syntax("<from-player> <to-player> <groups/worlds> [profile-types] [--include-groups-worlds]")
    void onCommand(
            MVCommandIssuer issuer,

            @Syntax("<from-player>")
            GlobalProfileKey fromPlayer,

            @Syntax("<to-player>")
            GlobalProfileKey[] toPlayers,

            @Syntax("<groups/worlds>")
            ContainerKey[] containerKeys,

            @Syntax("[profile-types]")
            ProfileType[] profileTypes,

            @Syntax("[--include-groups-worlds]")
            String[] flagArray
    ) {
        ParsedCommandFlags parsedFlags = flags.parse(flagArray);

        BulkEditAction<?> bulkEditAction = bulkEditCreator.playerProfileClonePlayer(
                fromPlayer,
                new PlayerProfilesPayload(
                        toPlayers,
                        containerKeys,
                        profileTypes,
                        parsedFlags.hasFlag(flags.includeGroupsWorlds)
                )
        );

        outputActionSummary(issuer, bulkEditAction);

        commandQueueManager.addToQueue(CommandQueuePayload.issuer(issuer)
                .prompt(Message.of("Are you sure you want to clone profiles from %s to %s for the selected groups/worlds?"
                        .formatted(fromPlayer.getPlayerName(),
                                toPlayers.length == 1 ? toPlayers[0].getPlayerName() : toPlayers.length + " players")))
                .action(() -> runBulkEditAction(issuer, bulkEditAction)));
    }
}
