package com.onarandombox.multiverseinventories.migration.worldinventories;

import com.onarandombox.multiverseinventories.MultiverseInventories;
import me.drayshak.WorldInventories.WorldInventories;

public class WorldInventoriesImporter {

    private WorldInventories wiPlugin;
    private MultiverseInventories plugin;

    public WorldInventoriesImporter(MultiverseInventories plugin, WorldInventories wiPlugin) {
        this.plugin = plugin;
        this.wiPlugin = wiPlugin;
    }

    /**
     * @return The WorldInventories plugin hooked to the importer.
     */
    public WorldInventories getWIPlugin() {
        return this.wiPlugin;
    }
}
