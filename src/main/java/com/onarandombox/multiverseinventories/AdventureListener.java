package com.onarandombox.multiverseinventories;

import com.dumptruckman.minecraft.util.Logging;
import com.onarandombox.MultiverseAdventure.event.MVAResetFinishedEvent;
import com.onarandombox.multiverseinventories.profile.container.WorldProfileContainer;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Listener for Multiverse-Adventure events.
 */
public class AdventureListener implements Listener {

    private MultiverseInventories inventories;

    public AdventureListener(MultiverseInventories inventories) {
        this.inventories = inventories;
    }

    /**
     * @param event The Multiverse-Adventure event to handle when a world has finished resetting.
     */
    @EventHandler
    public void worldReset(MVAResetFinishedEvent event) {
        WorldProfileContainer container = inventories.getWorldManager().getWorldProfileContainer(event.getWorld());
        for (OfflinePlayer player : inventories.getServer().getOfflinePlayers()) {
            container.removeAllPlayerData(player);
        }
        Logging.info("Removed all inventories for Multiverse-Adventure world.");
    }
}

