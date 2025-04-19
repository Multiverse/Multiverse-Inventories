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
import org.mvplugins.multiverse.inventories.profile.key.ContainerKey;
import org.mvplugins.multiverse.inventories.profile.key.GlobalProfileKey;
import org.mvplugins.multiverse.inventories.profile.key.ProfileFileKey;
import org.mvplugins.multiverse.inventories.profile.key.ProfileKey;
import org.mvplugins.multiverse.inventories.profile.key.ProfileType;
import org.mvplugins.multiverse.inventories.profile.key.ProfileTypes;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
final class ClearCommand extends InventoriesCommand {

    private final CommandQueueManager commandQueueManager;
    private final ProfileDataSource profileDataSource;
    private final ProfilesAggregator profilesAggregator;
    private final IncludeGroupsWorldsFlag flags;

    @Inject
    ClearCommand(
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

    @Subcommand("bulkedit playerprofile clear")
    @CommandPermission("multiverse.inventories.bulkedit")
    @CommandCompletion("@mvinvplayernames @empty @mvinvprofiletypes @flags:groupName=" + IncludeGroupsWorldsFlag.NAME)
    @Syntax("<players> <groups|worlds> [profile-type] [--include-groups-worlds]")
    void onCommand(
            MVCommandIssuer issuer,
            GlobalProfileKey[] globalProfileKeys,
            ContainerKey[] containerKeys,
            ProfileType[] profileTypes,
            String[] flagArray
    ) {
        ParsedCommandFlags parsedFlags = flags.parse(flagArray);

        issuer.sendMessage("Players: " + StringFormatter.join(List.of(globalProfileKeys), ", "));
        issuer.sendMessage("Containers: " + StringFormatter.join(List.of(containerKeys), ", "));
        issuer.sendMessage("Profile Types: " + StringFormatter.join(List.of(profileTypes), ", "));

        commandQueueManager.addToQueue(CommandQueuePayload.issuer(issuer)
                .prompt(Message.of("Are you sure you want to delete the above selected data?"))
                .action(() -> runDelete(issuer, globalProfileKeys, containerKeys, profileTypes, parsedFlags)));
    }

    private void runDelete(MVCommandIssuer issuer, GlobalProfileKey[] globalProfileKeys, ContainerKey[] containerKeys, ProfileType[] profileTypes, ParsedCommandFlags parsedFlags) {
        //TODO: Check lastWorld and online
        if (ProfileTypes.isAll(profileTypes)) {
            doFileDelete(issuer, globalProfileKeys, containerKeys, parsedFlags.hasFlag(flags.includeGroupsWorlds));
        } else {
            doProfileDelete(issuer, globalProfileKeys, containerKeys, profileTypes, parsedFlags.hasFlag(flags.includeGroupsWorlds));
        }
    }

    private void doFileDelete(MVCommandIssuer issuer, GlobalProfileKey[] globalProfileKeys, ContainerKey[] containerKeys, boolean includeGroupsWorlds) {
        List<ProfileFileKey> profileFileKeys = profilesAggregator.getProfileFileKeys(globalProfileKeys, containerKeys, includeGroupsWorlds);
        CompletableFuture.allOf(profileFileKeys.stream()
                        .map(profileDataSource::deletePlayerFile)
                        .toArray(CompletableFuture[]::new))
                .thenRun(() -> issuer.sendMessage("Successfully deleted %d profiles.".formatted(profileFileKeys.size())));
    }

    private void doProfileDelete(MVCommandIssuer issuer, GlobalProfileKey[] globalProfileKeys, ContainerKey[] containerKeys, ProfileType[] profileTypes, boolean includeGroupsWorlds) {
        List<ProfileKey> playerProfileKeys = profilesAggregator.getPlayerProfileKeys(globalProfileKeys, containerKeys, profileTypes, includeGroupsWorlds);
        CompletableFuture.allOf(playerProfileKeys.stream()
                        .map(profileDataSource::deletePlayerProfile)
                        .toArray(CompletableFuture[]::new))
                .thenRun(() -> issuer.sendMessage("Successfully deleted %d profiles.".formatted(playerProfileKeys.size())));
    }
}
