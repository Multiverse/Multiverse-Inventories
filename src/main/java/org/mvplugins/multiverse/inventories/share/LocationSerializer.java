package org.mvplugins.multiverse.inventories.share;

import org.bukkit.Location;
import org.mvplugins.multiverse.inventories.util.LegacyParsers;

import java.util.Map;

/**
 * A simple {@link SharableSerializer} usable with {@link Location} which converts the {@link Location} to the string
 * format that is used by default in Multiverse-Inventories.
 * @deprecated Locations no longer need a special serializer because they are
 * {@link org.bukkit.configuration.serialization.ConfigurationSerializable}. This remains to convert legacy data.
 */
@Deprecated
final class LocationSerializer implements SharableSerializer<Location> {

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
    public Object serialize(Location location) {
        return location;
    }
}
