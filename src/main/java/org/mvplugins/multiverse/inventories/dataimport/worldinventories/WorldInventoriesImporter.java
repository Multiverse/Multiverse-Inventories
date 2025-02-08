package org.mvplugins.multiverse.inventories.dataimport.worldinventories;

import me.drayshak.WorldInventories.WorldInventories;
import org.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.inventories.config.InventoriesConfig;
import org.mvplugins.multiverse.inventories.dataimport.AbstractDataImporter;
import org.mvplugins.multiverse.inventories.dataimport.DataImportException;
import org.mvplugins.multiverse.inventories.profile.ProfileDataSource;
import org.mvplugins.multiverse.inventories.profile.container.ProfileContainerStoreProvider;
import org.mvplugins.multiverse.inventories.profile.group.WorldGroupManager;

@Service
final class WorldInventoriesImporter extends AbstractDataImporter {

    private final WorldGroupManager worldGroupManager;
    private final InventoriesConfig inventoriesConfig;
    private final ProfileContainerStoreProvider profileContainerStoreProvider;
    private final ProfileDataSource profileDataSource;

    @Inject
    WorldInventoriesImporter(
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
        new WorldInventoriesImportHelper(
                (WorldInventories) importer,
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
        return "WorldInventories";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull Class<WorldInventories> getPluginClass() {
        return WorldInventories.class;
    }
}
