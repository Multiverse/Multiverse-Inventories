package com.onarandombox.multiverseinventories;

import com.onarandombox.multiverseinventories.api.profile.ProfileType;
import com.onarandombox.multiverseinventories.api.share.Sharables;
import com.onarandombox.multiverseinventories.api.share.Shares;
import com.onarandombox.multiverseinventories.util.Logging;
import org.bukkit.GameMode;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class ProfileTypes {

    public static final ProfileType SURVIVAL = new DefaultProfileType("SURVIVAL", Sharables.allOf());

    public static final ProfileType CREATIVE = new DefaultProfileType("CREATIVE", Sharables.allOf());

    private static Map<String, ProfileType> profileTypeMap;

    static {
        resetProfileTypes();
    }

    static void resetProfileTypes() {
        profileTypeMap = new HashMap<String, ProfileType>();
        profileTypeMap.put(SURVIVAL.getName(), SURVIVAL);
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

    public static ProfileType forGameMode(GameMode mode) {
        switch (mode) {
            case SURVIVAL:
                return SURVIVAL;
            case CREATIVE:
                return CREATIVE;
            default:
                return SURVIVAL;
        }
    }

    static Collection<ProfileType> getProfileTypes() {
        return profileTypeMap.values();
    }

    private ProfileTypes() {
        throw new AssertionError();
    }
}
