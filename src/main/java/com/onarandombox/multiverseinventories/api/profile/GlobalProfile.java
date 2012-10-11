package com.onarandombox.multiverseinventories.api.profile;

import java.util.Map;

/**
 * The global profile for a player.  This is where Multiverse-Inventories stores meta-data for players.
 */
public interface GlobalProfile {

    /**
     * @return The name of the player.
     */
    String getName();

    /**
     * @return The last world the player was in.
     */
    String getWorld();

    /**
     * Says whether the player data for the player's logout world should be loaded when the player logs in.
     *
     * @return true if player data should be loaded when they log in.
     */
    boolean shouldLoadOnLogin();

    /**
     * Sets whether the player data for the player's logout world should be loaded when the player logs in.
     *
     * @param loadOnLogin true if player data should be loaded when they log in.
     */
    void setLoadOnLogin(boolean loadOnLogin);

    /**
     * Sets the last world the player was known to be in.  This is done automatically on world change.
     *
     * @param world The world the player is in.
     */
    void setWorld(String world);

    /**
     * @return A Map form of the data for easy serialization.
     */
    Map<String, Object> serialize();
}
