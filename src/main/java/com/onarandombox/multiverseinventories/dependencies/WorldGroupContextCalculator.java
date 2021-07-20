package com.onarandombox.multiverseinventories.dependencies;

import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.WorldGroup;
import net.luckperms.api.context.ContextCalculator;
import net.luckperms.api.context.ContextConsumer;
import net.luckperms.api.context.ContextSet;
import net.luckperms.api.context.ImmutableContextSet;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class WorldGroupContextCalculator implements ContextCalculator<Player> {

    private static final String WORLD_GROUP_CONTEXT_KEY = "mvinv:worldgroup";

    private final MultiverseInventories plugin;

    public WorldGroupContextCalculator(MultiverseInventories plugin) {
        this.plugin = plugin;
    }

    @Override
    public void calculate(@NotNull Player player, @NotNull ContextConsumer contextConsumer) {
        ImmutableContextSet.Builder contextBuilder = ImmutableContextSet.builder();
        this.plugin.getGroupManager()
                .getGroupsForWorld(player.getWorld().getName())
                .forEach(worldGroup -> contextBuilder.add(WORLD_GROUP_CONTEXT_KEY, worldGroup.getName()));

        contextConsumer.accept(contextBuilder.build());
    }

    @Override
    public ContextSet estimatePotentialContexts() {
        ImmutableContextSet.Builder contextBuilder = ImmutableContextSet.builder();
        this.plugin.getGroupManager()
                .getGroups()
                .forEach(worldGroup -> contextBuilder.add(WORLD_GROUP_CONTEXT_KEY, worldGroup.getName()));

        return contextBuilder.build();
    }
}
