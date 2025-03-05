package org.mvplugins.multiverse.inventories.destination;

import com.dumptruckman.minecraft.util.Logging;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.mvplugins.multiverse.core.destination.DestinationInstance;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.external.vavr.control.Option;
import org.mvplugins.multiverse.inventories.profile.container.ContainerType;
import org.mvplugins.multiverse.inventories.profile.container.ProfileContainerStoreProvider;
import org.mvplugins.multiverse.inventories.profile.group.WorldGroupManager;
import org.mvplugins.multiverse.inventories.share.Sharables;

public final class LastLocationDestinationInstance extends DestinationInstance<LastLocationDestinationInstance, LastLocationDestination> {

    private final WorldGroupManager worldGroupManager;
    private final ProfileContainerStoreProvider profileContainerStoreProvider;
    private final String worldName;

    LastLocationDestinationInstance(
            @NotNull LastLocationDestination destination,
            @NotNull WorldGroupManager worldGroupManager,
            @NotNull ProfileContainerStoreProvider profileContainerStoreProvider,
            @NotNull String worldName) {
        super(destination);
        this.worldGroupManager = worldGroupManager;
        this.profileContainerStoreProvider = profileContainerStoreProvider;
        this.worldName = worldName;
    }

    @Override
    public @NotNull Option<Location> getLocation(@NotNull Entity teleportee) {
        Logging.warning("LastLocationDestination: teleportee: " + teleportee);
        if (!(teleportee instanceof Player player)) {
            return Option.none();
        }

        var playerWorld = player.getWorld().getName();
        if (playerWorld.equals(worldName)) {
            return Option.none();
        }

        for (var group : worldGroupManager.getGroupsForWorld(worldName)) {
            Logging.warning("LastLocationDestination: group: " + group);
            if (!group.containsWorld(playerWorld) && group.getApplicableShares().contains(Sharables.LAST_LOCATION)) {
                return Option.of(profileContainerStoreProvider.getStore(ContainerType.GROUP)
                        .getContainer(group.getName())
                        .getPlayerDataNow(player)
                        .get(Sharables.LAST_LOCATION));
            }
        }

        // Means last location isn't shared by any group, and will be read directly for world profile
        return Option.of(profileContainerStoreProvider.getStore(ContainerType.WORLD)
                .getContainer(worldName)
                .getPlayerDataNow(player)
                .get(Sharables.LAST_LOCATION));
    }

    @Override
    public @NotNull Option<Vector> getVelocity(@NotNull Entity teleportee) {
        return Option.none();
    }

    @Override
    public boolean checkTeleportSafety() {
        return false;
    }

    @Override
    public @NotNull Option<String> getFinerPermissionSuffix() {
        return Option.of(worldName);
    }

    @Override
    protected @NotNull String serialise() {
        return worldName;
    }
}
