package org.mvplugins.multiverse.inventories.commands.bulkedit.playerprofile;

import com.dumptruckman.minecraft.util.Logging;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.core.command.MVCommandIssuer;
import org.mvplugins.multiverse.core.command.MVCommandManager;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandAlias;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandPermission;
import org.mvplugins.multiverse.external.acf.commands.annotation.Subcommand;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.inventories.commands.InventoriesCommand;
import org.mvplugins.multiverse.inventories.config.InventoriesConfig;
import org.mvplugins.multiverse.inventories.profile.GlobalProfile;
import org.mvplugins.multiverse.inventories.profile.ProfileDataSource;
import org.mvplugins.multiverse.inventories.profile.container.ProfileContainerStoreProvider;
import org.mvplugins.multiverse.inventories.profile.key.GlobalProfileKey;
import org.mvplugins.multiverse.inventories.profile.key.ProfileKey;
import org.mvplugins.multiverse.inventories.profile.key.ProfileTypes;
import org.mvplugins.multiverse.inventories.profile.key.ContainerType;

import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@CommandAlias("mvinv")
final class MigrateInventorySerializationCommand extends InventoriesCommand {

    private final ProfileDataSource profileDataSource;
    private final InventoriesConfig inventoriesConfig;

    @Inject
    MigrateInventorySerializationCommand(
            MVCommandManager commandManager,
            ProfileDataSource profileDataSource,
            InventoriesConfig inventoriesConfig
    ) {
        super(commandManager);
        this.profileDataSource = profileDataSource;
        this.inventoriesConfig = inventoriesConfig;
    }

    @Subcommand("bulkedit migrate inventory-serialization nbt")
    @CommandPermission("multiverse.inventories.bulkedit")
    void onNbtCommand(MVCommandIssuer issuer) {
        doMigration(issuer, true);
    }

    @Subcommand("bulkedit migrate inventory-serialization bukkit")
    @CommandPermission("multiverse.inventories.bulkedit")
    void onBukkitCommand(MVCommandIssuer issuer) {
        doMigration(issuer, false);
    }

    private void doMigration(MVCommandIssuer issuer, boolean useByteSerialization) {
        inventoriesConfig.setUseByteSerializationForInventoryData(useByteSerialization);
        inventoriesConfig.save();

        long startTime = System.nanoTime();
        CompletableFuture.allOf(profileDataSource.listGlobalProfileUUIDs()
                        .stream()
                        .map(playerUUID -> profileDataSource.getGlobalProfile(GlobalProfileKey.create(playerUUID))
                                .thenCompose(profile -> run(profile))
                                .exceptionally(throwable -> {
                                    issuer.sendMessage("Error updating player " + playerUUID + ": " + throwable.getMessage());
                                    return null;
                                }))
                        .toArray(CompletableFuture[]::new))
                .thenRun(() -> {
                    long timeDuration = (System.nanoTime() - startTime) / 1000000;
                    issuer.sendMessage("Bulk edit completed in " + timeDuration + " ms.");
                    issuer.sendMessage("Please restart your server to complete the migration.");
                });
    }

    private CompletableFuture<Void> run(GlobalProfile profile) {
        return CompletableFuture.allOf(Arrays.stream(ContainerType.values())
                .flatMap(containerType -> profileDataSource.listContainerDataNames(containerType).stream()
                        .flatMap(dataName -> ProfileTypes.getTypes().stream()
                                .map(profileType -> profileDataSource.getPlayerProfile(ProfileKey.create(
                                        containerType,
                                        dataName,
                                        profileType,
                                        profile.getPlayerUUID(),
                                        profile.getLastKnownName()
                                )).thenCompose(profileDataSource::updatePlayerProfile))))
                .toArray(CompletableFuture[]::new));
    }
}
