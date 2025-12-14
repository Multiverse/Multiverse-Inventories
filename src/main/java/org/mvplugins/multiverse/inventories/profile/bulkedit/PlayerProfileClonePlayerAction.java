package org.mvplugins.multiverse.inventories.profile.bulkedit;

import org.mvplugins.multiverse.inventories.MultiverseInventories;
import org.mvplugins.multiverse.inventories.profile.key.GlobalProfileKey;
import org.mvplugins.multiverse.inventories.profile.key.ProfileFileKey;

import java.util.concurrent.CompletableFuture;

final class PlayerProfileClonePlayerAction extends AllSharesPlayerFileAction {

    private final GlobalProfileKey fromPlayer;

    PlayerProfileClonePlayerAction(MultiverseInventories inventories,
                                   PlayerProfilesPayload bulkProfilesPayload,
                                   GlobalProfileKey fromPlayer) {
        super(inventories, bulkProfilesPayload);
        this.fromPlayer = fromPlayer;
    }

    @Override
    protected CompletableFuture<Void> performAction(ProfileFileKey key) {
        ProfileFileKey fromKey = ProfileFileKey.of(key.getContainerType(), key.getDataName(), fromPlayer);
        return profileDataSource.clonePlayerProfiles(fromKey, key, bulkProfilesPayload.profileTypes())
                .thenCompose(ignore -> profileDataSource.modifyGlobalProfile(
                        key,
                        profile -> profile.setLoadOnLogin(true)
                ));
    }
}
