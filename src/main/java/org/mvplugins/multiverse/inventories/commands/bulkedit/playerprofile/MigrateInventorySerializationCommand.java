package org.mvplugins.multiverse.inventories.commands.bulkedit.playerprofile;

import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.core.command.MVCommandIssuer;
import org.mvplugins.multiverse.core.command.queue.CommandQueueManager;
import org.mvplugins.multiverse.core.command.queue.CommandQueuePayload;
import org.mvplugins.multiverse.core.locale.message.Message;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandPermission;
import org.mvplugins.multiverse.external.acf.commands.annotation.Subcommand;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.inventories.commands.InventoriesCommand;
import org.mvplugins.multiverse.inventories.config.InventoriesConfig;
import org.mvplugins.multiverse.inventories.profile.GlobalProfile;
import org.mvplugins.multiverse.inventories.profile.ProfileDataSource;
import org.mvplugins.multiverse.inventories.profile.key.GlobalProfileKey;
import org.mvplugins.multiverse.inventories.profile.key.ProfileKey;
import org.mvplugins.multiverse.inventories.profile.key.ProfileTypes;
import org.mvplugins.multiverse.inventories.profile.key.ContainerType;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

@Service
final class MigrateInventorySerializationCommand extends InventoriesCommand {

    private final CommandQueueManager commandQueueManager;
    private final ProfileDataSource profileDataSource;
    private final InventoriesConfig inventoriesConfig;

    @Inject
    MigrateInventorySerializationCommand(
            @NotNull CommandQueueManager commandQueueManager,
            @NotNull ProfileDataSource profileDataSource,
            @NotNull InventoriesConfig inventoriesConfig
    ) {
        this.commandQueueManager = commandQueueManager;
        this.profileDataSource = profileDataSource;
        this.inventoriesConfig = inventoriesConfig;
    }

    @Subcommand("bulkedit migrate inventory-serialization nbt")
    @CommandPermission("multiverse.inventories.bulkedit")
    void onNbtCommand(MVCommandIssuer issuer) {
        commandQueueManager.addToQueue(CommandQueuePayload.issuer(issuer)
                .prompt(Message.of("Are you sure you want to migrate all player data to NBT?"))
                .action(() -> doMigration(issuer, true)));
    }

    @Subcommand("bulkedit migrate inventory-serialization bukkit")
    @CommandPermission("multiverse.inventories.bulkedit")
    void onBukkitCommand(MVCommandIssuer issuer) {
        commandQueueManager.addToQueue(CommandQueuePayload.issuer(issuer)
                .prompt(Message.of("Are you sure you want to migrate all player data to old Bukkit serialization?"))
                .action(() -> doMigration(issuer, false)));
    }

    private void doMigration(MVCommandIssuer issuer, boolean useByteSerialization) {
        inventoriesConfig.setUseByteSerializationForInventoryData(useByteSerialization);
        inventoriesConfig.save();

        long startTime = System.nanoTime();
        AtomicLong profileCounter = new AtomicLong(0);
        CompletableFuture.allOf(profileDataSource.listGlobalProfileUUIDs()
                        .stream()
                        .map(playerUUID -> profileDataSource.getGlobalProfile(GlobalProfileKey.of(playerUUID, ""))
                                .thenCompose(profile -> run(profile, profileCounter))
                                .exceptionally(throwable -> {
                                    issuer.sendMessage("Error updating player " + playerUUID + ": " + throwable.getMessage());
                                    return null;
                                }))
                        .toArray(CompletableFuture[]::new))
                .thenRun(() -> {
                    long timeDuration = (System.nanoTime() - startTime) / 1000000;
                    issuer.sendMessage("Updated " + profileCounter.get() + " player profiles.");
                    issuer.sendMessage("Bulk edit completed in " + timeDuration + " ms.");
                });
    }

    private CompletableFuture<Void> run(GlobalProfile profile, AtomicLong profileCounter) {
        return CompletableFuture.allOf(Arrays.stream(ContainerType.values())
                .flatMap(containerType -> profileDataSource.listContainerDataNames(containerType).stream()
                        .flatMap(dataName -> ProfileTypes.getTypes().stream()
                                .map(profileType -> profileDataSource.getPlayerProfile(ProfileKey.of(
                                        containerType,
                                        dataName,
                                        profileType,
                                        profile.getPlayerUUID(),
                                        profile.getLastKnownName()
                                )).thenCompose(playerProfile -> {
                                    if (playerProfile.getData().isEmpty()) {
                                        return CompletableFuture.completedFuture(null);
                                    }
                                    profileCounter.incrementAndGet();
                                    return profileDataSource.updatePlayerProfile(playerProfile);
                                }))))
                .toArray(CompletableFuture[]::new));
    }
}
