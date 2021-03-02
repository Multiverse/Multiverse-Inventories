package com.onarandombox.multiverseinventories.dataimport;

import com.dumptruckman.minecraft.util.Logging;
import com.onarandombox.multiverseinventories.MultiverseInventories;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

/**
 * Abstract implementation of {@link DataImporter} without actual import logic.
 */
public abstract class AbstractDataImporter<T extends Plugin> implements DataImporter<T> {

    protected final MultiverseInventories plugin;
    protected T importer = null;

    public AbstractDataImporter(MultiverseInventories plugin) {
        this.plugin = plugin;
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
            Logging.severe("Cause: %s", e.getCauseException().getMessage());
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
                    plugin.getClass().getName(), getPluginName());
            return false;
        }
        try {
            this.importer = (T) importerPlugin;
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
    public T getPlugin() {
        return importer;
    }
}
