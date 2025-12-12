package org.mvplugins.multiverse.inventories.profile.bulkedit;

import org.mvplugins.multiverse.inventories.MultiverseInventories;
import org.mvplugins.multiverse.inventories.profile.key.ProfileFileKey;

import java.util.List;
import java.util.Map;

abstract sealed class PlayerFileAction extends BulkEditAction<ProfileFileKey>
        permits AllSharesPlayerFileAction {

    private final PlayerProfilesAggregator profilesAggregator;
    protected final PlayerProfilesPayload bulkProfilesPayload;
    
    PlayerFileAction(MultiverseInventories inventories, PlayerProfilesPayload bulkProfilesPayload) {
        super(inventories, bulkProfilesPayload.globalProfileKeys());
        this.profilesAggregator = inventories.getServiceLocator().getService(PlayerProfilesAggregator.class);
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
