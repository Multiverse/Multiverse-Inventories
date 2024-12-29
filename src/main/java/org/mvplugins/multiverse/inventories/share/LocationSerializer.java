package org.mvplugins.multiverse.inventories.share;

import org.mvplugins.multiverse.inventories.DataStrings;
import org.bukkit.Location;

import java.util.Map;

/**
 * A simple {@link SharableSerializer} usable with {@link Location} which converts the {@link Location} to the string
 * format that is used by default in Multiverse-Inventories.
 * @deprecated Locations no longer need a special serializer because they are
 * {@link org.bukkit.configuration.serialization.ConfigurationSerializable}. This remains to convert legacy data.
 */
@Deprecated
public final class LocationSerializer implements SharableSerializer<Location> {

    @Override
    public Location deserialize(Object obj) {
        if (obj instanceof Location) {
            return (Location) obj;
        } else if (obj instanceof String) {
            return DataStrings.parseLocation(obj.toString());
        } else {
            if (obj instanceof Map) {
                return DataStrings.parseLocation((Map) obj);
            } else {
                return DataStrings.parseLocation(obj.toString());
            }
        }
    }

    @Override
    public Object serialize(Location location) {
        return location;
    }
}
