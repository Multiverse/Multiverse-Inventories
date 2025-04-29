package org.mvplugins.multiverse.inventories.profile.bulkedit;

import org.jetbrains.annotations.ApiStatus;
import org.mvplugins.multiverse.inventories.MultiverseInventories;
import org.mvplugins.multiverse.inventories.profile.key.ProfileKey;

import java.util.List;
import java.util.Map;

abstract sealed class PlayerProfileAction extends BulkEditAction<ProfileKey> permits PlayerProfileDeleteAction {

    private final PlayerProfilesAggregator profilesAggregator;
    private final PlayerProfilesPayload bulkProfilesPayload;

    protected PlayerProfileAction(
            MultiverseInventories inventories,
            PlayerProfilesPayload bulkProfilesPayload
    ) {
        super(inventories, bulkProfilesPayload.globalProfileKeys());
        this.profilesAggregator = inventories.getServiceLocator().getService(PlayerProfilesAggregator.class);
        this.bulkProfilesPayload = bulkProfilesPayload;
    }

    @Override
    protected List<ProfileKey> aggregateKeys() {
        return profilesAggregator.getPlayerProfileKeys(bulkProfilesPayload);
    }

    @Override
    public Map<String, List<String>> getActionSummary() {
        return bulkProfilesPayload.getSummary();
    }
}
