package com.onarandombox.multiverseinventories.util.data;

import com.onarandombox.multiverseinventories.api.DataStrings;
import com.onarandombox.multiverseinventories.api.profile.GlobalProfile;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Default implementation of global player profiles.
 */
class DefaultGlobalProfile implements GlobalProfile {

    private final String playerName;
    private final UUID playerUUID;
    private String lastWorld = null;
    private boolean loadOnLogin = false;
    //private ProfileType profileType = ProfileTypes.SURVIVAL;
    @Deprecated
    DefaultGlobalProfile(String playerName, Map<String, Object> playerData) {
        this.playerName = playerName;
        this.playerUUID = null;
        for (String key : playerData.keySet()) {
            if (key.equalsIgnoreCase(DataStrings.PLAYER_LAST_WORLD)) {
                this.lastWorld = playerData.get(key).toString();
            } else if (key.equalsIgnoreCase(DataStrings.PLAYER_SHOULD_LOAD)) {
                this.loadOnLogin = Boolean.valueOf(playerData.get(key).toString());
            }
        }
    }
    DefaultGlobalProfile(UUID playerUUID, Map<String, Object> playerData) {
        this.playerUUID = playerUUID;
        this.playerName = null;
        for (String key : playerData.keySet()) {
            if (key.equalsIgnoreCase(DataStrings.PLAYER_LAST_WORLD)) {
                this.lastWorld = playerData.get(key).toString();
            } else if (key.equalsIgnoreCase(DataStrings.PLAYER_SHOULD_LOAD)) {
                this.loadOnLogin = Boolean.valueOf(playerData.get(key).toString());
            }
        }
    }

    @Deprecated
    @Override
    public String getName() {
        return this.playerName;
    }

    @Override
    public UUID getUUID() {
        return this.playerUUID;
    }

    @Override
    public String getWorld() {
        return this.lastWorld;
    }

    @Override
    public void setWorld(String world) {
        this.lastWorld = world;
    }

    @Override
    public boolean shouldLoadOnLogin() {
        return loadOnLogin;
    }

    @Override
    public void setLoadOnLogin(boolean loadOnLogin) {
        this.loadOnLogin = loadOnLogin;
    }

    /*
    @Override
    public ProfileType getProfileType() {
        return this.profileType;
    }

    public void setProfileType(ProfileType type) {
        this.profileType = type;
    }
    */

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new HashMap<String, Object>(2);
        if (this.lastWorld != null) {
            result.put(DataStrings.PLAYER_LAST_WORLD, this.lastWorld);
        }
        result.put(DataStrings.PLAYER_SHOULD_LOAD, this.loadOnLogin);
        //result.put(DataStrings.PLAYER_PROFILE_TYPE, this.profileType.getName());
        return result;
    }
}
