package org.mvplugins.multiverse.inventories.profile.bulkedit;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.inventories.MultiverseInventories;
import org.mvplugins.multiverse.inventories.profile.key.ContainerKey;
import org.mvplugins.multiverse.inventories.profile.key.GlobalProfileKey;
import org.mvplugins.multiverse.inventories.share.Sharable;

@Service
@ApiStatus.Experimental
public final class BulkEditCreator {

    private final MultiverseInventories inventories;

    @Inject
    BulkEditCreator(@NotNull MultiverseInventories inventories) {
        this.inventories = inventories;
    }

    public BulkEditAction<?> globalProfileClear(GlobalProfileKey[] globalProfileKeys, boolean clearPlayerProfiles) {
        return new GlobalProfileClearAction(inventories, globalProfileKeys, clearPlayerProfiles);
    }

    @ApiStatus.AvailableSince("5.3")
    public BulkEditAction<?> playerProfileCloneWorldGroup(ContainerKey fromContainerKey, PlayerProfilesPayload toProfiles) {
        return new PlayerProfileCloneWorldGroupAction(inventories, toProfiles, fromContainerKey);
    }

    @ApiStatus.AvailableSince("5.3")
    public BulkEditAction<?> playerProfileClonePlayer(GlobalProfileKey fromPlayer, PlayerProfilesPayload toProfiles) {
        return new PlayerProfileClonePlayerAction(inventories, toProfiles, fromPlayer);
    }

    public BulkEditAction<?> playerProfileClear(PlayerProfilesPayload bulkProfilesPayload) {
        return new PlayerProfileClearAction(inventories, bulkProfilesPayload);
    }

    public BulkEditAction<?> playerProfileDeleteSharable(PlayerProfilesPayload bulkProfilesPayload, Sharable<?> sharable) {
        return new PlayerProfileDeleteAction(inventories, sharable, bulkProfilesPayload);
    }
}
