package org.mvplugins.multiverse.inventories.dataimport.perworldinventory;

import me.ebonjaeger.perworldinventory.PerWorldInventory;
import org.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.core.world.WorldManager;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.inventories.config.InventoriesConfig;
import org.mvplugins.multiverse.inventories.dataimport.AbstractDataImporter;
import org.mvplugins.multiverse.inventories.dataimport.DataImportException;
import org.mvplugins.multiverse.inventories.profile.ProfileDataSource;
import org.mvplugins.multiverse.inventories.profile.group.WorldGroupManager;

import java.util.Objects;

@Service
public class PerWorldInventoryImporter extends AbstractDataImporter {

    private final InventoriesConfig inventoriesConfig;
    private final WorldManager worldManager;
    private final WorldGroupManager worldGroupManager;
    private final ProfileDataSource profileDataSource;

    @Inject
    PerWorldInventoryImporter(
            @NotNull InventoriesConfig inventoriesConfig,
            @NotNull WorldManager worldManager,
            @NotNull WorldGroupManager worldGroupManager,
            @NotNull ProfileDataSource profileDataSource) {
        super();
        this.inventoriesConfig = inventoriesConfig;
        this.worldManager = worldManager;
        this.worldGroupManager = worldGroupManager;
        this.profileDataSource = profileDataSource;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doDataImport() throws DataImportException {
        new PwiImportHelper(
                Objects.requireNonNull(((PerWorldInventory) importer).getApi()),
                inventoriesConfig,
                worldManager,
                worldGroupManager,
                profileDataSource
        ).importData();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull String getPluginName() {
        return "PerWorldInventory";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull Class<PerWorldInventory> getPluginClass() {
        return PerWorldInventory.class;
    }
}
