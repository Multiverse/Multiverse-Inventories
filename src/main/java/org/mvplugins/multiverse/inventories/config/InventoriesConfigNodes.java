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

    private final ConfigHeaderNode settingsHeader = node(ConfigHeaderNode.builder("settings")
            .comment("#######################################")
            .comment("# Settings for Multiverse-Inventories #")
            .comment("#######################################")
            .comment("")
            .comment("")
            .build());

    final ConfigNode<String> locale = node(ConfigNode.builder("settings.locale", String.class)
            .comment("This is the locale you wish to use.")
            .defaultValue("en")
            .name("locale")
            .build());

    final ConfigNode<Boolean> firstRun = node(ConfigNode.builder("settings.first_run", Boolean.class)
            .comment("")
            .comment("If this is true it will generate world groups for you based on MV worlds.")
            .defaultValue(true)
            .name(null)
            .build());

    final ConfigNode<Boolean> useBypass = node(ConfigNode.builder("settings.use_bypass", Boolean.class)
            .comment("")
            .comment("If this is set to true, it will enable bypass permissions (Check the wiki for more info.)")
            .defaultValue(false)
            .name("use-bypass")
            .build());

    final ConfigNode<Boolean> defaultUngroupedWorlds = node(ConfigNode.builder("settings.default_ungrouped_worlds", Boolean.class)
            .comment("")
            .comment("If set to true, any world not listed in a group will automatically use the settings for the default group!")
            .defaultValue(false)
            .name("default-ungrouped-worlds")
            .build());

    final ConfigNode<Boolean> loggingSaveLoad = node(ConfigNode.builder("settings.save_load_on_log_in_out", Boolean.class)
            .comment("")
            .comment("The default and suggested setting for this is FALSE.")
            .comment("False means Multiverse-Inventories will not attempt to load or save any player data when they log in and out.")
            .comment("That means that MINECRAFT will handle that exact thing JUST LIKE IT DOES NORMALLY.")
            .comment("Changing this to TRUE will have Multiverse-Inventories save player data when they log out and load it when they log in.")
            .comment("The biggest potential drawback here is that if your server crashes, player stats/inventories may be lost/rolled back!")
            .defaultValue(false)
            .name("save-load-on-log-in-out")
            .build());

    private final ConfigHeaderNode sharesHeader = node(ConfigHeaderNode.builder("shares")
            .comment("")
            .comment("")
            .build());


    final ConfigNode<Boolean> useOptionalsForUngrouped = node(ConfigNode.builder("shares.optionals_for_ungrouped_worlds", Boolean.class)
            .comment("When set to true, optional shares WILL be utilized in cases where a group does not cover their uses for a world.")
            .comment("An example of this in action would be an ungrouped world using last_location. When this is true, players will return to their last location in that world.")
            .comment("When set to false, optional shares WILL NOT be utilized in these cases, effectively disabling it for ungrouped worlds.")
            .defaultValue(true)
            .name("optionals-for-ungrouped-worlds")
            .build());

    final ConfigNode<Shares> optionalShares = node(ConfigNode.builder("shares.use_optionals", Shares.class)
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

    final ConfigNode<Boolean> useGameModeProfiles = node(ConfigNode.builder("settings.use_game_mode_profiles", Boolean.class)
            .comment("")
            .comment("If this is set to true, players will have different inventories/stats for each game mode.")
            .comment("Please note that old data migrated to the version that has this feature will have their data copied for both game modes.")
            .defaultValue(false)
            .name("use-game-mode-profiles")
            .build());
}
