package org.mvplugins.multiverse.inventories.profile.key;

import com.google.common.collect.Sets;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.mvplugins.multiverse.external.vavr.control.Option;
import org.mvplugins.multiverse.inventories.MultiverseInventories;
import org.mvplugins.multiverse.inventories.config.InventoriesConfig;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Static class for profile type lookup and protected registration.
 */
public final class ProfileTypes {

    private static final Set<ProfileType> types = new HashSet<>();
    private static InventoriesConfig config;

    public static void init(MultiverseInventories plugin) {
        config = plugin.getServiceLocator().getService(InventoriesConfig.class);
    }

    private static ProfileType createProfileType(String name) {
        ProfileType type = ProfileType.createProfileType(name);
        types.add(type);
        return type;
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

    public static Collection<ProfileType> getTypes() {
        return types;
    }

    public static Collection<ProfileType> getApplicableTypes() {
        if (config != null && config.getEnableGamemodeShareHandling()) {
            return types;
        }
        return List.of(getDefault());
    }

    public static ProfileType getDefault() {
        return SURVIVAL;
    }

    public static ProfileType forPlayer(Player player) {
        if (config != null && config.getEnableGamemodeShareHandling()) {
            return forGameMode(player.getGameMode());
        }
        return getDefault();
    }

    public static Option<ProfileType> forName(String name) {
        return Option.ofOptional(types.stream()
                .filter(type -> type.getName().equalsIgnoreCase(name))
                .findFirst());
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

    public static boolean isAll(ProfileType[] otherTypes) {
        return Set.of(otherTypes).equals(types);
    }

    private ProfileTypes() {
        throw new IllegalStateException();
    }
}
