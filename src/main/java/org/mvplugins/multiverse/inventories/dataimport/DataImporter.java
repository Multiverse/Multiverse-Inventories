package org.mvplugins.multiverse.inventories.dataimport;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jvnet.hk2.annotations.Contract;

/**
 * Interface for data migration importers.
 */
@Contract
public interface DataImporter {

    /**
     * Imports the data from another plugin and optionally disable it after successful import.
     *
     * @param disableOnSuccess  Whether to disable the importer plugin after a successful import.
     * @return True if data import is successful, else false.
     */
    boolean importData(boolean disableOnSuccess);

    /**
     * Imports the data from another plugin and disabled it upon success so Multiverse inventories
     * can work without conflicts.
     *
     * @return True if data import is successful, else false.
     */
    boolean importData();

    /**
     * Hooks plugin for importing its data. Needs plugin class of {@link #getPluginClass()}.
     *
     * @param plugin    The target plugin instance to hook.
     * @return True if successfully enabled, else false.
     */
    boolean enable(Plugin plugin);

    /**
     * Hooks plugin for importing its data. Needs plugin class of {@link #getPluginClass()}.
     *
     * @return True if successfully enabled, else false.
     */
    boolean enable();

    /**
     * Unhook plugin from this Data Importer.
     *
     * @return True if successfully disabled, else false.
     */
    boolean disable();

    /**
     * Checks if this Data Importer has been {@link #enable(Plugin)} successfully.
     *
     * @return True if is enabled, else false.
     */
    boolean isEnabled();

    /**
     * @return The plugin associated with this Data Importer, null if not enabled.
     */
    @Nullable Plugin getPlugin();

    /**
     * @return The plugin name associated with this Data Importer.
     */
    @NotNull String getPluginName();

    /**
     * @return The plugin class associated with this Data Importer.
     */
    @NotNull Class<? extends Plugin> getPluginClass();
}
