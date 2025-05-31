package org.mvplugins.multiverse.inventories.destination;

import com.dumptruckman.minecraft.util.Logging;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.mvplugins.multiverse.core.destination.DestinationInstance;
import org.mvplugins.multiverse.core.world.MultiverseWorld;
import org.mvplugins.multiverse.core.world.WorldManager;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.external.vavr.control.Option;
import org.mvplugins.multiverse.inventories.profile.key.ContainerType;
import org.mvplugins.multiverse.inventories.profile.container.ProfileContainerStoreProvider;
import org.mvplugins.multiverse.inventories.profile.group.WorldGroupManager;
import org.mvplugins.multiverse.inventories.share.Sharables;

public final class LastLocationDestinationInstance extends DestinationInstance<LastLocationDestinationInstance, LastLocationDestination> {

    private final WorldManager worldManager;
    private final WorldGroupManager worldGroupManager;
    private final ProfileContainerStoreProvider profileContainerStoreProvider;
    private final String worldName;

    LastLocationDestinationInstance(
            @NotNull LastLocationDestination destination,
            @NotNull WorldManager worldManager,
            @NotNull WorldGroupManager worldGroupManager,
            @NotNull ProfileContainerStoreProvider profileContainerStoreProvider,
            @NotNull String worldName) {
        super(destination);
        this.worldManager = worldManager;
        this.worldGroupManager = worldGroupManager;
        this.profileContainerStoreProvider = profileContainerStoreProvider;
        this.worldName = worldName;
    }

    @Override
    public @NotNull Option<Location> getLocation(@NotNull Entity teleportee) {
        Logging.finer("LastLocationDestination: teleportee: " + teleportee);
        if (!(teleportee instanceof Player player)) {
            return worldManager.getLoadedWorld(worldName).map(MultiverseWorld::getSpawnLocation);
        }

        var playerWorld = player.getWorld().getName();
        if (playerWorld.equals(worldName)) {
            return worldManager.getLoadedWorld(worldName).map(MultiverseWorld::getSpawnLocation);
        }

        for (var group : worldGroupManager.getGroupsForWorld(worldName)) {
            Logging.finer("LastLocationDestination: group: " + group);
            if (!group.containsWorld(playerWorld) && group.getApplicableShares().contains(Sharables.LAST_LOCATION)) {
                return Option.of(profileContainerStoreProvider.getStore(ContainerType.GROUP)
                        .getContainer(group.getName())
                        .getPlayerProfileNow(player)
                        .get(Sharables.LAST_LOCATION))
                        .orElse(() -> worldManager.getLoadedWorld(worldName).map(MultiverseWorld::getSpawnLocation));
            }
        }

        // Means last location isn't shared by any group, and will be read directly for world profile
        return Option.of(profileContainerStoreProvider.getStore(ContainerType.WORLD)
                .getContainer(worldName)
                .getPlayerProfileNow(player)
                .get(Sharables.LAST_LOCATION))
                .orElse(() -> worldManager.getLoadedWorld(worldName).map(MultiverseWorld::getSpawnLocation));
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
