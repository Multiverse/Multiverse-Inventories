package org.mvplugins.multiverse.inventories.profile;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.mvplugins.multiverse.inventories.profile.key.GlobalProfileKey;
import org.mvplugins.multiverse.inventories.util.DataStrings;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * The global profile for a player which contains meta-data for the player.
 */
public final class GlobalProfile {

    /**
     * Creates a global profile object for the given player with default values.
     *
     * @param key the player to create the profile object for.
     * @return a new GlobalProfile for the given player.
     */
    static GlobalProfile createGlobalProfile(GlobalProfileKey key) {
        return new GlobalProfile(key.getPlayerName(), key.getPlayerUUID());
    }

    /**
     * Creates a global profile object for the given player with default values.
     *
     * @param playerUUID the UUID of the player to create the profile for.
     * @param playerName the player to create the profile object for.
     * @return a new GlobalProfile for the given player.
     */
    static GlobalProfile createGlobalProfile(UUID playerUUID, String playerName) {
        return new GlobalProfile(playerName, playerUUID);
    }

    private final UUID uuid;
    private String lastWorld = null;
    private String lastKnownName;
    private boolean loadOnLogin = false;

    private GlobalProfile(String name, UUID uuid) {
        this.uuid = uuid;
        this.lastKnownName = name;
    }

    public GlobalProfile(UUID uuid, String lastWorld, String lastKnownName, boolean loadOnLogin) {
        this.uuid = uuid;
        this.lastWorld = lastWorld;
        this.lastKnownName = lastKnownName;
        this.loadOnLogin = loadOnLogin;
    }

    /**
     * Returns the name of the player.
     *
     * @return The name of the player.
     * @deprecated Use {@link #getPlayerUUID()} to uniquely identify a player.
     *             If you need player name, use {@link #getLastKnownName()}.
     */
    @Deprecated
    public String getPlayerName() {
        return this.lastKnownName;
    }

    /**
     * Returns the UUID of the player.
     *
     * @return the UUID of the player.
     */
    public UUID getPlayerUUID() {
        return uuid;
    }

    /**
     * Returns the last name the player was known to have.
     *
     * @return the last name the player was known to have.
     */
    public String getLastKnownName() {
        return lastKnownName;
    }

    /**
     * Sets the last name that the player was seen having.
     * <p>This should be updated when a player's name is changed through Mojang but only after their data has been
     * migrated to the new name.</p>
     *
     * @param lastKnownName the last known name for the player.
     */
    public void setLastKnownName(String lastKnownName) {
        this.lastKnownName = lastKnownName;
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
     * Sets the last world the player was known to be in. This is done automatically on world change.
     *
     * @param world The world the player is in.
     */
    public void setLastWorld(String world) {
        this.lastWorld = world;
    }

    @Override
    public String toString() {
        return "GlobalProfile{" +
                "uuid=" + uuid +
                ", lastWorld='" + lastWorld + '\'' +
                ", lastKnownName='" + lastKnownName + '\'' +
                ", loadOnLogin=" + loadOnLogin +
                '}';
    }

    /**
     * Converts a global profile to a map that can be serialized into the profile data file.
     *
     * @param profile    The global profile data.
     * @return The serialized profile map.
     */
    Map<String, Object> serialize(GlobalProfile profile) {
        Map<String, Object> result = new HashMap<>(3);
        if (profile.getLastWorld() != null) {
            result.put(DataStrings.PLAYER_LAST_WORLD, profile.getLastWorld());
        }
        result.put(DataStrings.PLAYER_SHOULD_LOAD, profile.shouldLoadOnLogin());
        result.put(DataStrings.PLAYER_LAST_KNOWN_NAME, profile.getLastKnownName());
        return result;
    }

    /**
     * Converts a configuration section to a global profile.
     *
     * @param playerName    The player name.
     * @param playerUUID    The player UUID.
     * @param data          The configuration section to convert.
     * @return The global profile.
     */
    static GlobalProfile deserialize(String playerName, UUID playerUUID, ConfigurationSection data) {
        return new GlobalProfile(
                playerUUID,
                data.getString(DataStrings.PLAYER_LAST_WORLD, null),
                data.getString(DataStrings.PLAYER_LAST_KNOWN_NAME, playerName),
                data.getBoolean(DataStrings.PLAYER_SHOULD_LOAD, false)
        );
    }
}
