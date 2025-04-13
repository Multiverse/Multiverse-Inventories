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
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandPermission;
import org.mvplugins.multiverse.external.acf.commands.annotation.Subcommand;
import org.mvplugins.multiverse.external.acf.commands.annotation.Syntax;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.inventories.profile.ProfileDataSource;
import org.mvplugins.multiverse.inventories.profile.key.GlobalProfileKey;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

@Service
final class ClearCommand {

    private final CommandQueueManager commandQueueManager;
    private final ProfileDataSource profileDataSource;
    private final Flags flags;

    @Inject
    ClearCommand(
            @NotNull CommandQueueManager commandQueueManager,
            @NotNull ProfileDataSource profileDataSource,
            @NotNull Flags flags
    ) {
        this.commandQueueManager = commandQueueManager;
        this.profileDataSource = profileDataSource;
        this.flags = flags;
    }

    @Subcommand("bulkedit globalprofile clear")
    @CommandPermission("multiverse.inventories.bulkedit")
    @Syntax("<players>")
    void onCommand(
            MVCommandIssuer issuer,

            @Syntax("<players>")
            GlobalProfileKey[] globalProfileKeys,

            String[] flagArray
    ) {
        ParsedCommandFlags parsedFlags = flags.parse(flagArray);

        commandQueueManager.addToQueue(CommandQueuePayload.issuer(issuer)
                .prompt(Message.of("Are you sure you want to clear %d profiles?".formatted(globalProfileKeys.length)))
                .action(() -> doClear(issuer, globalProfileKeys, parsedFlags.hasFlag(flags.clearAllPlayerprofiles))));
    }

    private void doClear(MVCommandIssuer issuer, GlobalProfileKey[] globalProfileKeys, boolean clearPlayerProfile) {
        CompletableFuture[] futures = Arrays.stream(globalProfileKeys)
                .map(globalProfileKey -> profileDataSource.deleteGlobalProfile(globalProfileKey, clearPlayerProfile))
                .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(futures)
                .thenRun(() -> issuer.sendMessage("Successfully cleared %d profiles.".formatted(globalProfileKeys.length)));
    }

    @Service
    private static final class Flags extends FlagBuilder {
        @Inject
        private Flags(@NotNull String name, @NotNull CommandFlagsManager flagsManager) {
            super(name, flagsManager);
        }

        private final CommandFlag clearAllPlayerprofiles = flag(CommandFlag.builder("--clear-all-playerprofiles")
                .addAlias("-a")
                .build());
    }
}
