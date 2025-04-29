package org.mvplugins.multiverse.inventories.profile.bulkedit;

import org.bukkit.entity.Player;
import org.mvplugins.multiverse.inventories.MultiverseInventories;
import org.mvplugins.multiverse.inventories.handleshare.ReadOnlyShareHandler;
import org.mvplugins.multiverse.inventories.profile.key.GlobalProfileKey;

import java.util.List;
import java.util.concurrent.CompletableFuture;

final class GlobalProfileClearAction extends BulkEditAction<GlobalProfileKey> {

    private final boolean clearPlayerProfile;

    GlobalProfileClearAction(MultiverseInventories inventories, GlobalProfileKey[] globalProfileKeys, boolean clearPlayerProfiles) {
        super(inventories, globalProfileKeys);
        this.clearPlayerProfile = clearPlayerProfiles;
    }

    @Override
    protected List<GlobalProfileKey> aggregateKeys() {
        return List.of(globalProfileKeys);
    }

    @Override
    protected CompletableFuture<Void> performAction(GlobalProfileKey key) {
        return profileDataSource.deleteGlobalProfile(key, clearPlayerProfile);
    }

    @Override
    protected boolean isOnlinePlayerAffected(GlobalProfileKey key, Player player) {
        return super.isOnlinePlayerAffected(key, player) && clearPlayerProfile;
    }

    @Override
    protected void updateOnlinePlayerNow(Player player) {
        new ReadOnlyShareHandler(inventories, player).handleSharing();
    }
}
