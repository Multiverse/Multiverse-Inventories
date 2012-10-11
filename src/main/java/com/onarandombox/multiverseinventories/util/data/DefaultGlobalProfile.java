package com.onarandombox.multiverseinventories.util.data;

import com.onarandombox.multiverseinventories.api.DataStrings;
import com.onarandombox.multiverseinventories.api.profile.GlobalProfile;

import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation of global player profiles.
 */
class DefaultGlobalProfile implements GlobalProfile {

    private final String name;
    private String lastWorld = null;
    private boolean loadOnLogin = false;
    //private ProfileType profileType = ProfileTypes.SURVIVAL;

    DefaultGlobalProfile(String playerName, Map<String, Object> playerData) {
        this.name = playerName;
        for (String key : playerData.keySet()) {
            if (key.equalsIgnoreCase(DataStrings.PLAYER_LAST_WORLD)) {
                this.lastWorld = playerData.get(key).toString();
            } else if (key.equalsIgnoreCase(DataStrings.PLAYER_SHOULD_LOAD)) {
                this.loadOnLogin = Boolean.valueOf(playerData.get(key).toString());
            }
        }
    }

    @Override
    public String getName() {
        return this.name;
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
