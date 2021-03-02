package com.onarandombox.multiverseinventories.dataimport;

import org.bukkit.plugin.Plugin;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Manager class for importing data from other inventory plugins or similar, e.g. PerWorldInventory.
 */
public class DataImportManager {

    final private Map<String, DataImporter<? extends Plugin>> dataImporters;

    public DataImportManager() {
        this.dataImporters = new HashMap<>();
    }

    /**
     * Register a Data Importer and optionally try to enable to it as well.
     *
     * @param dataImporter  The Data Importer to register.
     * @param tryEnable     Whether to try and {@link DataImporter#enable(Plugin)} the Data Importer.
     */
    public void register(DataImporter<? extends Plugin> dataImporter, boolean tryEnable) {
        this.dataImporters.put(dataImporter.getPluginName().toLowerCase(), dataImporter);
        if (tryEnable) {
            dataImporter.enable();
        }
    }

    /**
     * Register a Data Importer and try to enable to it as well.
     *
     * @param dataImporter  The Data Importer to register.
     */
    public void register(DataImporter<? extends Plugin> dataImporter) {
        this.register(dataImporter, true);
    }

    /**
     * Gets a {@link DataImporter} based on an importable plugin name.
     *
     * @param pluginName The plugin name you want to import data from.
     * @return The {@link DataImporter} if Data Importer present for that plugin, else null.
     */
    public DataImporter<? extends Plugin> getImporter(String pluginName) {
        return this.dataImporters.get(pluginName.toLowerCase());
    }

    /**
     * Gets a {@link DataImporter} based on an importable {@link Plugin}.
     *
     * @param plugin The plugin you want to import data from.
     * @return The {@link DataImporter} if Data Importer present for that plugin, else null.
     */
    public DataImporter<? extends Plugin> getImporter(Plugin plugin) {
        return getImporter(plugin.getName());
    }

    /**
     * Gets all the Data Importer names that are enabled.
     *
     * @return A collection of Data Importer names that are enabled.
     */
    public Collection<String> getEnabledImporterNames() {
        return this.dataImporters.values().stream()
                .filter(DataImporter::isEnabled)
                .map(DataImporter::getPluginName)
                .collect(Collectors.toList());
    }
}
