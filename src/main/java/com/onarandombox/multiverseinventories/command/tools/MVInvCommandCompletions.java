package com.onarandombox.multiverseinventories.command.tools;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.onarandombox.acf.BukkitCommandCompletionContext;
import com.onarandombox.acf.CommandCompletions;
import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.WorldGroup;
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
        String input = context.getInput();

        if (input.isEmpty()) {
            // No input, so we're suggesting the first share
            return Sharables.registeredNames();
        }

        int lastComma = input.lastIndexOf(",");
        if (lastComma == -1) {
            // No comma, so we're suggesting the first share
            if (input.startsWith("-")) {
                return Sharables.registeredNames().stream()
                        .map(name -> "-" + name)
                        .collect(Collectors.toList());
            }
            return Sharables.registeredNames();
        }

        // We're suggesting a share after a comma
        String lastShare = input.substring(lastComma + 1);
        String currentSharesString = input.substring(0, lastComma + (lastShare.startsWith("-") ? 2 : 1));
        Set<String> currentShares = Arrays.stream(input.split(","))
                .map(share -> share.startsWith("-") ? share.substring(1) : share)
                .collect(Collectors.toSet());

        return Sharables.registeredNames().stream()
                .filter(name -> !currentShares.contains(name))
                .map(name -> currentSharesString + name)
                .collect(Collectors.toList());
    }

    private Collection<String> suggestWorldGroups(BukkitCommandCompletionContext context) {
        return this.plugin.getGroupManager().getGroups().stream()
                .unordered()
                .map(WorldGroup::getName)
                .collect(Collectors.toList());
    }
}
