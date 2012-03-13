package com.onarandombox.multiverseinventories.share;

import com.onarandombox.multiverseinventories.api.DataStrings;
import org.bukkit.Location;

public final class LocationSerializer implements SharableSerializer<Location> {

    @Override
    public final Location deserialize(Object obj) {
        return DataStrings.parseLocation(obj.toString());
    }

    @Override
    public final Object serialize(Location location) {
        return DataStrings.valueOf(location);
    }
}
