package org.mvplugins.multiverse.inventories.destination;

import com.dumptruckman.minecraft.util.Logging;
import org.bukkit.command.CommandSender;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.core.destination.Destination;
import org.mvplugins.multiverse.core.destination.DestinationSuggestionPacket;
import org.mvplugins.multiverse.core.world.WorldManager;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.external.jetbrains.annotations.Nullable;
import org.mvplugins.multiverse.inventories.profile.container.ProfileContainerStoreProvider;
import org.mvplugins.multiverse.inventories.profile.group.WorldGroupManager;

import java.util.Collection;

@Service
public final class LastLocationDestination implements Destination<LastLocationDestination, LastLocationDestinationInstance> {

    private final WorldManager worldManager;
    private final WorldGroupManager worldGroupManager;
    private final ProfileContainerStoreProvider profileContainerStoreProvider;

    @Inject
    LastLocationDestination(
            @NotNull WorldManager worldManager,
            @NotNull WorldGroupManager worldGroupManager,
            @NotNull ProfileContainerStoreProvider profileContainerStoreProvider) {
        this.worldManager = worldManager;
        this.worldGroupManager = worldGroupManager;
        this.profileContainerStoreProvider = profileContainerStoreProvider;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "ll";
    }

    @Override
    public LastLocationDestinationInstance getDestinationInstance(@Nullable String destinationParams) {
        try {
            Logging.warning("LastLocationDestination: destinationParams: " + destinationParams);
            String worldName = destinationParams;
            if (!worldManager.isLoadedWorld(worldName)) {
                return null;
            }
            return new LastLocationDestinationInstance(this, worldGroupManager, profileContainerStoreProvider, worldName);
        } catch (Exception e) {
            Logging.severe(e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public @NotNull Collection<DestinationSuggestionPacket> suggestDestinations(@NotNull CommandSender commandSender, @Nullable String destinationParams) {
        return worldManager.getLoadedWorlds().stream()
                .map(world -> new DestinationSuggestionPacket(world.getName(), world.getName()))
                .toList();
    }
}
