package com.onarandombox.multiverseinventories.migration;

import org.bukkit.plugin.Plugin;

/**
 * Interface for data migration importers.
 */
public interface DataImporter {

    /**
     * Imports the data from another plugin.
     *
     * @throws MigrationException If there was any MAJOR issue loading the data.
     */
    void importData() throws MigrationException;

    /**
     * @return The plugin associated with this Importer.
     */
    Plugin getPlugin();
}

