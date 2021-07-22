package com.onarandombox.multiverseinventories;

import com.onarandombox.MultiverseCore.api.MVDestination;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * A utility class to keep track of currently happening teleportations.
 * Keeps track of teleport destinations as well the type of destination.
 */
public class TeleportDetails {
    public enum TeleportType {
        MVTP,
        OTHER
    }

    static class TeleportDestination<T> {
        private final TeleportType type;
        private final T destination;

        TeleportDestination(TeleportType type, T destination) {
            this.type = type;
            this.destination = destination;
        }

        public TeleportType getType() {
            return this.type;
        }

        public T getDestination() {
            return this.destination;
        }
    }

    private final static Map<Player, TeleportDestination<?>> teleportDestinationMap = new HashMap<>();

    public static void addTeleportDestination(Player player, MVDestination mvDestination) {
        teleportDestinationMap.put(player, new TeleportDestination<>(TeleportType.MVTP, mvDestination));
    }

    public static void addTeleportDestination(Player player, Location location) {
        teleportDestinationMap.put(player, new TeleportDestination<>(TeleportType.OTHER, location));
    }

    public static TeleportDestination<?> getTeleportDestination(Player player) {
        return teleportDestinationMap.get(player);
    }

    public static void removeTeleportDestination(Player player) {
        teleportDestinationMap.remove(player);
    }
}
