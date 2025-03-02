package org.mvplugins.multiverse.inventories.dataimport;

import com.dumptruckman.minecraft.util.Logging;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jvnet.hk2.annotations.Contract;

/**
 * Abstract implementation of {@link DataImporter} without actual import logic.
 */
@Contract
public abstract class AbstractDataImporter implements DataImporter {

    protected Plugin importer = null;

    public AbstractDataImporter() {
    }

    /**
     * Logic that does the actual importing data.
     *
     * @throws DataImportException Errors occurred that caused import to fail.
     */
    protected abstract void doDataImport() throws DataImportException;

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean importData(boolean disableOnSuccess) {
        if (!isEnabled()) {
            Logging.severe("Data importer %s not enabled. No data is imported.", this.getPluginName());
            return false;
        }

        try {
            doDataImport();
        } catch (DataImportException e) {
            Logging.severe(e.getMessage());
            if(e.getCauseException() != null) {
                Logging.severe("Cause: %s", e.getCauseException().getMessage());
            }
            e.printStackTrace();
            return false;
        }

        Logging.info("Successfully imported data from %s!", this.getPluginName());
        if (disableOnSuccess) {
            Logging.info("Disabling %s...", this.getPluginName());
            Bukkit.getPluginManager().disablePlugin(this.importer);
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean importData() {
        return importData(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean enable(Plugin importerPlugin) {
        if (isEnabled()) {
            return false;
        }
        if (!importerPlugin.getClass().equals(this.getPluginClass())) {
            Logging.warning("Plugin '%s' is not data importer for '%s'.",
                    importerPlugin.getClass().getName(), getPluginName());
            return false;
        }
        try {
            this.importer = importerPlugin;
        } catch (ClassCastException | NoClassDefFoundError e) {
            Logging.warning("Error while enabling data importer for '%s'.", getPluginName());
            return false;
        }
        Logging.info("Successfully enabled data importer for '%s'.", getPluginName());
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean enable() {
        Plugin importerPlugin = Bukkit.getPluginManager().getPlugin(this.getPluginName());
        if (importerPlugin == null) {
            Logging.finer("Unable to get plugin '%s' for import hook.", this.getPluginName());
            return false;
        }
        return enable(importerPlugin);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean disable() {
        this.importer = null;
        Logging.info("Successfully disabled data importer for '%s'.", getPluginName());
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEnabled() {
        return importer != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Plugin getPlugin() {
        return importer;
    }
}
