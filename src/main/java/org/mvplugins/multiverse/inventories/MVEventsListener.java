package org.mvplugins.multiverse.inventories;

import com.dumptruckman.minecraft.util.Logging;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.core.event.MVConfigReloadEvent;
import org.mvplugins.multiverse.core.event.MVDebugModeEvent;
import org.mvplugins.multiverse.core.event.MVDumpsDebugInfoEvent;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.inventories.config.InventoriesConfig;
import org.mvplugins.multiverse.inventories.profile.group.WorldGroup;
import org.mvplugins.multiverse.inventories.profile.group.WorldGroupManager;

import java.io.File;

@Service
final class MVEventsListener implements Listener {

    private final MultiverseInventories inventories;
    private final InventoriesConfig config;
    private final WorldGroupManager worldGroupManager;

    @Inject
    MVEventsListener(@NotNull MultiverseInventories inventories, @NotNull InventoriesConfig config, @NotNull WorldGroupManager worldGroupManager) {
        this.inventories = inventories;
        this.config = config;
        this.worldGroupManager = worldGroupManager;
    }

    /**
     * Adds Multiverse-Inventories version info to /mv version.
     *
     * @param event The MVVersionEvent that this plugin will listen for.
     */
    @EventHandler
    void dumpsDebugInfoRequest(MVDumpsDebugInfoEvent event) {
        event.appendDebugInfo(getDebugInfo());
        File configFile = new File(this.inventories.getDataFolder(), "config.yml");
        File groupsFile = new File(this.inventories.getDataFolder(), "groups.yml");
        event.putDetailedDebugInfo("multiverse-inventories/config.yml", configFile);
        event.putDetailedDebugInfo("multiverse-inventories/groups.yml", groupsFile);
    }

    /**
     * Builds a String containing Multiverse-Inventories' version info.
     *
     * @return The version info.
     */
    private String getDebugInfo() {
        StringBuilder versionInfo = new StringBuilder("[Multiverse-Inventories] Multiverse-Inventories Version: " + inventories.getDescription().getVersion() + '\n'
                + "[Multiverse-Inventories] === Settings ===" + '\n'
                + "[Multiverse-Inventories] First Run: " + config.getFirstRun() + '\n'
                + "[Multiverse-Inventories] Using Bypass: " + config.getEnableBypassPermissions() + '\n'
                + "[Multiverse-Inventories] Default Ungrouped Worlds: " + config.getDefaultUngroupedWorlds() + '\n'
                + "[Multiverse-Inventories] Save and Load on Log In and Out: " + config.getSavePlayerdataOnQuit() + '\n'
                + "[Multiverse-Inventories] Using GameMode Profiles: " + config.getEnableGamemodeShareHandling() + '\n'
                + "[Multiverse-Inventories] === Shares ===" + '\n'
                + "[Multiverse-Inventories] Optionals for Ungrouped Worlds: " + config.getUseOptionalsForUngroupedWorlds() + '\n'
                + "[Multiverse-Inventories] Enabled Optionals: " + config.getActiveOptionalShares() + '\n'
                + "[Multiverse-Inventories] === Groups ===" + '\n');

        for (WorldGroup group : worldGroupManager.getGroups()) {
            versionInfo.append("[Multiverse-Inventories] ").append(group.toString()).append('\n');
        }

        return versionInfo.toString();
    }

    @EventHandler
    void onDebugModeChange(MVDebugModeEvent event) {
        Logging.setDebugLevel(event.getLevel());
    }

    /**
     * Hooks Multiverse-Inventories into the Multiverse reload command.
     *
     * @param event The MVConfigReloadEvent that this plugin will listen for.
     */
    @EventHandler
    void configReload(MVConfigReloadEvent event) {
        this.inventories.reloadConfig();
        event.addConfig("Multiverse-Inventories - config.yml");
    }
}
