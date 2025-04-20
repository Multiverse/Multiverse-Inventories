package org.mvplugins.multiverse.inventories.profile.bulkedit.action;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.mvplugins.multiverse.inventories.MultiverseInventories;
import org.mvplugins.multiverse.inventories.profile.ProfileDataSource;
import org.mvplugins.multiverse.inventories.profile.key.GlobalProfileKey;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@ApiStatus.Experimental
public abstract class BulkEditAction<K extends GlobalProfileKey> {

    protected final MultiverseInventories inventories;
    protected final ProfileDataSource profileDataSource;
    protected final GlobalProfileKey[] globalProfileKeys;

    BulkEditAction(MultiverseInventories inventories, GlobalProfileKey[] globalProfileKeys) {
        this.inventories = inventories;
        this.profileDataSource = inventories.getServiceLocator().getService(ProfileDataSource.class);
        this.globalProfileKeys = globalProfileKeys;
    }

    public CompletableFuture<BulkEditResult> execute() {
        BulkEditResult bulkEditResult = new BulkEditResult();
        List<K> targetKeys = aggregateKeys();
        Set<Player> onlinePlayers = new HashSet<>();
        return CompletableFuture.allOf(targetKeys.stream()
                .map(key -> {
                    Player player = key.getOnlinePlayer();
                    if (player != null && isOnlinePlayerAffected(key, player)) {
                        onlinePlayers.add(player);
                    }
                    return performAction(key)
                            .thenRun(bulkEditResult::incrementSuccess)
                            .exceptionally(throwable -> {
                                bulkEditResult.incrementFailure();
                                throwable.printStackTrace();
                                return null;
                            });
                })
                .toArray(CompletableFuture[]::new))
                .thenRun(() ->
                        Bukkit.getScheduler().runTask(inventories, () ->
                                onlinePlayers.forEach(this::updateOnlinePlayerNow)))
                .thenApply(ignore -> bulkEditResult);
    }

    protected abstract List<K> aggregateKeys();

    protected abstract CompletableFuture<Void> performAction(K key);

    protected boolean isOnlinePlayerAffected(K key, Player player) {
        return key.getPlayerUUID().equals(player.getUniqueId());
    }

    protected abstract void updateOnlinePlayerNow(Player player);

    public Map<String, List<String>> getActionSummary() {
        return Map.of("Players", Arrays.stream(globalProfileKeys)
                .map(GlobalProfileKey::getPlayerName)
                .toList());
    }
}
