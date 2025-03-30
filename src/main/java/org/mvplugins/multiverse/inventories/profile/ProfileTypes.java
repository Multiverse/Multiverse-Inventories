package org.mvplugins.multiverse.inventories.profile;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.mvplugins.multiverse.inventories.MultiverseInventories;
import org.mvplugins.multiverse.inventories.config.InventoriesConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Static class for profile type lookup and protected registration.
 */
public final class ProfileTypes {

    private static final List<ProfileType> types = new ArrayList<>();
    private static InventoriesConfig config;

    public static void init(MultiverseInventories plugin) {
        config = plugin.getServiceLocator().getService(InventoriesConfig.class);
    }

    private static ProfileType createProfileType(String name) {
        ProfileType type = ProfileType.createProfileType(name);
        types.add(type);
        return type;
    }

    public static List<ProfileType> getTypes() {
        return types;
    }

    /**
     * The profile type for the SURVIVAL Game Mode.
     */
    public static final ProfileType SURVIVAL = createProfileType("SURVIVAL");

    /**
     * The profile type for the CREATIVE Game Mode.
     */
    public static final ProfileType CREATIVE = createProfileType("CREATIVE");

    /**
     * The profile type for the ADVENTURE Game Mode.
     */
    public static final ProfileType ADVENTURE = createProfileType("ADVENTURE");

    /**
     * The profile type for the SPECTATOR Game Mode.
     */
    public static final ProfileType SPECTATOR = createProfileType("SPECTATOR");

    public static ProfileType forPlayer(Player player) {
        if (config != null && config.getEnableGamemodeShareHandling()) {
            return forGameMode(player.getGameMode());
        }
        return SURVIVAL;
    }

    /**
     * Returns the appropriate ProfileType for the given game mode.
     *
     * @param mode The game mode to get the profile type for.
     * @return The profile type for the given game mode.
     */
    public static ProfileType forGameMode(GameMode mode) {
        return switch (mode) {
            case SURVIVAL -> SURVIVAL;
            case CREATIVE -> CREATIVE;
            case ADVENTURE -> ADVENTURE;
            case SPECTATOR -> SPECTATOR;
            default -> SURVIVAL;
        };
    }

    private ProfileTypes() {
        throw new AssertionError();
    }
}
