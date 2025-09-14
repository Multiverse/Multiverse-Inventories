package org.mvplugins.multiverse.inventories.share;

import org.bukkit.Location;
import org.mvplugins.multiverse.core.world.location.UnloadedWorldLocation;
import org.mvplugins.multiverse.inventories.util.LegacyParsers;
import org.mvplugins.multiverse.inventories.util.RespawnLocation;

import java.util.Map;

/**
 * A simple {@link SharableSerializer} usable with {@link Location} which converts the {@link Location} to the string
 * format that is used by default in Multiverse-Inventories.
 */
abstract class LocationSerializer implements SharableSerializer<Location> {

    @Override
    public Location deserialize(Object obj) {
        if (obj instanceof Location) {
            return (Location) obj;
        }
        if (obj instanceof String) {
            return LegacyParsers.parseLocation(obj.toString());
        }
        if (obj instanceof Map) {
            return LegacyParsers.parseLocation((Map) obj);
        }
        return LegacyParsers.parseLocation(obj.toString());
    }

    @Override
    abstract public Location serialize(Location loc);

    static final class UnloadedWorldLocationSerializer extends LocationSerializer {
        @Override
        public Location serialize(Location loc) {
            if (loc != null && !(loc instanceof UnloadedWorldLocation)) {
                return new UnloadedWorldLocation(loc);
            }
            return loc;
        }
    }

    static final class RespawnLocationSerializer extends LocationSerializer {
        @Override
        public Location serialize(Location loc) {
            if (loc != null && !(loc instanceof RespawnLocation)) {
                return new RespawnLocation(loc, RespawnLocation.RespawnLocationType.UNKNOWN);
            }
            return loc;
        }
    }
}
