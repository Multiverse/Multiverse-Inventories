package org.mvplugins.multiverse.inventories.profile;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.mvplugins.multiverse.core.config.handle.StringPropertyHandle;
import org.mvplugins.multiverse.core.config.node.ConfigNode;
import org.mvplugins.multiverse.core.config.node.Node;
import org.mvplugins.multiverse.core.config.node.NodeGroup;
import org.mvplugins.multiverse.external.vavr.control.Try;
import org.mvplugins.multiverse.inventories.config.handle.JsonConfigurationHandle;
import org.mvplugins.multiverse.inventories.profile.key.GlobalProfileKey;
import org.mvplugins.multiverse.inventories.util.DataStrings;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * The global profile for a player which contains meta-data for the player.
 */
public final class GlobalProfile {

    private final UUID uuid;
    private final Nodes nodes;
    private final JsonConfigurationHandle handle;
    private final StringPropertyHandle stringPropertyHandle;

    GlobalProfile(UUID uuid, Path configPath) {
        this.uuid = uuid;
        this.nodes = new Nodes();
        this.handle = JsonConfigurationHandle.builder(configPath, nodes.nodes).build();
        this.stringPropertyHandle = new StringPropertyHandle(handle);
        this.handle.load();
    }

    Try<Void> load() {
        return handle.load();
    }

    Try<Void> save() {
        return handle.save();
    }

    public StringPropertyHandle getStringPropertyHandle() {
        return stringPropertyHandle;
    }

    /**
     * Returns the UUID of the player.
     *
     * @return the UUID of the player.
     */
    public UUID getPlayerUUID() {
        return uuid;
    }

    /**
     * Returns the last name the player was known to have.
     *
     * @return the last name the player was known to have.
     */
    public String getLastKnownName() {
        return handle.get(nodes.lastKnownName);
    }

    /**
     * Sets the last name that the player was seen having.
     * <p>This should be updated when a player's name is changed through Mojang but only after their data has been
     * migrated to the new name.</p>
     *
     * @param lastKnownName the last known name for the player.
     */
    public Try<Void> setLastKnownName(String lastKnownName) {
        return handle.set(nodes.lastKnownName, lastKnownName);
    }

    /**
     * Returns the name of last world the player was in.
     *
     * @return The last world the player was in or null if not set.
     */
    public String getLastWorld() {
        return handle.get(nodes.lastWorld);
    }

    /**
     * Sets the last world the player was known to be in. This is done automatically on world change.
     *
     * @param world The world the player is in.
     */
    public Try<Void> setLastWorld(String world) {
        return handle.set(nodes.lastWorld, world);
    }

    /**
     * Says whether the player data for the player's logout world should be loaded when the player logs in.
     * The default value is false.
     *
     * @return true if player data should be loaded when they log in.
     */
    public boolean shouldLoadOnLogin() {
        return handle.get(nodes.loadOnLogin);
    }

    /**
     * Sets whether the player data for the player's logout world should be loaded when the player logs in.
     *
     * @param loadOnLogin true if player data should be loaded when they log in.
     */
    public Try<Void> setLoadOnLogin(boolean loadOnLogin) {
        return handle.set(nodes.loadOnLogin, loadOnLogin);
    }

    @Override
    public String toString() {
        return "GlobalProfile{" +
                "uuid=" + uuid +
                ", lastWorld='" + getLastWorld() + '\'' +
                ", lastKnownName='" + getLastKnownName() + '\'' +
                ", loadOnLogin=" + shouldLoadOnLogin() +
                '}';
    }

    private static final class Nodes {
        private final NodeGroup nodes = new NodeGroup();

        private <N extends Node> N node(N node) {
            nodes.add(node);
            return node;
        }

        private final ConfigNode<String> lastWorld = node(ConfigNode.builder("playerData.lastWorld", String.class)
                .defaultValue("")
                .name("last-world")
                .build());

        private final ConfigNode<String> lastKnownName = node(ConfigNode.builder("playerData.lastKnownName", String.class)
                .defaultValue("")
                .name("last-known-name")
                .build());

        private final ConfigNode<Boolean> loadOnLogin = node(ConfigNode.builder("playerData.loadOnLogin", Boolean.class)
                .defaultValue(false)
                .name("load-on-login")
                .build());
    }
}
