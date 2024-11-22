package org.mvplugins.multiverse.inventories.migration;

import com.dumptruckman.minecraft.util.Logging;
import org.mvplugins.multiverse.inventories.MultiverseInventories;
import org.mvplugins.multiverse.inventories.migration.multiinv.MultiInvImporter;
import org.mvplugins.multiverse.inventories.migration.worldinventories.WorldInventoriesImporter;
import me.drayshak.WorldInventories.WorldInventories;
import uk.co.tggl.pluckerpluck.multiinv.MultiInv;

/**
 * Manages the import heplers for other similar plugins.
 */
public class ImportManager {

    private MultiInvImporter multiInvImporter = null;
    private WorldInventoriesImporter worldInventoriesImporter = null;
    private MultiverseInventories inventories;

    public ImportManager(MultiverseInventories inventories) {
        this.inventories = inventories;
    }

    /**
     * Hooks MultiInv for importing it's data.
     *
     * @param plugin Instance of MultiInv.
     */
    public void hookMultiInv(MultiInv plugin) {
        this.multiInvImporter = new MultiInvImporter(this.inventories, plugin);
        Logging.info("Hooked MultiInv for importing!");
    }

    /**
     * Hooks WorldInventories for importing it's data.
     *
     * @param plugin Instance of WorldInventories.
     */
    public void hookWorldInventories(WorldInventories plugin) {
        this.worldInventoriesImporter = new WorldInventoriesImporter(this.inventories, plugin);
        Logging.info("Hooked WorldInventories for importing!");
    }

    /**
     * @return The MultiInv importer class or null if not hooked.
     */
    public MultiInvImporter getMultiInvImporter() {
        return this.multiInvImporter;
    }

    /**
     * @return The WorldInventories importer class or null if not hooked.
     */
    public WorldInventoriesImporter getWorldInventoriesImporter() {
        return this.worldInventoriesImporter;
    }

    /**
     * Un-hooks MultiInv so we're not able to import from it anymore.
     */
    public void unHookMultiInv() {
        this.multiInvImporter = null;
    }

    /**
     * Un-hooks WorldInventories so we're not able to import from it anymore.
     */
    public void unHookWorldInventories() {
        this.worldInventoriesImporter = null;
    }
}

