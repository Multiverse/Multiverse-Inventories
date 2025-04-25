package org.mvplugins.multiverse.inventories.profile.bulkedit;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.inventories.MultiverseInventories;
import org.mvplugins.multiverse.inventories.share.Sharable;

@Service
@ApiStatus.Experimental
public final class BulkEditCreator {

    private final MultiverseInventories inventories;

    @Inject
    BulkEditCreator(@NotNull MultiverseInventories inventories) {
        this.inventories = inventories;
    }

    public BulkEditAction<?> playerProfileClear(PlayerProfilesPayload bulkProfilesPayload) {
        return new PlayerProfileClearAction(inventories, bulkProfilesPayload);
    }

    public BulkEditAction<?> playerProfileDeleteSharable(PlayerProfilesPayload bulkProfilesPayload, Sharable<?> sharable) {
        return new PlayerProfileDeleteAction(inventories, sharable, bulkProfilesPayload);
    }
}
