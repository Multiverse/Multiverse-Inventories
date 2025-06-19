package org.mvplugins.multiverse.inventories;

import com.google.common.collect.Lists;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.core.utils.REPatterns;
import org.mvplugins.multiverse.core.utils.StringFormatter;
import org.mvplugins.multiverse.external.jakarta.annotation.PostConstruct;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.inventories.profile.group.WorldGroup;
import org.mvplugins.multiverse.inventories.profile.group.WorldGroupManager;

import java.util.List;
import java.util.Objects;

@Service
final class PlaceholderExpansionHook extends PlaceholderExpansion {

    private final MultiverseInventories plugin;
    private final WorldGroupManager worldGroupManager;

    @Inject
    PlaceholderExpansionHook(@NotNull MultiverseInventories plugin, @NotNull WorldGroupManager worldGroupManager) {
        this.plugin = plugin;
        this.worldGroupManager = worldGroupManager;
    }

    @PostConstruct
    @Override
    public boolean register() {
        return super.register();
    }

    @NotNull
    @Override
    public String getIdentifier() {
        return "multiverse-inventories";
    }

    @NotNull
    @Override
    public String getAuthor() {
        return StringFormatter.joinAnd(plugin.getDescription().getAuthors());
    }

    @NotNull
    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer offlinePlayer, @NotNull String params) {
        List<String> paramsArray = Lists.newArrayList(REPatterns.UNDERSCORE.split(params));
        if (paramsArray.isEmpty() || paramsArray.size() < 2) {
            warning("Placeholder has too little parameters: " + params);
            return null;
        }

        return switch (paramsArray.get(0)) {
            case "world" -> worldPlaceholders(offlinePlayer, paramsArray);
            case "group" -> groupPlaceholders(offlinePlayer, paramsArray);
            default -> {
                warning("Unknown placeholder: " + paramsArray.get(0));
                yield null;
            }
        };
    }

    private String worldPlaceholders(OfflinePlayer offlinePlayer, List<String> paramsArray) {
        String world = null;
        if (paramsArray.size() > 2) {
            world = paramsArray.get(paramsArray.size() - 1);
        }
        if (offlinePlayer instanceof Player player) {
            world = player.getWorld().getName();
        }

        if (world == null) {
            warning("Please specify a world name to use with this placeholder.");
            return null;
        }

        return switch (paramsArray.get(1)) {
            case "groups" -> StringFormatter.join(worldGroupManager.getGroupsForWorld(world).stream()
                    .map(WorldGroup::getName)
                    .toList(), ", ");
            case "groupcount" -> String.valueOf(worldGroupManager.getGroupsForWorld(world).size());
            default -> {
                warning("Unknown world placeholder arg: " + paramsArray.get(1));
                yield null;
            }
        };
    }

    private String groupPlaceholders(OfflinePlayer offlinePlayer, List<String> paramsArray) {
        WorldGroup group = null;
        if (paramsArray.size() > 2) {
            group = worldGroupManager.getGroup(paramsArray.get(paramsArray.size() - 1));
            if (group == null) {
                warning("Group not found: " + paramsArray.get(paramsArray.size() - 1));
                return null;
            }
        } else if (offlinePlayer instanceof Player player) {
            group = worldGroupManager.getGroupsForWorld(player.getWorld().getName())
                    .stream()
                    .findFirst()
                    .orElse(null);
            if (group == null) {
                return "ungrouped world";
            }
        }

        if (group == null) {
            warning("Please specify a group name to use with this placeholder.");
            return null;
        }

        return getGroupPlaceholderValue(group, paramsArray.get(1));
    }

    private String getGroupPlaceholderValue(WorldGroup worldGroup, String placeholder) {
        return switch (placeholder) {
            case "name" -> worldGroup.getName();
            case "worlds" -> StringFormatter.join(worldGroup.getWorlds(), ", ");
            case "shares" -> StringFormatter.join(worldGroup.getShares().toStringList(), ", ");
            case "players" -> StringFormatter.join(worldGroup.getWorlds().stream()
                    .map(Bukkit::getWorld)
                    .filter(Objects::nonNull)
                    .flatMap(world -> world.getPlayers().stream().map(Player::getName))
                    .toList(), ", ");
            case "playercount" -> worldGroup.getWorlds().stream()
                    .map(Bukkit::getWorld)
                    .filter(Objects::nonNull)
                    .map(World::getPlayers)
                    .map(List::size)
                    .reduce(Integer::sum)
                    .orElse(0)
                    .toString();
            default -> {
                warning("Unknown group placeholder arg: " + placeholder);
                yield null;
            }
        };
    }
}
