package com.onarandombox.multiverseinventories.migration;

import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.migration.multiinv.MultiInvImporter;
import com.onarandombox.multiverseinventories.migration.worldinventories.WorldInventoriesImporter;
import me.drayshak.WorldInventories.WorldInventories;
import uk.co.tggl.pluckerpluck.multiinv.MultiInv;

public class ImportManager {

    private MultiInvImporter multiInvImporter = null;
    private WorldInventoriesImporter worldInventoriesImporter = null;
    MultiverseInventories plugin;
    
    public ImportManager(MultiverseInventories plugin) {
        this.plugin = plugin;
    }

    /**
     * Hooks MultiInv for importing it's data.
     *
     * @param plugin Instance of MultiInv.
     */
    public void hookMultiInv(MultiInv plugin) {
        this.multiInvImporter = new MultiInvImporter(this.plugin, plugin);
        //this.getMultiInvImporter().getMIPlugin().
    }

    /**
     * Hooks WorldInventories for importing it's data.
     *
     * @param plugin Instance of WorldInventories.
     */
    public void hookWorldInventories(WorldInventories plugin) {
        this.worldInventoriesImporter = new WorldInventoriesImporter(this.plugin, plugin);
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
