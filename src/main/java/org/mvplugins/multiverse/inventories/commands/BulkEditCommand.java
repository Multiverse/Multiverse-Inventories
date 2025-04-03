package org.mvplugins.multiverse.inventories.commands;

import com.dumptruckman.minecraft.util.Logging;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.core.command.MVCommandIssuer;
import org.mvplugins.multiverse.core.command.MVCommandManager;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandAlias;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandPermission;
import org.mvplugins.multiverse.external.acf.commands.annotation.Subcommand;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.inventories.config.InventoriesConfig;
import org.mvplugins.multiverse.inventories.profile.ProfileDataSource;
import org.mvplugins.multiverse.inventories.profile.key.GlobalProfileKey;
import org.mvplugins.multiverse.inventories.profile.key.ProfileKey;
import org.mvplugins.multiverse.inventories.profile.key.ProfileTypes;
import org.mvplugins.multiverse.inventories.profile.key.ContainerType;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@CommandAlias("mvinv")
final class BulkEditCommand extends InventoriesCommand {

    private final ProfileDataSource profileDataSource;
    private final InventoriesConfig inventoriesConfig;

    @Inject
    BulkEditCommand(MVCommandManager commandManager, ProfileDataSource profileDataSource, InventoriesConfig inventoriesConfig) {
        super(commandManager);
        this.profileDataSource = profileDataSource;
        this.inventoriesConfig = inventoriesConfig;
    }

    @Subcommand("bulkedit migrate inventory-serialization nbt")
    @CommandPermission("multiverse.inventories.bulkedit")
    void onBulkEditCommand(MVCommandIssuer issuer) {
        inventoriesConfig.setUseByteSerializationForInventoryData(true);
        inventoriesConfig.save();

        Collection<UUID> globalPlayersList = profileDataSource.listGlobalProfileUUIDs();
        Collection<String> worldContainerNames = profileDataSource.listContainerDataNames(ContainerType.WORLD);
        Collection<String> groupContainerNames = profileDataSource.listContainerDataNames(ContainerType.GROUP);
        Logging.fine(String.join(", ", worldContainerNames));
        CompletableFuture.allOf(globalPlayersList
                .stream()
                .map(playerUUID -> profileDataSource.getGlobalProfile(GlobalProfileKey.create(playerUUID))
                        .thenApply(profile -> {
                            profile.setLoadOnLogin(true);
                            profileDataSource.updateGlobalProfile(profile);
                            return profile;
                        })
                        .thenCompose(profile -> {
                            return CompletableFuture.allOf(worldContainerNames.stream().flatMap(worldName -> {
                                return ProfileTypes.getTypes().stream().map(profileType -> {
                                    return profileDataSource.getPlayerProfile(
                                            ProfileKey.create(ContainerType.WORLD, worldName, profileType, profile.getPlayerUUID(), profile.getLastKnownName())
                                    ).thenCompose(profileDataSource::updatePlayerProfile);
                                });
                            }).toArray(CompletableFuture[]::new)).thenCompose(ignore -> {
                                return CompletableFuture.allOf(groupContainerNames.stream().flatMap(worldName -> {
                                    return ProfileTypes.getTypes().stream().map(profileType -> {
                                        return profileDataSource.getPlayerProfile(
                                                ProfileKey.create(ContainerType.GROUP, worldName, profileType, profile.getPlayerUUID(), profile.getLastKnownName())
                                        ).thenCompose(profileDataSource::updatePlayerProfile);
                                    });
                                }).toArray(CompletableFuture[]::new));
                            });
                        })
                        .exceptionally(throwable -> {
                            issuer.sendMessage("Error updating player " + playerUUID + ": " + throwable.getMessage());
                            return null;
                        }))
                .toArray(CompletableFuture[]::new))
                .thenRun(() -> {
                    issuer.sendMessage("Bulk edit complete.");
                    issuer.sendMessage("Please restart your server to complete the migration.");
                });
    }
}
