package com.onarandombox.multiverseinventories.api.profile;

import com.onarandombox.multiverseinventories.share.Sharable;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

/**
 * Interface for a PlayerProfile which controls all the world specific data for a player.
 */
public interface PlayerProfile {

    /**
     * @return A map containing all the player data to be saved to disk.
     */
    Map<String, Object> serialize();

    /**
     * @return The type of player.
     */
    ProfileType getType();

    /**
     * @return the Player associated with this player.
     */
    OfflinePlayer getPlayer();
    
    <T> T get(Sharable<T> sharable);
    
    <T> void set(Sharable<T> sharable, T value);
}

