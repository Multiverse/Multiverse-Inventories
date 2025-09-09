package org.mvplugins.multiverse.inventories.config;

import org.mvplugins.multiverse.core.config.node.ConfigHeaderNode;
import org.mvplugins.multiverse.core.config.node.ConfigNode;
import org.mvplugins.multiverse.core.config.node.ListConfigNode;
import org.mvplugins.multiverse.core.config.node.Node;
import org.mvplugins.multiverse.core.config.node.NodeGroup;
import org.mvplugins.multiverse.core.config.node.serializer.NodeSerializer;
import org.mvplugins.multiverse.inventories.share.Sharables;
import org.mvplugins.multiverse.inventories.share.Shares;

import java.util.ArrayList;
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
            .comment("Built-in optional shares are: \"economy\" and \"last_location\".")
            .defaultValue(Sharables.noneOf())
            .hidden()
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
            .onSetValue((oldValue, newValue) -> Sharables.recalculateEnabledShares())
            .build());

    private final ConfigHeaderNode sharablesHeader = node(ConfigHeaderNode.builder("sharables")
            .comment("")
            .comment("")
            .build());

    final ConfigNode<Boolean> useImprovedRespawnLocationDetection = node(ConfigNode.builder("sharables.use-improved-respawn-location-detection", Boolean.class)
            .comment("When enabled, we will use 1.21's PlayerSpawnChangeEvent to better detect bed and anchor respawn locations.")
            .comment("This options is not applicable for older minecraft server versions.")
            .defaultValue(true)
            .name("use-improved-respawn-location-detection")
            .build());

    final ConfigNode<Boolean> resetLastLocationOnDeath = node(ConfigNode.builder("sharables.reset-last-location-on-death", Boolean.class)
            .comment("")
            .comment("When set to true, the last location of the player will be reset when they die.")
            .comment("This is useful if they respawn in a different world and you do not want them to return to their death location.")
            .defaultValue(false)
            .name("reset-last-location-on-death")
            .build());

    final ConfigNode<Boolean> applyLastLocationForAllTeleports = node(ConfigNode.builder("sharables.apply-last-location-for-all-teleports", Boolean.class)
            .comment("")
            .comment("When enabled, the last location of the player will be applied for any teleportation.")
            .comment("This is useful as you want to use the last location for any teleportation, such as the warp system.")
            .comment("When disabled, you can only use `/mv tp ll:worldname` to teleport to the player's last location.")
            .defaultValue(true)
            .name("apply-last-location-for-all-teleports")
            .build());

    final ConfigNode<Boolean> useByteSerializationForInventoryData = node(ConfigNode.builder("sharables.use-byte-serialization-for-inventory-data", Boolean.class)
            .comment("")
            .comment("When enabled, we will use paper's improved byte serialization for inventory data.")
            .comment("When disabled, we will use the legacy configuration serialization method.")
            .comment("!!!!!BIG NOTE:")
            .comment("  This option is only applicable on PAPERMC.")
            .comment("  Once you enable this option, you cannot change your server software back to SPIGOT.")
            .comment("------------")
            .comment("Byte serialization will use minecraft's NBT format. NBT is safer for data migrations as it will use the built in ")
            .comment("data converter instead of bukkits dangerous serialization system. This will fix various issues with the inventory data")
            .comment("such as Skulker Box data loss, equip-sound crash, FoodEffect error, and more.")
            .defaultValue(false)
            .name("use-byte-serialization-for-inventory-data")
            .build());

    private final ConfigHeaderNode performanceHeader = node(ConfigHeaderNode.builder("performance")
            .comment("")
            .comment("")
            .build());

    final ConfigNode<Boolean> applyPlayerdataOnJoin = node(ConfigNode.builder("performance.apply-playerdata-on-join", Boolean.class)
            .comment("")
            .comment("Minecraft will already load the most up-to-date player data and this option will generally be redundant.")
            .comment("The only possible edge case uses is if you have a need to always modify the mvinv playerdata while the player is offline.")
            .defaultValue(false)
            .name("apply-playerdata-on-join")
            .build());

    final ConfigNode<Boolean> alwaysWriteWorldProfile = node(ConfigNode.builder("performance.always-write-world-profile", Boolean.class)
            .comment("")
            .comment("By default, even when the group shares all or going to a world within the same group, the world profile will still be written to disk.")
            .comment("This will ensure that the world profile is always up-to-date, so when removing the world from the group, it will not be missing data.")
            .comment("However, if you are certain that your world will always be in a group, you can set this to false to slightly improve performance.")
            .defaultValue(true)
            .name("always-write-world-profile")
            .build());

    private final ConfigHeaderNode preloadHeader = node(ConfigHeaderNode.builder("performance.preload-data-on-join")
            .comment("")
            .comment("Pre-loads player data into caches when joining the server.")
            .comment("This will reduce the load time on first teleport to the world/group, with the cost of increased memory usage and join time.")
            .build());

    final ListConfigNode<String> preloadDataOnJoinWorlds = node(ListConfigNode.listBuilder("performance.preload-data-on-join.worlds", String.class)
            .defaultValue(ArrayList::new)
            .name("preload-data-on-join-worlds")
            .build());

    final ListConfigNode<String> preloadDataOnJoinGroups = node(ListConfigNode.listBuilder("performance.preload-data-on-join.groups", String.class)
            .defaultValue(ArrayList::new)
            .name("preload-data-on-join-groups")
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

    private final ConfigHeaderNode miscHeader = node(ConfigHeaderNode.builder("misc")
            .comment("")
            .comment("")
            .build());

    final ConfigNode<Boolean> registerPapiHook = node(ConfigNode.builder("misc.register-papi-hook", Boolean.class)
            .comment("This config option defines whether or not Multiverse should register the PlaceholderAPI hook.")
            .comment("This only applies if PlaceholderAPI is installed.")
            .defaultValue(true)
            .name("register-papi-hook")
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
