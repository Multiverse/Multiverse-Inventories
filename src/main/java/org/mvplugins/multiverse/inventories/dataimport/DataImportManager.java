package org.mvplugins.multiverse.inventories.dataimport;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.vavr.control.Option;
import org.mvplugins.multiverse.inventories.MultiverseInventories;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Manager class for importing data from other inventory plugins or similar, e.g. PerWorldInventory.
 */
@Service
public final class DataImportManager implements Listener {

    final private Map<String, DataImporter> dataImporters;

    @Inject
    DataImportManager(@NotNull MultiverseInventories inventories, @NotNull PluginManager pluginManager) {
        this.dataImporters = new HashMap<>();
        pluginManager.registerEvents(this, inventories);
    }

    /**
     * Register a Data Importer and optionally try to enable to it as well.
     *
     * @param dataImporter  The Data Importer to register.
     * @param tryEnable     Whether to try and {@link DataImporter#enable(Plugin)} the Data Importer.
     */
    public void register(DataImporter dataImporter, boolean tryEnable) {
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
    public void register(DataImporter dataImporter) {
        this.register(dataImporter, true);
    }

    /**
     * Gets a {@link DataImporter} based on an importable plugin name.
     *
     * @param pluginName The plugin name you want to import data from.
     * @return The {@link DataImporter} if Data Importer present for that plugin, else null.
     */
    public Option<DataImporter> getImporter(String pluginName) {
        return Option.of(this.dataImporters.get(pluginName.toLowerCase()));
    }

    /**
     * Gets a {@link DataImporter} based on an importable {@link Plugin}.
     *
     * @param plugin The plugin you want to import data from.
     * @return The {@link DataImporter} if Data Importer present for that plugin, else null.
     */
    public Option<DataImporter> getImporter(Plugin plugin) {
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

    /**
     * Called when a plugin is enabled.
     *
     * @param event The plugin enable event.
     */
    @EventHandler
    private void pluginEnable(PluginEnableEvent event) {
        getImporter(event.getPlugin()).peek(dataImporter -> dataImporter.enable(event.getPlugin()));
    }

    /**
     * Called when a plugin is disabled.
     *
     * @param event The plugin disable event.
     */
    @EventHandler
    private void pluginDisable(PluginDisableEvent event) {
        getImporter(event.getPlugin()).peek(DataImporter::disable);
    }
}
