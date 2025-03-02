package org.mvplugins.multiverse.inventories.commandtools;

import org.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.core.commandtools.MVCommandCompletions;
import org.mvplugins.multiverse.core.commandtools.MVCommandManager;
import org.mvplugins.multiverse.core.configuration.handle.PropertyModifyAction;
import org.mvplugins.multiverse.external.acf.commands.BukkitCommandCompletionContext;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.vavr.control.Try;
import org.mvplugins.multiverse.inventories.config.InventoriesConfig;
import org.mvplugins.multiverse.inventories.dataimport.DataImportManager;
import org.mvplugins.multiverse.inventories.profile.group.WorldGroup;
import org.mvplugins.multiverse.inventories.profile.group.WorldGroupManager;
import org.mvplugins.multiverse.inventories.share.Sharables;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import static org.mvplugins.multiverse.core.utils.StringFormatter.addonToCommaSeperated;

@Service
public final class MVInvCommandCompletion {

    private final InventoriesConfig inventoriesConfig;
    private final WorldGroupManager worldGroupManager;
    private final DataImportManager dataImportManager;

    @Inject
    private MVInvCommandCompletion(
            @NotNull InventoriesConfig inventoriesConfig,
            @NotNull WorldGroupManager worldGroupManager,
            @NotNull DataImportManager dataImportManager,
            @NotNull MVCommandManager mvCommandManager) {
        this.inventoriesConfig = inventoriesConfig;
        this.worldGroupManager = worldGroupManager;
        this.dataImportManager = dataImportManager;

        MVCommandCompletions commandCompletions = mvCommandManager.getCommandCompletions();
        commandCompletions.registerAsyncCompletion("dataimporters", this::suggestDataImporters);
        commandCompletions.registerStaticCompletion("mvinvconfigs", inventoriesConfig.getStringPropertyHandle().getAllPropertyNames());
        commandCompletions.registerAsyncCompletion("mvinvconfigvalues", this::suggestConfigValues);
        commandCompletions.registerAsyncCompletion("sharables", this::suggestSharables);
        commandCompletions.registerAsyncCompletion("shares", this::suggestShares);
        commandCompletions.registerAsyncCompletion("worldGroups", this::suggestWorldGroups);
        commandCompletions.registerAsyncCompletion("worldGroupWorlds", this::suggestWorldGroupWorlds);
    }

    private Collection<String> suggestDataImporters(BukkitCommandCompletionContext context) {
        return dataImportManager.getEnabledImporterNames();
    }

    private Collection<String> suggestConfigValues(BukkitCommandCompletionContext context) {
        return Try.of(() -> context.getContextValue(String.class))
                .map(propertyName -> inventoriesConfig.getStringPropertyHandle()
                        .getSuggestedPropertyValue(propertyName, context.getInput(), PropertyModifyAction.SET))
                .getOrElse(Collections.emptyList());
    }

    private Collection<String> suggestSharables(BukkitCommandCompletionContext context) {
        String scope = context.getConfig("scope", "enabled");

        return Sharables.all().stream()
                .filter(sharable -> switch (scope) {
                    case "enabled" ->
                            !sharable.isOptional() || inventoriesConfig.getActiveOptionalShares().contains(sharable);
                    case "optional" -> sharable.isOptional();
                    default -> true;
                })
                .filter(sharable -> sharable.getNames().length > 0)
                .map(sharable -> sharable.getNames()[0])
                .toList();
    }

    private Collection<String> suggestShares(BukkitCommandCompletionContext context) {
        String input = context.getInput();

        if (input.isEmpty()) {
            // No input, so we're suggesting the first share
            return Sharables.getShareNames();
        }

        int lastComma = input.lastIndexOf(",");
        if (lastComma == -1) {
            // No comma, so we're suggesting the first share
            if (input.startsWith("-")) {
                return Sharables.getShareNames().stream()
                        .map(name -> "-" + name)
                        .collect(Collectors.toList());
            }
            return Sharables.getShareNames();
        }

        // We're suggesting a share after a comma
        String lastShare = input.substring(lastComma + 1);
        String currentSharesString = input.substring(0, lastComma + (lastShare.startsWith("-") ? 2 : 1));
        Set<String> currentShares = Arrays.stream(input.split(","))
                .map(share -> share.startsWith("-") ? share.substring(1) : share)
                .collect(Collectors.toSet());

        return Sharables.getShareNames().stream()
                .filter(name -> !currentShares.contains(name))
                .map(name -> currentSharesString + name)
                .toList();
    }

    private Collection<String> suggestWorldGroups(BukkitCommandCompletionContext context) {
        return worldGroupManager.getGroups().stream()
                .map(WorldGroup::getName)
                .toList();
    }

    private Collection<String> suggestWorldGroupWorlds(BukkitCommandCompletionContext context) {

        var worlds = Try.of(() -> context.getContextValue(WorldGroup.class))
                .map(WorldGroup::getWorlds)
                .getOrElse(Collections.emptySet());

        return addonToCommaSeperated(context.getInput(), worlds);
    }
}
