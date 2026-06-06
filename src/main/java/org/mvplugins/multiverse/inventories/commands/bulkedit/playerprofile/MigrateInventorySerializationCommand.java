package org.mvplugins.multiverse.inventories.commands.bulkedit.playerprofile;

import com.google.common.io.Files;
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
import org.mvplugins.multiverse.inventories.profile.ProfileFilesLocator;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

@Service
final class MigrateInventorySerializationCommand extends InventoriesCommand {

    private final CommandQueueManager commandQueueManager;
    private final ProfileDataSource profileDataSource;
    private final InventoriesConfig inventoriesConfig;
    private final ProfileFilesLocator profileFilesLocator;

    @Inject
    MigrateInventorySerializationCommand(
            @NotNull CommandQueueManager commandQueueManager,
            @NotNull ProfileDataSource profileDataSource,
            @NotNull InventoriesConfig inventoriesConfig,
            @NotNull ProfileFilesLocator profileFilesLocator
    ) {
        this.commandQueueManager = commandQueueManager;
        this.profileDataSource = profileDataSource;
        this.inventoriesConfig = inventoriesConfig;
        this.profileFilesLocator = profileFilesLocator;
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

        // Scan global files
        Set<String> fileNamesSet = new HashSet<>();
        profileFilesLocator.listGlobalFiles().forEach(file ->
                fileNamesSet.add(Files.getNameWithoutExtension(file.getName())));

        // Scan world and group files
        for (ContainerType type : ContainerType.values()) {
            for (File folder : profileFilesLocator.listProfileContainerFolders(type)) {
                profileFilesLocator.listPlayerProfileFiles(type, folder.getName()).forEach(file ->
                        fileNamesSet.add(Files.getNameWithoutExtension(file.getName())));
            }
        }

        List<String> fileNames = new ArrayList<>(fileNamesSet);
        issuer.sendMessage("Found " + fileNames.size() + " unique players to migrate.");

        CompletableFuture<Void> future = CompletableFuture.completedFuture(null);
        for (int i = 0; i < fileNames.size(); i++) {
            final int index = i;
            final String fileName = fileNames.get(i);

            future = future.thenCompose(v -> {
                UUID playerUUID;
                try {
                    playerUUID = UUID.fromString(fileName);
                } catch (IllegalArgumentException e) {
                    playerUUID = UUID.nameUUIDFromBytes(("OfflinePlayer:" + fileName).getBytes(StandardCharsets.UTF_8));
                }

                if (index % 100 == 0) {
                    issuer.sendMessage("Processed " + index + " players...");
                }

                return profileDataSource.getGlobalProfile(GlobalProfileKey.of(playerUUID, fileName))
                        .thenCompose(profile -> run(profile, profileCounter))
                        .exceptionally(throwable -> {
                            issuer.sendMessage("Error updating player " + fileName + ": " + throwable.getMessage());
                            return null;
                        });
            });
        }

        future.thenRun(() -> {
            long timeDuration = (System.nanoTime() - startTime) / 1000000;
            issuer.sendMessage("Updated " + profileCounter.get() + " player profiles.");
            issuer.sendMessage("Bulk edit completed in " + timeDuration + " ms.");
        });
    }

    private CompletableFuture<Void> run(GlobalProfile profile, AtomicLong profileCounter) {
        String fileName = profile.getLastKnownName();
        if (fileName == null || fileName.isEmpty()) {
            fileName = profile.getPlayerUUID().toString();
        }
        final String finalFileName = fileName;

        return CompletableFuture.allOf(Arrays.stream(ContainerType.values())
                .flatMap(containerType -> profileDataSource.listContainerDataNames(containerType).stream()
                        .flatMap(dataName -> ProfileTypes.getTypes().stream()
                                .map(profileType -> profileDataSource.getPlayerProfile(ProfileKey.of(
                                        containerType,
                                        dataName,
                                        profileType,
                                        profile.getPlayerUUID(),
                                        finalFileName
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
