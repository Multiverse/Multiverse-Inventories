package org.mvplugins.multiverse.inventories.profile;

import org.bukkit.GameMode;

/**
 * Static class for profile type lookup and protected registration.
 */
public final class ProfileTypes {

    /**
     * The profile type for the SURVIVAL Game Mode.
     */
    public static final ProfileType SURVIVAL = ProfileType.createProfileType("SURVIVAL");

    /**
     * The profile type for the CREATIVE Game Mode.
     */
    public static final ProfileType CREATIVE = ProfileType.createProfileType("CREATIVE");

    /**
     * The profile type for the ADVENTURE Game Mode.
     */
    public static final ProfileType ADVENTURE = ProfileType.createProfileType("ADVENTURE");

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

    private ProfileTypes() {
        throw new AssertionError();
    }
}
