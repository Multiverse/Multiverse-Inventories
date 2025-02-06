package org.mvplugins.multiverse.inventories.dataimport.multiinv;

import org.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.inventories.config.InventoriesConfig;
import org.mvplugins.multiverse.inventories.dataimport.AbstractDataImporter;
import org.mvplugins.multiverse.inventories.dataimport.DataImportException;
import org.mvplugins.multiverse.inventories.profile.ProfileDataSource;
import org.mvplugins.multiverse.inventories.profile.container.ProfileContainerStoreProvider;
import org.mvplugins.multiverse.inventories.profile.group.WorldGroupManager;
import uk.co.tggl.pluckerpluck.multiinv.MultiInv;

@Service
final class MultiInvImporter extends AbstractDataImporter {

    private final WorldGroupManager worldGroupManager;
    private final InventoriesConfig inventoriesConfig;
    private final ProfileContainerStoreProvider profileContainerStoreProvider;
    private final ProfileDataSource profileDataSource;

    @Inject
    MultiInvImporter(
            @NotNull WorldGroupManager worldGroupManager,
            @NotNull InventoriesConfig inventoriesConfig,
            @NotNull ProfileContainerStoreProvider profileContainerStoreProvider,
            @NotNull ProfileDataSource profileDataSource) {
        super();
        this.worldGroupManager = worldGroupManager;
        this.inventoriesConfig = inventoriesConfig;
        this.profileContainerStoreProvider = profileContainerStoreProvider;
        this.profileDataSource = profileDataSource;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doDataImport() throws DataImportException {
        new MultiInvImportHelper(
                (MultiInv) importer,
                worldGroupManager,
                inventoriesConfig,
                profileContainerStoreProvider,
                profileDataSource
        ).importData();
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull String getPluginName() {
        return "MultiInv";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull Class<MultiInv> getPluginClass() {
        return MultiInv.class;
    }
}
