package com.onarandombox.multiverseinventories.profile;

/**
 * The global profile for a player which contains meta-data for the player.
 */
public final class GlobalProfile {

    /**
     * Creates a global profile object for the given player with default values.
     *
     * @param playerName the player to create the profile object for.
     * @return a new GlobalProfile for the given player.
     */
    public static GlobalProfile createGlobalProfile(String playerName) {
        return new GlobalProfile(playerName);
    }

    private final String name;
    private String lastWorld = null;
    private boolean loadOnLogin = false;

    private GlobalProfile(String name) {
        this.name = name;
    }

    /**
     * Returns the name of the player.
     *
     * @return The name of the player.
     */
    public String getPlayerName() {
        return this.name;
    }

    /**
     * Returns the name of last world the player was in.
     *
     * @return The last world the player was in or null if not set.
     */
    public String getLastWorld() {
        return this.lastWorld;
    }

    /**
     * Says whether the player data for the player's logout world should be loaded when the player logs in.
     * The default value is false.
     *
     * @return true if player data should be loaded when they log in.
     */
    public boolean shouldLoadOnLogin() {
        return loadOnLogin;
    }

    /**
     * Sets whether the player data for the player's logout world should be loaded when the player logs in.
     *
     * @param loadOnLogin true if player data should be loaded when they log in.
     */
    public void setLoadOnLogin(boolean loadOnLogin) {
        this.loadOnLogin = loadOnLogin;
    }

    /**
     * Sets the last world the player was known to be in.  This is done automatically on world change.
     *
     * @param world The world the player is in.
     */
    public void setLastWorld(String world) {
        this.lastWorld = world;
    }
}
