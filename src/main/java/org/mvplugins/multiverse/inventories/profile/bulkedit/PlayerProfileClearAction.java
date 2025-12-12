package org.mvplugins.multiverse.inventories.profile.bulkedit;

import org.mvplugins.multiverse.inventories.MultiverseInventories;
import org.mvplugins.multiverse.inventories.profile.key.ProfileFileKey;

import java.util.concurrent.CompletableFuture;

final class PlayerProfileClearAction extends AllSharesPlayerFileAction {

    public PlayerProfileClearAction(MultiverseInventories inventories, PlayerProfilesPayload bulkProfilesPayload) {
        super(inventories, bulkProfilesPayload);
    }

    @Override
    protected CompletableFuture<Void> performAction(ProfileFileKey key) {
        return profileDataSource.deletePlayerProfiles(key, bulkProfilesPayload.profileTypes())
                .thenCompose(ignore -> profileDataSource.modifyGlobalProfile(
                        key,
                        profile -> profile.setLoadOnLogin(true)
                ));
    }
}
