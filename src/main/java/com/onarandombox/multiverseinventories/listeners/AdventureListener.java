package com.onarandombox.multiverseinventories.listeners;

import com.dumptruckman.minecraft.util.Logging;
import com.onarandombox.MultiverseAdventure.event.MVAResetFinishedEvent;
import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.profile.container.ProfileContainer;
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
        ProfileContainer container = inventories.getWorldProfileContainerStore().getContainer(event.getWorld());
        for (OfflinePlayer player : inventories.getServer().getOfflinePlayers()) {
            container.removeAllPlayerData(player);
        }
        Logging.info("Removed all inventories for Multiverse-Adventure world.");
    }
}

