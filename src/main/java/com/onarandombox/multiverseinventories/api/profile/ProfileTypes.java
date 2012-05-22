package com.onarandombox.multiverseinventories.api.profile;

import com.onarandombox.multiverseinventories.api.share.Sharables;
import com.onarandombox.multiverseinventories.api.share.Shares;
import com.onarandombox.multiverseinventories.util.Logging;

import java.util.HashMap;
import java.util.Map;

public class ProfileTypes {

    private static Map<String, ProfileType> profileTypeMap;

    static {
        resetProfileTypes();
    }

    public static void resetProfileTypes() {
        profileTypeMap = new HashMap<String, ProfileType>();
        profileTypeMap.put(ProfileType.DEFAULT.getName(), ProfileType.DEFAULT);
    }

    public static ProfileType registerProfileType(String name, Shares shares) {
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
}
