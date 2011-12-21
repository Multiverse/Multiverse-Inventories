package com.onarandombox.multiverseinventories.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerLoginEvent;

/**
 * @author dumptruckman
 */
public class MVIPlayerListener extends PlayerListener {

    public void onPlayerLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        
    }

    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        
    }
}
