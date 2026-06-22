package org.mvplugins.multiverse.inventories.commands;

import com.github.benmanes.caffeine.cache.stats.CacheStats;
import org.bukkit.entity.Player;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.core.command.MVCommandIssuer;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandCompletion;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandPermission;
import org.mvplugins.multiverse.external.acf.commands.annotation.Flags;
import org.mvplugins.multiverse.external.acf.commands.annotation.Subcommand;
import org.mvplugins.multiverse.external.acf.commands.annotation.Syntax;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.inventories.profile.ProfileCacheManager;
import org.mvplugins.multiverse.inventories.util.MVInvi18n;

import java.util.Map;

import static org.mvplugins.multiverse.core.locale.message.MessageReplacement.replace;

@Service
final class CacheCommand extends InventoriesCommand {

    private final ProfileCacheManager ProfileCacheManager;

    @Inject
    CacheCommand(@NotNull ProfileCacheManager ProfileCacheManager) {
        this.ProfileCacheManager = ProfileCacheManager;
    }

    @Subcommand("cache stats")
    @CommandPermission("multiverse.inventories.cache.stats")
    void onCacheStatsCommand(MVCommandIssuer issuer) {
        Map<String, CacheStats> stats = this.ProfileCacheManager.getCacheStats();
        for (Map.Entry<String, CacheStats> entry : stats.entrySet()) {
            CacheStats cacheStats = entry.getValue();
            issuer.sendMessage(MVInvi18n.CACHE_ENTRY, replace("{cache}").with(entry.getKey()));
            issuer.sendMessage(MVInvi18n.CACHE_HITSCOUNT, replace("{count}").with(cacheStats.hitCount()));
            issuer.sendMessage(MVInvi18n.CACHE_MISSESCOUNT, replace("{count}").with(cacheStats.missCount()));
            issuer.sendMessage(MVInvi18n.CACHE_LOADSCOUNT, replace("{count}").with(cacheStats.loadCount()));
            issuer.sendMessage(MVInvi18n.CACHE_EVICTIONS, replace("{count}").with(cacheStats.evictionCount()));
            issuer.sendMessage(MVInvi18n.CACHE_HITRATE, replace("{rate}").with(cacheStats.hitRate() * 100));
            issuer.sendMessage(MVInvi18n.CACHE_MISSRATE, replace("{rate}").with(cacheStats.missRate() * 100));
            issuer.sendMessage(MVInvi18n.CACHE_AVGLOADPENALTY,
                    replace("{milliseconds}").with(cacheStats.averageLoadPenalty() / 1000000));
            issuer.sendMessage(MVInvi18n.CACHE_SEPARATOR);
        }
    }

    @Subcommand("cache invalidate all")
    @CommandPermission("multiverse.inventories.cache.invalidate")
    void onCacheClearAllCommand(MVCommandIssuer issuer) {
        this.ProfileCacheManager.clearAllCache();
    }

    @Subcommand("cache invalidate player")
    @CommandPermission("multiverse.inventories.cache.invalidate")
    @CommandCompletion("@players")
    @Syntax("<player>")
    void onCacheClearProfileCommand(
            MVCommandIssuer issuer,

            @Flags("resolve=issuerAware")
            Player player) {
        this.ProfileCacheManager.clearPlayerProfileCache(key ->
                key.getPlayerUUID().equals(player.getUniqueId()));
    }
}
