package org.mvplugins.multiverse.inventories.profile;

import com.github.benmanes.caffeine.cache.AsyncCache;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import com.google.common.collect.Sets;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.vavr.control.Option;
import org.mvplugins.multiverse.inventories.config.InventoriesConfig;
import org.mvplugins.multiverse.inventories.profile.key.ProfileKey;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

@Service
public final class ProfileCacheManager {

    private final Cache<ProfileKey, FileConfiguration> playerFileCache;
    private final AsyncCache<ProfileKey, PlayerProfile> playerProfileCache;
    private final AsyncCache<UUID, GlobalProfile> globalProfileCache;

    @Inject
    ProfileCacheManager(@NotNull InventoriesConfig inventoriesConfig, @NotNull AsyncFileIO asyncFileIO) {
        this.playerFileCache = Caffeine.newBuilder()
                .expireAfterAccess(inventoriesConfig.getPlayerFileCacheExpiry(), TimeUnit.MINUTES)
                .maximumSize(inventoriesConfig.getPlayerFileCacheSize())
                .recordStats()
                .build();

        this.playerProfileCache = Caffeine.newBuilder()
                .expireAfterAccess(inventoriesConfig.getPlayerProfileCacheExpiry(), TimeUnit.MINUTES)
                .maximumSize(inventoriesConfig.getPlayerProfileCacheSize())
                .executor(asyncFileIO.getExecutor())
                .recordStats()
                .buildAsync();

        this.globalProfileCache = Caffeine.newBuilder()
                .expireAfterAccess(inventoriesConfig.getGlobalProfileCacheExpiry(), TimeUnit.MINUTES)
                .maximumSize(inventoriesConfig.getGlobalProfileCacheSize())
                .executor(asyncFileIO.getExecutor())
                .recordStats()
                .buildAsync();
    }

    FileConfiguration getOrLoadPlayerFile(ProfileKey key, Function<ProfileKey, FileConfiguration> mappingFunction) {
        return playerFileCache.get(key, mappingFunction);
    }

    CompletableFuture<PlayerProfile> getOrLoadPlayerProfile(ProfileKey key, BiFunction<ProfileKey, ? super Executor, ? extends CompletableFuture<PlayerProfile>> mappingFunction) {
        return playerProfileCache.get(key, mappingFunction);
    }

    CompletableFuture<GlobalProfile> getOrLoadGlobalProfile(UUID uuid, BiFunction<UUID, ? super Executor, ? extends CompletableFuture<GlobalProfile>> mappingFunction) {
        return globalProfileCache.get(uuid, mappingFunction);
    }

    Option<PlayerProfile> getCachedPlayerProfile(ProfileKey key) {
        return Option.of(playerProfileCache.synchronous().getIfPresent(key));
    }

    public void clearPlayerCache(String playerName) {
        clearPlayerProfileCache(key -> key.getPlayerName().equals(playerName));
    }

    public void clearPlayerCache(UUID playerUUID) {
        clearPlayerProfileCache(key -> key.getPlayerUUID().equals(playerUUID));
        clearGlobalProfileCache(key -> key.equals(playerUUID));
    }

    public void clearPlayerProfileCache(Predicate<ProfileKey> predicate) {
        playerFileCache.invalidateAll(Sets.filter(playerFileCache.asMap().keySet(), predicate::test));
        playerProfileCache.synchronous().invalidateAll(Sets.filter(playerProfileCache.asMap().keySet(), predicate::test));
    }

    public void clearAllPlayerProfileCaches() {
        playerFileCache.invalidateAll();
        playerProfileCache.synchronous().invalidateAll();
    }

    public void clearGlobalProfileCache(Predicate<UUID> predicate) {
        globalProfileCache.synchronous().invalidateAll(Sets.filter(globalProfileCache.asMap().keySet(), predicate::test));
    }

    public void clearAllGlobalProfileCaches() {
        globalProfileCache.synchronous().invalidateAll();
    }

    public void clearAllCache() {
        playerFileCache.invalidateAll();
        globalProfileCache.synchronous().invalidateAll();
        playerProfileCache.synchronous().invalidateAll();
    }

    public Map<String, CacheStats> getCacheStats() {
        Map<String, CacheStats> stats = new HashMap<>();
        stats.put("playerFileCache", playerFileCache.stats());
        stats.put("globalProfileCache", globalProfileCache.synchronous().stats());
        stats.put("profileCache", playerProfileCache.synchronous().stats());
        return stats;
    }
}
