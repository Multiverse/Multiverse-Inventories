package org.mvplugins.multiverse.inventories;

import com.dumptruckman.minecraft.util.Logging;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.context.ContextCalculator;
import net.luckperms.api.context.ContextConsumer;
import net.luckperms.api.context.ContextSet;
import net.luckperms.api.context.ImmutableContextSet;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.external.jakarta.annotation.PostConstruct;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.vavr.control.Try;
import org.mvplugins.multiverse.inventories.profile.group.WorldGroupManager;

@Service
final class WorldGroupContextCalculator implements ContextCalculator<Player> {

    private static final String WORLD_GROUP_CONTEXT_KEY = "mvinv:worldgroup";
    private final WorldGroupManager worldGroupManager;

    @Inject
    WorldGroupContextCalculator(WorldGroupManager worldGroupManager) {
        this.worldGroupManager = worldGroupManager;
    }

    @PostConstruct
    private void registerCalculator() {
        Try.of(LuckPermsProvider::get)
                .peek(luckPerms -> luckPerms.getContextManager().registerCalculator(this))
                .onFailure(e -> Logging.warning("Failed to hook LuckPerms! %s", e.getMessage()));
    }

    @Override
    public void calculate(@NotNull Player player, @NotNull ContextConsumer contextConsumer) {
        ImmutableContextSet.Builder contextBuilder = ImmutableContextSet.builder();
        worldGroupManager.getGroupsForWorld(player.getWorld().getName())
                .forEach(worldGroup -> contextBuilder.add(WORLD_GROUP_CONTEXT_KEY, worldGroup.getName()));

        contextConsumer.accept(contextBuilder.build());
    }

    @NotNull
    @Override
    public ContextSet estimatePotentialContexts() {
        ImmutableContextSet.Builder contextBuilder = ImmutableContextSet.builder();
        worldGroupManager.getGroups()
                .forEach(worldGroup -> contextBuilder.add(WORLD_GROUP_CONTEXT_KEY, worldGroup.getName()));

        return contextBuilder.build();
    }
}
