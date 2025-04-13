package org.mvplugins.multiverse.inventories.commands.bulkedit.globalprofile;

import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.core.command.MVCommandIssuer;
import org.mvplugins.multiverse.core.command.queue.CommandQueueManager;
import org.mvplugins.multiverse.core.command.queue.CommandQueuePayload;
import org.mvplugins.multiverse.core.config.handle.PropertyModifyAction;
import org.mvplugins.multiverse.core.locale.message.Message;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandPermission;
import org.mvplugins.multiverse.external.acf.commands.annotation.Subcommand;
import org.mvplugins.multiverse.external.acf.commands.annotation.Syntax;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.inventories.commands.InventoriesCommand;
import org.mvplugins.multiverse.inventories.profile.ProfileDataSource;
import org.mvplugins.multiverse.inventories.profile.key.GlobalProfileKey;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

@Service
final class ModifyCommand extends InventoriesCommand {

    private final CommandQueueManager commandQueueManager;
    private final ProfileDataSource profileDataSource;

    @Inject
    ModifyCommand(CommandQueueManager commandQueueManager, ProfileDataSource profileDataSource) {
        this.commandQueueManager = commandQueueManager;
        this.profileDataSource = profileDataSource;
    }

    @Subcommand("bulkedit globalprofile modify")
    @CommandPermission("multiverse.inventories.bulkedit")
    @Syntax("<property> <value> <players>")
    void onCommand(
            MVCommandIssuer issuer,

            @Syntax("<property>")
            String property,

            @Syntax("<value>")
            String value,

            @Syntax("<players>")
            GlobalProfileKey[] profileKeys
    ) {
        commandQueueManager.addToQueue(CommandQueuePayload.issuer(issuer)
                .prompt(Message.of("Are you sure you want to modify %s to %s for %d players?".formatted(property, value, profileKeys.length)))
                .action(() -> doModify(issuer, property, value, profileKeys)));
    }

    private void doModify(MVCommandIssuer issuer, String property, String value, GlobalProfileKey[] profileKeys) {
        AtomicInteger counter = new AtomicInteger(0);
        CompletableFuture[] futures = Arrays.stream(profileKeys)
                .map(profileKey ->
                        profileDataSource.modifyGlobalProfile(profileKey, globalProfile ->
                                globalProfile.getStringPropertyHandle()
                                        .modifyPropertyString(property, value, PropertyModifyAction.SET)
                                        .onSuccess(ignore -> counter.incrementAndGet())
                                        .onFailure(throwable -> issuer.sendError("Failed to modify %s for %s. %s".formatted(property, profileKey, throwable.getMessage())))))
                .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(futures)
                .thenRun(() -> issuer.sendMessage("Successfully modified %s to %s for %d players.".formatted(property, value, counter.get())));
    }
}
