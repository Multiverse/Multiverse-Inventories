package org.mvplugins.multiverse.inventories.profile.bulkedit.action;

import org.jetbrains.annotations.ApiStatus;
import org.mvplugins.multiverse.inventories.MultiverseInventories;
import org.mvplugins.multiverse.inventories.profile.bulkedit.BulkProfilesAggregator;
import org.mvplugins.multiverse.inventories.profile.bulkedit.BulkProfilesPayload;
import org.mvplugins.multiverse.inventories.profile.key.ProfileFileKey;

import java.util.List;
import java.util.Map;

@ApiStatus.Experimental
public abstract class PlayerFileAction extends BulkEditAction<ProfileFileKey> {

    private final BulkProfilesAggregator profilesAggregator;
    protected final BulkProfilesPayload bulkProfilesPayload; 
    
    PlayerFileAction(MultiverseInventories inventories, BulkProfilesPayload bulkProfilesPayload) {
        super(inventories, bulkProfilesPayload.globalProfileKeys());
        this.profilesAggregator = inventories.getServiceLocator().getService(BulkProfilesAggregator.class);
        this.bulkProfilesPayload = bulkProfilesPayload;
    }

    @Override
    protected List<ProfileFileKey> aggregateKeys() {
        return profilesAggregator.getProfileFileKeys(bulkProfilesPayload);
    }

    @Override
    public Map<String, List<String>> getActionSummary() {
        return bulkProfilesPayload.getSummary();
    }
}
