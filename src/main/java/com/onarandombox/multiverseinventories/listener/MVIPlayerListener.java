package com.onarandombox.multiverseinventories.listener;

import com.onarandombox.multiverseinventories.MVIManager;
import com.onarandombox.multiverseinventories.data.Shares;
import com.onarandombox.multiverseinventories.data.WorldGroup;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerLoginEvent;

import java.util.List;

/**
 * @author dumptruckman
 */
public class MVIPlayerListener extends PlayerListener {

    public void onPlayerLogin(PlayerLoginEvent event) {
    }

    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        World fromWorld = event.getFrom();
        World toWorld = event.getPlayer().getWorld();
        // A precaution..
        if (fromWorld.equals(toWorld)) {
            return;
        }

        Shares currentShares = new Shares();
        List<WorldGroup> toWorldGroups = MVIManager.getWorldGroups().get(toWorld);
        for (WorldGroup toWorldGroup : toWorldGroups) {
            if (toWorldGroup.getWorlds().contains(fromWorld)) {
                currentShares.mergeShares(toWorldGroup.getShares());
            }
        }
        currentShares.fillNullsWithDefaults();

        List<WorldGroup> fromWorldGroups = MVIManager.getWorldGroups().get(fromWorld);
        for (WorldGroup fromWorldGroup : fromWorldGroups) {
            if (!currentShares.isSharingInventory()) {
                // persist inventory in world group
            }
        }

    }
}
