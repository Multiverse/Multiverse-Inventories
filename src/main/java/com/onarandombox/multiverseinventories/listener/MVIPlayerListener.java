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
        Player player = event.getPlayer();
        World fromWorld = event.getFrom();
        World toWorld = player.getWorld();
        // A precaution..  Will this ever be true?
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

        List<WorldGroup> fromWorldGroups = MVIManager.getWorldGroups().get(fromWorld);
        for (WorldGroup fromWorldGroup : fromWorldGroups) {
            // persist current stats in world group
            if (!currentShares.isSharingInventory()) {

            }
            if (!currentShares.isSharingHealth()) {
                
            }
            if (!currentShares.isSharingHunger()) {

            }
            if (!currentShares.isSharingExp()) {

            }
            if (!currentShares.isSharingEffects()) {
                
            }
        }

        /*
        // Set player defaults
        if (!currentShares.isSharingInventory()) {
            player.getInventory().clear();
        }
        if (!currentShares.isSharingHealth()) {
            player.setHealth(20);
        }
        if (!currentShares.isSharingHunger()) {
            player.setFoodLevel(20);
            player.setExhaustion(0);
            player.setSaturation(0);
        }
        if (!currentShares.isSharingExp()) {
            player.setExp(0);
            player.setLevel(0);
        }
        if (!currentShares.isSharingEffects()) {
            
        }*/
    }
}
