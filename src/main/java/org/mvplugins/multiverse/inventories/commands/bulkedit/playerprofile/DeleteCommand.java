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
import org.mvplugins.multiverse.inventories.commands.InventoriesCommand;
import org.mvplugins.multiverse.inventories.profile.ProfileDataSource;
import org.mvplugins.multiverse.inventories.profile.bulkedit.ProfilesAggregator;
import org.mvplugins.multiverse.inventories.profile.group.WorldGroupManager;
import org.mvplugins.multiverse.inventories.profile.key.ContainerKey;
import org.mvplugins.multiverse.inventories.profile.key.GlobalProfileKey;
import org.mvplugins.multiverse.inventories.profile.key.ProfileKey;
import org.mvplugins.multiverse.inventories.profile.key.ProfileType;
import org.mvplugins.multiverse.inventories.share.Sharable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
final class DeleteCommand extends InventoriesCommand {

    private final CommandQueueManager commandQueueManager;
    private final ProfileDataSource profileDataSource;
    private final ProfilesAggregator profilesAggregator;
    private final IncludeGroupsWorldsFlag flags;

    @Inject
    DeleteCommand(
            @NotNull CommandQueueManager commandQueueManager,
            @NotNull ProfileDataSource profileDataSource,
            @NotNull ProfilesAggregator profilesAggregator,
            @NotNull IncludeGroupsWorldsFlag flags
    ) {
        this.commandQueueManager = commandQueueManager;
        this.profileDataSource = profileDataSource;
        this.profilesAggregator = profilesAggregator;
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

        issuer.sendMessage("Players: " + StringFormatter.join(List.of(globalProfileKeys), ", "));
        issuer.sendMessage("Containers: " + StringFormatter.join(List.of(containerKeys), ", "));
        issuer.sendMessage("Profile Types: " + StringFormatter.join(List.of(profileTypes), ", "));

        List<ProfileKey> playerProfileKeys = profilesAggregator.getPlayerProfileKeys(
                globalProfileKeys, containerKeys, profileTypes, parsedFlags.hasFlag(flags.includeGroupsWorlds));

        commandQueueManager.addToQueue(CommandQueuePayload.issuer(issuer)
                .prompt(Message.of("Are you sure you want to delete %s?".formatted(sharable.getNames()[0])))
                .action(() -> runDelete(issuer, sharable, playerProfileKeys)));
    }

    private void runDelete(MVCommandIssuer issuer, Sharable sharable, List<ProfileKey> playerProfileKeys) {
        //TODO: Check lastWorld and online
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (ProfileKey playerProfileKey : playerProfileKeys) {
            profileDataSource.getPlayerProfile(playerProfileKey)
                    .thenCompose(playerProfile -> {
                        playerProfile.set(sharable, null);
                        return profileDataSource.updatePlayerProfile(playerProfile);
                    });
        }
        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).thenRun(() -> {
            issuer.sendMessage("Successfully deleted %s from %d profiles.".formatted(sharable.getNames()[0], playerProfileKeys.size()));
        });
    }
}
