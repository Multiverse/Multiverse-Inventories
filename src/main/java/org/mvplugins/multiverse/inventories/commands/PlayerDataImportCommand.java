package org.mvplugins.multiverse.inventories.commands;

import com.google.common.io.Files;
import org.bukkit.World;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.core.command.MVCommandIssuer;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandCompletion;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandPermission;
import org.mvplugins.multiverse.external.acf.commands.annotation.Description;
import org.mvplugins.multiverse.external.acf.commands.annotation.Subcommand;
import org.mvplugins.multiverse.external.acf.commands.annotation.Syntax;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.vavr.control.Try;
import org.mvplugins.multiverse.inventories.profile.ProfileDataSource;
import org.mvplugins.multiverse.inventories.profile.data.ProfileData;
import org.mvplugins.multiverse.inventories.profile.key.ContainerType;
import org.mvplugins.multiverse.inventories.profile.key.GlobalProfileKey;
import org.mvplugins.multiverse.inventories.profile.key.ProfileKey;
import org.mvplugins.multiverse.inventories.profile.key.ProfileTypes;
import org.mvplugins.multiverse.inventories.profile.nbt.PlayerDataExtractor;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
final class PlayerDataImportCommand extends InventoriesCommand {

    private final PlayerDataExtractor playerDataExtractor;
    private final ProfileDataSource profileDataSource;

    @Inject
    PlayerDataImportCommand(PlayerDataExtractor playerDataExtractor, ProfileDataSource profileDataSource) {
        this.playerDataExtractor = playerDataExtractor;
        this.profileDataSource = profileDataSource;
    }

    @Subcommand("playerdata import")
    @Syntax("<world>")
    @CommandPermission("multiverse.inventories.importplayerdata")
    @CommandCompletion("@worldwithplayerdata")
    @Description("Import player data from the world's playerdata folder.")
    void onCommand(MVCommandIssuer issuer, World world) {
        Path worldPath = world.getWorldFolder().toPath();
        File playerDataPath = worldPath.resolve("playerdata").toFile();
        if (!playerDataPath.isDirectory()) {
            issuer.sendMessage("World's playerdata folder does not exist: " + world.getName());
            return;
        }

        List<CompletableFuture<Void>> playerDataFutures = new ArrayList<>();
        File[] files = playerDataPath.listFiles();
        if (files == null) {
            issuer.sendMessage("No player data files found in the world's playerdata folder: " + world.getName());
            return;
        }

        for (File playerDataFile : files) {
            if (!Files.getFileExtension(playerDataFile.getName()).equals("dat")) {
                continue;
            }
            UUID playerUUID = UUID.fromString(Files.getNameWithoutExtension(playerDataFile.getName()));
            Try<ProfileData> profileData = playerDataExtractor.extract(playerDataFile.toPath());
            playerDataFutures.add(profileDataSource
                    .getGlobalProfile(GlobalProfileKey.of(playerUUID))
                    .thenCompose(profileDataSource::updateGlobalProfile)
                    .thenCompose(ignore -> profileDataSource.getPlayerProfile(
                            ProfileKey.of(ContainerType.WORLD, world.getName(), ProfileTypes.getDefault(), playerUUID)))
                    .thenCompose(playerProfile -> {
                        playerProfile.update(profileData.get());
                        return profileDataSource.updatePlayerProfile(playerProfile);
                    }));
        }
        CompletableFuture.allOf(playerDataFutures.toArray(new CompletableFuture[0]))
                        .thenRun(() -> issuer.sendMessage("Successfully imported all player data from " + world.getName() + "."));
    }
}
