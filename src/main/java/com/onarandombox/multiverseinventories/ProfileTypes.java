package com.onarandombox.multiverseinventories;

import com.onarandombox.multiverseinventories.api.profile.ProfileType;
import com.onarandombox.multiverseinventories.api.share.Sharables;
import com.onarandombox.multiverseinventories.api.share.Shares;
import com.onarandombox.multiverseinventories.util.Logging;
import org.bukkit.GameMode;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Static class for profile type lookup and protected registration.
 */
public final class ProfileTypes {

    /**
     * The profile type for the SURVIVAL Game Mode.
     */
    public static final ProfileType SURVIVAL = new DefaultProfileType("SURVIVAL", Sharables.allOf());

    /**
     * The profile type for the CREATIVE Game Mode.
     */
    public static final ProfileType CREATIVE = new DefaultProfileType("CREATIVE", Sharables.allOf());

    /**
     * The profile type for the ADVENTURE Game Mode.
     */
    public static final ProfileType ADVENTURE = new DefaultProfileType("ADVENTURE", Sharables.allOf());

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
        profileTypeMap.put(name.toLowerCase(), type);
        Logging.finest("Registered profile type '" + name + "' with shares '" + shares + "'");
        return type;
    }

    /**
     * Looks up a profile type and optionally creates it if it doesn't exist.
     *
     * @param name           The name of the profile type to look up.
     * @param registerIfNone Registers the profile type if it doesn't exist.
     * @return The profile by the given name or null if non-existent and uncreated.
     */
    public static ProfileType lookupType(String name, boolean registerIfNone) {
        ProfileType type = profileTypeMap.get(name.toLowerCase());
        if (type == null && registerIfNone) {
            type = registerProfileType(name, Sharables.allOf());
        }
        return type;
    }

    /**
     * Returns the appropriate ProfileType for the given game mode.
     *
     * @param mode The game mode to get the profile type for.
     * @return The profile type for the given game mode.
     */
    public static ProfileType forGameMode(GameMode mode) {
        switch (mode) {
            case SURVIVAL:
                return SURVIVAL;
            case CREATIVE:
                return CREATIVE;
            case ADVENTURE:
                return ADVENTURE;
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
