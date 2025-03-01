package org.mvplugins.multiverse.inventories.config;

import org.mvplugins.multiverse.core.configuration.functions.NodeSerializer;
import org.mvplugins.multiverse.core.configuration.node.ConfigHeaderNode;
import org.mvplugins.multiverse.core.configuration.node.ConfigNode;
import org.mvplugins.multiverse.core.configuration.node.Node;
import org.mvplugins.multiverse.core.configuration.node.NodeGroup;
import org.mvplugins.multiverse.inventories.share.Sharables;
import org.mvplugins.multiverse.inventories.share.Shares;

import java.util.List;
import java.util.Objects;

final class InventoriesConfigNodes {

    private final NodeGroup nodes = new NodeGroup();

    InventoriesConfigNodes() {
    }

    NodeGroup getNodes() {
        return nodes;
    }

    private <N extends Node> N node(N node) {
        nodes.add(node);
        return node;
    }

    private final ConfigHeaderNode shareHandlingHeader = node(ConfigHeaderNode.builder("share-handling")
            .comment("#-----------------------------------------------------------------------------------------------------------------#")
            .comment("#                                                                                                                 #")
            .comment("#     __  __ _   _ _____ _    _____   _____ ___  ___ ___    ___ _  ___   _____ _  _ _____ ___  ___ ___ ___ ___    #")
            .comment("#    |  \\/  | | | |_   _| |  |_ _\\ \\ / / __| _ \\/ __| __|  |_ _| \\| \\ \\ / / __| \\| |_   _/ _ \\| _ \\_ _| __/ __|   #")
            .comment("#    | |\\/| | |_| | | | | |__ | | \\ V /| _||   /\\__ \\ _|    | || .` |\\ V /| _|| .` | | || (_) |   /| || _|\\__ \\   #")
            .comment("#    |_|  |_|\\___/  |_| |____|___| \\_/ |___|_|_\\|___/___|  |___|_|\\_| \\_/ |___|_|\\_| |_| \\___/|_|_\\___|___|___/   #")
            .comment("#                                                                                                                 #")
            .comment("#                                                                                                                 #")
            .comment("#                                                                                                                 #")
            .comment("#                                                                                                                 #")
            .comment("#            WIKI:        https://github.com/Multiverse/Multiverse-Core/wiki/Basics-(Inventories)                 #")
            .comment("#            DISCORD:     https://discord.gg/NZtfKky                                                              #")
            .comment("#            BUG REPORTS: https://github.com/Multiverse/Multiverse-Inventories/issues                             #")
            .comment("#                                                                                                                 #")
            .comment("#                                                                                                                 #")
            .comment("#            New options are added to this file automatically. If you manually made changes                       #")
            .comment("#            to this file while your server is running, please run `/mvinv reload` command.                       #")
            .comment("#                                                                                                                 #")
            .comment("#-----------------------------------------------------------------------------------------------------------------#")
            .comment("")
            .comment("")
            .build());

    final ConfigNode<Boolean> enableBypassPermissions = node(ConfigNode.builder("share-handling.enable-bypass-permissions", Boolean.class)
            .comment("If this is set to true, it will enable bypass permissions (Check the wiki for more info.)")
            .defaultValue(false)
            .name("enable-bypass")
            .build());

    final ConfigNode<Boolean> enableGamemodeShareHandling = node(ConfigNode.builder("share-handling.enable-gamemode-share-handling", Boolean.class)
            .comment("")
            .comment("If this is set to true, players will have different inventories/stats for each game mode.")
            .comment("Please note that old data migrated to the version that has this feature will have their data copied for both game modes.")
            .defaultValue(false)
            .name("enable-gamemode-share-handling")
            .build());

    final ConfigNode<Boolean> defaultUngroupedWorlds = node(ConfigNode.builder("share-handling.default-ungrouped-worlds", Boolean.class)
            .comment("")
            .comment("If set to true, any world not listed in a group will automatically be assigned to the 'default' group!")
            .defaultValue(false)
            .name("default-ungrouped-worlds")
            .build());

    final ConfigNode<Boolean> useOptionalsForUngroupedWorlds = node(ConfigNode.builder("share-handling.use-optionals-for-ungrouped-worlds", Boolean.class)
            .comment("")
            .comment("When set to true, optional shares WILL be utilized in cases where a group does not cover their uses for a world.")
            .comment("An example of this in action would be an ungrouped world using last_location. When this is true, players will return to their last location in that world.")
            .comment("When set to false, optional shares WILL NOT be utilized in these cases, effectively disabling it for ungrouped worlds.")
            .defaultValue(true)
            .name("use-optionals-for-ungrouped-worlds")
            .build());

