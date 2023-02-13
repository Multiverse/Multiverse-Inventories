package com.onarandombox.multiverseinventories.command.tools;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import com.onarandombox.acf.BukkitCommandCompletionContext;
import com.onarandombox.acf.CommandCompletions;
import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.WorldGroup;
import com.onarandombox.multiverseinventories.share.Sharable;
import com.onarandombox.multiverseinventories.share.Sharables;

public class MVInvCommandCompletions {
    public static void init(MultiverseInventories plugin) {
        new MVInvCommandCompletions(plugin);
    }

    private final MultiverseInventories plugin;

    private MVInvCommandCompletions(MultiverseInventories plugin) {
        this.plugin = plugin;
        CommandCompletions<BukkitCommandCompletionContext> commandCompletions = plugin.getCore().getMVCommandManager().getCommandCompletions();
        commandCompletions.registerAsyncCompletion("sharables", this::suggestSharables);
        commandCompletions.registerAsyncCompletion("shares", this::suggestShares);
        commandCompletions.registerAsyncCompletion("worldGroups", this::suggestWorldGroups);
    }

    private Collection<String> suggestSharables(BukkitCommandCompletionContext context) {
        String scope = context.getConfig("scope", "enabled");

        return Sharables.all().stream()
                .filter(sharable -> {
                    switch (scope) {
                        case "enabled": return !sharable.isOptional() || this.plugin.getMVIConfig().getOptionalShares().contains(sharable);
                        case "optional": return sharable.isOptional();
                    }
                    return true;
                })
                .filter(sharable -> sharable.getNames().length > 0)
                .map(sharable -> sharable.getNames()[0])
                .collect(Collectors.toList());
    }

    private Collection<String> suggestShares(BukkitCommandCompletionContext context) {
        return Sharables.allShares().stream()
                .filter(share -> share.getNames().length > 0)
                .map(share -> share.getNames()[0])
                .collect(Collectors.toList());
    }

    private Collection<String> suggestWorldGroups(BukkitCommandCompletionContext context) {
        return this.plugin.getGroupManager().getGroups().stream()
                .unordered()
                .map(WorldGroup::getName)
                .collect(Collectors.toList());
    }
}
