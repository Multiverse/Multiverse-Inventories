package com.onarandombox.multiverseinventories;

import com.onarandombox.multiverseinventories.api.profile.ProfileType;
import com.onarandombox.multiverseinventories.api.share.Sharables;
import com.onarandombox.multiverseinventories.api.share.Shares;
import com.onarandombox.multiverseinventories.util.Logging;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ProfileTypes {

    public static final ProfileType DEFAULT = new DefaultProfileType("default_profile", Sharables.allOf());

    public static final ProfileType GAME_MODE = new DefaultProfileType("game_mode_profile", Sharables.allOf());

    private static Map<String, ProfileType> profileTypeMap;

    static {
        resetProfileTypes();
    }

    static void resetProfileTypes() {
        profileTypeMap = new HashMap<String, ProfileType>();
        profileTypeMap.put(DEFAULT.getName(), DEFAULT);
    }

    static ProfileType registerProfileType(String name, Shares shares) {
        ProfileType type = new DefaultProfileType(name, shares);
        profileTypeMap.put(name, type);
        Logging.finest("Registered profile type '" + name + "' with shares '" + shares + "'");
        return type;
    }

    public static ProfileType lookupType(String name, boolean registerIfNone) {
        ProfileType type = profileTypeMap.get(name);
        if (type == null && registerIfNone) {
            type = registerProfileType(name, Sharables.allOf());
        }
        return type;
    }

    static Collection<ProfileType> getProfileTypes() {
        return profileTypeMap.values();
    }
}
