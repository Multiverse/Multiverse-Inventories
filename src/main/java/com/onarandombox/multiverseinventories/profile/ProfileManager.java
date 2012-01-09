package com.onarandombox.multiverseinventories.profile;

import com.onarandombox.multiverseinventories.profile.WorldProfile;

import java.util.List;

/**
 * @author dumptruckman, SwearWord
 */
public interface ProfileManager {

    public void addWorldProfile(WorldProfile worldProfile);

    public WorldProfile getWorldProfile(String worldName);

    public void setWorldProfiles(List<WorldProfile> worldProfiles);
}
