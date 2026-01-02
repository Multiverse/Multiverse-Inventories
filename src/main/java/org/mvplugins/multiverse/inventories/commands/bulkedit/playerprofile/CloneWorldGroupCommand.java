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
import org.mvplugins.multiverse.external.vavr.collection.Array;
import org.mvplugins.multiverse.inventories.commands.bulkedit.BulkEditCommand;
import org.mvplugins.multiverse.inventories.profile.bulkedit.BulkEditAction;
import org.mvplugins.multiverse.inventories.profile.bulkedit.BulkEditCreator;
import org.mvplugins.multiverse.inventories.profile.bulkedit.PlayerProfilesPayload;
import org.mvplugins.multiverse.inventories.profile.key.ContainerKey;
import org.mvplugins.multiverse.inventories.profile.key.GlobalProfileKey;
import org.mvplugins.multiverse.inventories.profile.key.ProfileType;

import java.util.Objects;

@Service
public class CloneWorldGroupCommand extends BulkEditCommand {

    private final CommandQueueManager commandQueueManager;
    private final IncludeGroupsWorldsFlag flags;

    @Inject
    CloneWorldGroupCommand(@NotNull BulkEditCreator bulkEditCreator,
                           @NotNull CommandQueueManager commandQueueManager,
                           @NotNull IncludeGroupsWorldsFlag flags
    ) {
        super(bulkEditCreator);
        this.commandQueueManager = commandQueueManager;
        this.flags = flags;
    }

    @Subcommand("bulkedit playerprofile clone-world-group")
    @CommandPermission("multiverse.inventories.bulkedit")
    @CommandCompletion("@mvinvplayernames @mvinvcontainerkey @mvinvcontainerkeys @mvinvprofiletypes:multiple " +
            "@flags:groupName=" + IncludeGroupsWorldsFlag.NAME)
    @Syntax("<players> <from-group/world> <to-groups/worlds> [profile-type]")
    void onCommand(
            MVCommandIssuer issuer,

            @Syntax("<players>")
            GlobalProfileKey[] globalProfileKeys,

            @Syntax("<from-group/world>")
            ContainerKey fromContainerKey,

            @Syntax("<to-groups/worlds>")
            ContainerKey[] toContainerKeys,

            @Syntax("[profile-types]")
            ProfileType[] profileTypes,

            @Syntax("[--include-groups-worlds]")
            String[] flagArray
    ) {
        if (Array.of(toContainerKeys)
                .find(toKey -> Objects.equals(fromContainerKey, toKey))
                .peek(toKey -> issuer.sendError("Cannot copy profiles to the same "
                        + toKey.getContainerType() + ": " + toKey.getDataName()))
                .isDefined()) {
            return;
        }

        ParsedCommandFlags parsedFlags = flags.parse(flagArray);

        BulkEditAction<?> bulkEditAction = bulkEditCreator.playerProfileCloneWorldGroup(
                fromContainerKey,
                new PlayerProfilesPayload(
                        globalProfileKeys,
                        toContainerKeys,
                        profileTypes,
                        parsedFlags.hasFlag(flags.includeGroupsWorlds)
                )
        );

        outputActionSummary(issuer, bulkEditAction);

        commandQueueManager.addToQueue(CommandQueuePayload.issuer(issuer)
                .prompt(Message.of("Are you sure you want to clone profiles from %s %s to the selected groups/worlds?"
                        .formatted(fromContainerKey.getContainerType(), fromContainerKey.getDataName())))
                .action(() -> runBulkEditAction(issuer, bulkEditAction)));
    }
}
