package org.mvplugins.multiverse.inventories.destination;

import com.dumptruckman.minecraft.util.Logging;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.units.qual.N;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.core.destination.Destination;
import org.mvplugins.multiverse.core.destination.DestinationSuggestionPacket;
import org.mvplugins.multiverse.core.locale.MVCorei18n;
import org.mvplugins.multiverse.core.utils.result.Attempt;
import org.mvplugins.multiverse.core.utils.result.FailureReason;
import org.mvplugins.multiverse.core.world.WorldManager;
import org.mvplugins.multiverse.external.acf.locales.MessageKey;
import org.mvplugins.multiverse.external.acf.locales.MessageKeyProvider;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.external.jetbrains.annotations.Nullable;
import org.mvplugins.multiverse.inventories.profile.container.ProfileContainerStoreProvider;
import org.mvplugins.multiverse.inventories.profile.group.WorldGroupManager;

import java.util.Collection;

@Service
public final class LastLocationDestination implements Destination<LastLocationDestination, LastLocationDestinationInstance, LastLocationDestination.InstanceFailureReason> {

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
    public @NotNull Attempt<LastLocationDestinationInstance, LastLocationDestination.InstanceFailureReason> getDestinationInstance(@NotNull String destinationParams) {
        if (!worldManager.isLoadedWorld(destinationParams)) {
            return Attempt.failure(InstanceFailureReason.WORLD_NOT_FOUND);
        }
        return Attempt.success(new LastLocationDestinationInstance(this, worldGroupManager, profileContainerStoreProvider, destinationParams));
    }

    @Override
    public @NotNull Collection<DestinationSuggestionPacket> suggestDestinations(@NotNull CommandSender commandSender, @Nullable String destinationParams) {
        return worldManager.getLoadedWorlds().stream()
                .map(world -> new DestinationSuggestionPacket(this, world.getName(), world.getName()))
                .toList();
    }

    public enum InstanceFailureReason implements FailureReason {
        WORLD_NOT_FOUND(MVCorei18n.DESTINATION_SHARED_FAILUREREASON_WORLDNOTFOUND),
        ;

        private final MessageKeyProvider messageKey;

        InstanceFailureReason(MessageKeyProvider message) {
            this.messageKey = message;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public MessageKey getMessageKey() {
            return messageKey.getMessageKey();
        }
    }
}
