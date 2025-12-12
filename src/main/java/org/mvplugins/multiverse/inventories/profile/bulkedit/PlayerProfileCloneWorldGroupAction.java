package org.mvplugins.multiverse.inventories.profile.bulkedit;

import org.mvplugins.multiverse.inventories.MultiverseInventories;
import org.mvplugins.multiverse.inventories.profile.key.ContainerKey;
import org.mvplugins.multiverse.inventories.profile.key.ProfileFileKey;

import java.util.concurrent.CompletableFuture;

final class PlayerProfileCloneWorldGroupAction extends AllSharesPlayerFileAction {

    private final ContainerKey fromContainerKey;

    PlayerProfileCloneWorldGroupAction(MultiverseInventories inventories,
                                       PlayerProfilesPayload bulkProfilesPayload,
                                       ContainerKey fromContainerKey) {
        super(inventories, bulkProfilesPayload);
        this.fromContainerKey = fromContainerKey;
    }

    @Override
    protected CompletableFuture<Void> performAction(ProfileFileKey key) {
        ProfileFileKey fromKey = key.forContainer(fromContainerKey);
        return profileDataSource.clonePlayerProfiles(fromKey, key, bulkProfilesPayload.profileTypes())
                .thenCompose(ignore -> profileDataSource.modifyGlobalProfile(
                        key,
                        profile -> profile.setLoadOnLogin(true)
                ));
    }
}
