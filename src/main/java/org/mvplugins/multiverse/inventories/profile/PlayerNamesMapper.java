package org.mvplugins.multiverse.inventories.profile;

import com.dumptruckman.minecraft.util.Logging;
import com.google.common.base.Strings;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import net.minidev.json.parser.JSONParser;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jakarta.inject.Provider;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.external.vavr.control.Option;
import org.mvplugins.multiverse.external.vavr.control.Try;
import org.mvplugins.multiverse.inventories.MultiverseInventories;
import org.mvplugins.multiverse.inventories.profile.key.GlobalProfileKey;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
public final class PlayerNamesMapper {

    private static PlayerNamesMapper instance;

    public static PlayerNamesMapper getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Player names mapper has not been initialized yet.");
        }
        return instance;
    }

    private static final String FILENAME = "playernames.json";

    private final AsyncFileIO asyncFileIO;
    private final Provider<ProfileDataSource> profileDataSourceProvider;
    private final Provider<ProfileCacheManager> profileCacheManagerProvider;

    private final File playerNamesFile;
    private final Map<String, GlobalProfileKey> playerNamesMap;
    private final Map<UUID, GlobalProfileKey> playerUUIDMap;

    private Map<String, Object> playerNamesJson;

    @Inject
    private PlayerNamesMapper(
            @NotNull MultiverseInventories inventories,
            @NotNull AsyncFileIO asyncFileIO,
            @NotNull Provider<ProfileDataSource> profileDataSourceProvider,
            @NotNull Provider<ProfileCacheManager> profileCacheManagerProvider
    ) {
        this.asyncFileIO = asyncFileIO;
        this.profileDataSourceProvider = profileDataSourceProvider;
        this.profileCacheManagerProvider = profileCacheManagerProvider;

        this.playerNamesFile = new File(inventories.getDataFolder(), FILENAME);
        this.playerNamesMap = new ConcurrentHashMap<>();
        this.playerUUIDMap = new ConcurrentHashMap<>();

        instance = this;
    }

    public void loadMap() {
        Logging.config("Loading player names map...");
        playerNamesMap.clear();
        playerUUIDMap.clear();
        if (playerNamesFile.exists()) {
            loadFromPlayerNamesFile();
        } else {
            buildPlayerNamesMap();
        }
    }

    private void loadFromPlayerNamesFile() {
        try (FileReader fileReader = new FileReader(playerNamesFile)) {
            playerNamesJson = new ConcurrentHashMap<>((JSONObject) new JSONParser(JSONParser.DEFAULT_PERMISSIVE_MODE).parse(fileReader));
            if (playerNamesJson.isEmpty()) {
                buildPlayerNamesMap();
                return;
            }
            playerNamesJson.forEach((String uuidStr, Object name) -> Try.of(() -> UUID.fromString(uuidStr))
                    .onSuccess(uuid -> {
                        String playerName = String.valueOf(name);
                        GlobalProfileKey globalProfileKey = GlobalProfileKey.of(uuid, playerName);
                        playerNamesMap.put(playerName, globalProfileKey);
                        playerUUIDMap.put(uuid, globalProfileKey);
                    })
                    .onFailure(throwable -> {
                        Logging.warning("Not a valid UUID: %s", uuidStr);
                        Logging.warning(throwable.getMessage());
                    }));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void buildPlayerNamesMap() {
        Logging.info("Generating player names map... This may take a while.");
        playerNamesJson = new ConcurrentHashMap<>();

        ProfileDataSource profileDataSource = profileDataSourceProvider.get();
        CompletableFuture[] futures = profileDataSource.listGlobalProfileUUIDs().stream()
                .map(uuid -> profileDataSource.getGlobalProfile(GlobalProfileKey.of(uuid, ""))
                        .thenAccept(globalProfile -> setPlayerName(uuid, globalProfile.getLastKnownName())))
                .toArray(CompletableFuture[]::new);
        CompletableFuture.allOf(futures).thenCompose(ignore -> savePlayerNames()).join();
        profileCacheManagerProvider.get().clearAllGlobalProfileCaches();
        Logging.info("Generated player names map.");
    }

    boolean setPlayerName(UUID uuid, String name) {
        if (playerNamesJson == null) {
            throw new IllegalStateException("Player names mapper has not been loaded yet.");
        }
        if (Strings.isNullOrEmpty(name)) {
            return false;
        }
        if (getKey(name).filter(g -> g.getPlayerUUID().equals(uuid)).isDefined()) {
            return false;
        }

        Logging.finer("Setting player name mapping for %s to %s", uuid, name);
        GlobalProfileKey globalProfileKey = GlobalProfileKey.of(uuid, name);

        // Handle remove of old playername
        Object oldName = playerNamesJson.put(uuid.toString(), name);
        playerNamesMap.remove(String.valueOf(oldName));

        playerNamesMap.put(name, globalProfileKey);
        playerUUIDMap.put(uuid, globalProfileKey);
        return true;
    }

    CompletableFuture<Void> savePlayerNames() {
        if (playerNamesJson == null) {
            throw new IllegalStateException("Player names mapper has not been loaded yet.");
        }
        return asyncFileIO.queueFileAction(playerNamesFile, () -> {
            Logging.finer("Saving player names map...");
            try (FileWriter fileWriter = new FileWriter(playerNamesFile)) {
                fileWriter.write(JSONValue.toJSONString(playerNamesJson));
                Logging.finer("Saving player names map... Done!");
            } catch (Exception e) {
                Logging.severe("Could not save player names map.");
                e.printStackTrace();
            }
        });
    }

    public Option<GlobalProfileKey> getKey(String playerName) {
        return Option.of(playerNamesMap.get(playerName));
    }

    public Option<GlobalProfileKey> getKey(UUID playerUUID) {
        return Option.of(playerUUIDMap.get(playerUUID));
    }

    public List<GlobalProfileKey> getKeys() {
        return playerNamesMap.values().stream().toList();
    }
}
