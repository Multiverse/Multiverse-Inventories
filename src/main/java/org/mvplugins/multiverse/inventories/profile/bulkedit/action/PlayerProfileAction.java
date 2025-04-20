package org.mvplugins.multiverse.inventories.profile.bulkedit.action;

import org.jetbrains.annotations.ApiStatus;
import org.mvplugins.multiverse.inventories.MultiverseInventories;
import org.mvplugins.multiverse.inventories.profile.bulkedit.BulkProfilesAggregator;
import org.mvplugins.multiverse.inventories.profile.bulkedit.BulkProfilesPayload;
import org.mvplugins.multiverse.inventories.profile.key.ProfileKey;

import java.util.List;
import java.util.Map;

@ApiStatus.Experimental
public abstract class PlayerProfileAction extends BulkEditAction<ProfileKey> {

    private final BulkProfilesAggregator profilesAggregator;
    private final BulkProfilesPayload bulkProfilesPayload;

    protected PlayerProfileAction(
            MultiverseInventories inventories,
            BulkProfilesPayload bulkProfilesPayload
    ) {
        super(inventories, bulkProfilesPayload.globalProfileKeys());
        this.profilesAggregator = inventories.getServiceLocator().getService(BulkProfilesAggregator.class);
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