    final ConfigNode<Shares> activeOptionalShares = node(ConfigNode.builder("share-handling.active-optional-shares", Shares.class)
            .comment("")
            .comment("You must specify optional shares you wish to use here or they will be ignored.")
            .comment("The only built-in optional shares are \"economy\" and \"last_location\".")
            .defaultValue(Sharables.noneOf())
            .name(null)
            .serializer(new NodeSerializer<>() {
                @Override
                public Shares deserialize(Object o, Class<Shares> aClass) {
                    if (o instanceof List) {
                        return Sharables.fromList((List) o);
                    }
                    return Sharables.fromList(List.of(Objects.toString(o)));
                }

                @Override
                public Object serialize(Shares sharables, Class<Shares> aClass) {
                    return sharables.toStringList();
                }
            })
            .build());

    private final ConfigHeaderNode performanceHeader = node(ConfigHeaderNode.builder("performance")
            .comment("")
            .comment("")
            .build());

    final ConfigNode<Boolean> savePlayerdataOnQuit = node(ConfigNode.builder("performance.save-playerdata-on-quit", Boolean.class)
            .comment("This option may be useful if you want an up-to-date offline copy of the playerdata within mvinv.")
            .comment("However, this will result in minor performance overhead on every player quit.")
            .defaultValue(false)
            .name("save-playerdata-on-quit")
            .build());

    final ConfigNode<Boolean> applyPlayerdataOnJoin = node(ConfigNode.builder("performance.apply-playerdata-on-join", Boolean.class)
            .comment("")
            .comment("This will only work if save-playerdata-on-quit is set to true.")
            .comment("Minecraft will already load the most up-to-date player data and this option will generally be redundant.")
            .comment("The only possible edge case uses is if you have a need to always modify the mvinv playerdata while the player is offline.")
            .defaultValue(false)
            .name("apply-playerdata-on-join")
            .build());

    final ConfigNode<Boolean> alwaysWriteWorldProfile = node(ConfigNode.builder("performance.always-write-world-profile", Boolean.class)
            .comment("")
            .defaultValue(true)
            .name("always-write-world-profile")
            .build());

    private final ConfigHeaderNode cacheHeader = node(ConfigHeaderNode.builder("performance.cache")
            .comment("")
            .comment("NOTE: Cache options require a server restart to take effect.")
            .build());

    final ConfigNode<Integer> playerFileCacheSize = node(ConfigNode.builder("performance.cache.player-file-cache-size", Integer.class)
            .defaultValue(2000)
            .name("player-file-cache-size")
            .build());

    final ConfigNode<Integer> playerFileCacheExpiry = node(ConfigNode.builder("performance.cache.player-file-cache-expiry", Integer.class)
            .defaultValue(60)
            .name("player-file-cache-expiry")
            .build());

    final ConfigNode<Integer> playerProfileCacheSize = node(ConfigNode.builder("performance.cache.player-profile-cache-size", Integer.class)
            .defaultValue(6000)
            .name("player-profile-cache-size")
            .build());

    final ConfigNode<Integer> playerProfileCacheExpiry = node(ConfigNode.builder("performance.cache.player-profile-cache-expiry", Integer.class)
            .defaultValue(60)
            .name("player-profile-cache-expiry")
            .build());

    final ConfigNode<Integer> globalProfileCacheSize = node(ConfigNode.builder("performance.cache.global-profile-cache-size", Integer.class)
            .defaultValue(500)
            .name("global-profile-cache-size")
            .build());

    final ConfigNode<Integer> globalProfileCacheExpiry = node(ConfigNode.builder("performance.cache.global-profile-cache-expiry", Integer.class)
            .defaultValue(60)
            .name("global-profile-cache-expiry")
            .build());

    final ConfigNode<Boolean> firstRun = node(ConfigNode.builder("first-run", Boolean.class)
            .comment("")
            .comment("")
            .comment("Do not edit the following values!!!!!")
            .defaultValue(true)
            .hidden()
            .build());

    final ConfigNode<Double> version = node(ConfigNode.builder("version", Double.class)
            .defaultValue(0.0)
            .hidden()
            .build());
}
