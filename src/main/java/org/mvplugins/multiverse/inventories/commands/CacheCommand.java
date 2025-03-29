package org.mvplugins.multiverse.inventories.commands;

import com.github.benmanes.caffeine.cache.stats.CacheStats;
import org.bukkit.entity.Player;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.core.command.MVCommandIssuer;
import org.mvplugins.multiverse.core.command.MVCommandManager;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandAlias;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandCompletion;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandPermission;
import org.mvplugins.multiverse.external.acf.commands.annotation.Flags;
import org.mvplugins.multiverse.external.acf.commands.annotation.Subcommand;
import org.mvplugins.multiverse.external.acf.commands.annotation.Syntax;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.inventories.profile.ProfileDataSource;

import java.util.Map;

@Service
@CommandAlias("mvinv")
final class CacheCommand extends InventoriesCommand {

    private final ProfileDataSource profileDataSource;

    @Inject
    CacheCommand(@NotNull MVCommandManager commandManager, @NotNull ProfileDataSource profileDataSource) {
        super(commandManager);
        this.profileDataSource = profileDataSource;
    }

    @Subcommand("cache stats")
    @CommandPermission("multiverse.inventories.cache.stats")
    void onCacheStatsCommand(MVCommandIssuer issuer) {
        Map<String, CacheStats> stats = this.profileDataSource.getCacheStats();
        for (Map.Entry<String, CacheStats> entry : stats.entrySet()) {
            issuer.sendMessage("Cache: " + entry.getKey());
            issuer.sendMessage("  hits count: " + entry.getValue().hitCount());
            issuer.sendMessage("  misses count: " + entry.getValue().missCount());
            issuer.sendMessage("  loads count: " + entry.getValue().loadCount());
            issuer.sendMessage("  evictions: " + entry.getValue().evictionCount());
            issuer.sendMessage("  hit rate: " + entry.getValue().hitRate() * 100 + "%");
            issuer.sendMessage("  miss rate: " + entry.getValue().missRate() * 100 + "%");
            issuer.sendMessage("  avg load penalty: " + entry.getValue().averageLoadPenalty() / 1000000 + "ms");
            issuer.sendMessage("--------");
        }
    }

    @Subcommand("cache invalidate all")
    @CommandPermission("multiverse.inventories.cache.invalidate")
    void onCacheClearAllCommand(MVCommandIssuer issuer) {
        this.profileDataSource.clearAllCache();
    }

    @Subcommand("cache invalidate player")
    @CommandPermission("multiverse.inventories.cache.invalidate")
    @CommandCompletion("@players")
    @Syntax("<player>")
    void onCacheClearProfileCommand(
            MVCommandIssuer issuer,

            @Flags("resolve=issuerAware")
            Player player) {
        this.profileDataSource.clearProfileCache(key -> key.getPlayerUUID().equals(player.getUniqueId()));
    }
}
