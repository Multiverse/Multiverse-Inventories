package com.onarandombox.multiverseinventories;

import com.onarandombox.MultiverseAdventure.event.MVAResetFinishedEvent;
import com.onarandombox.multiverseinventories.api.Inventories;
import com.onarandombox.multiverseinventories.api.profile.WorldProfile;
import com.onarandombox.multiverseinventories.util.Logging;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Listener for Multiverse-Adventure events.
 */
public class AdventureListener implements Listener {

    private Inventories inventories;

    public AdventureListener(Inventories inventories) {
        this.inventories = inventories;
    }

    /**
     * @param event The Multiverse-Adventure event to handle when a world has finished resetting.
     */
    @EventHandler
    public void worldReset(MVAResetFinishedEvent event) {
        WorldProfile worldProfile = inventories.getWorldManager().getWorldProfile(event.getWorld());
        for (OfflinePlayer player : inventories.getServer().getOfflinePlayers()) {
            worldProfile.removeAllPlayerData(player);
        }
        Logging.info("Removed all inventories for Multiverse-Adventure world.");
    }
}

