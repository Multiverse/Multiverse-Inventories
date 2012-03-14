package com.onarandombox.multiverseinventories.api.share;

import com.onarandombox.multiverseinventories.api.DataStrings;
import org.bukkit.Location;

/**
 * A simple {@link SharableSerializer} usable with {@link Location} which converts the {@link Location} to the string
 * format that is used by default in Multiverse-Inventories.
 */
public final class LocationSerializer implements SharableSerializer<Location> {

    @Override
    public Location deserialize(Object obj) {
        return DataStrings.parseLocation(obj.toString());
    }

    @Override
    public Object serialize(Location location) {
        return DataStrings.valueOf(location);
    }
}
