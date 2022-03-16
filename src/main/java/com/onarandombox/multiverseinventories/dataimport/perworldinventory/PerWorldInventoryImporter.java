package com.onarandombox.multiverseinventories.dataimport.perworldinventory;

import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.dataimport.AbstractDataImporter;
import com.onarandombox.multiverseinventories.dataimport.DataImportException;
import me.ebonjaeger.perworldinventory.GroupManager;
import me.ebonjaeger.perworldinventory.PerWorldInventory;
import me.ebonjaeger.perworldinventory.api.PerWorldInventoryAPI;
import me.ebonjaeger.perworldinventory.data.DataSource;
import me.ebonjaeger.perworldinventory.data.ProfileManager;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;

public class PerWorldInventoryImporter extends AbstractDataImporter<PerWorldInventory> {

    private PerWorldInventoryAPI pwiAPI;
    private GroupManager pwiGroupManager;
    private ProfileManager pwiProfileManager;
    private DataSource pwiDataSource;

    private Method getFileMethod;

    public PerWorldInventoryImporter(MultiverseInventories plugin) {
        super(plugin);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doDataImport() throws DataImportException {
        new PwiImportHelper(this.plugin, this.importer.getApi()).importData();
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
