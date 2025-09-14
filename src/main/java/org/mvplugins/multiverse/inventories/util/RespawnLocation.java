package org.mvplugins.multiverse.inventories.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Utility;
import org.bukkit.World;
import org.bukkit.block.data.type.Bed;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.util.NumberConversions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mvplugins.multiverse.external.vavr.control.Try;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

//TODO: This should extend UnloadedWorldLocation, but that class is currently a final class!!!
@SerializableAs("RespawnLocation")
public final class RespawnLocation extends Location {

    private @Nullable String worldName;
    private @NotNull RespawnLocationType respawnType = RespawnLocationType.UNKNOWN;

    public RespawnLocation(@Nullable String worldName, double x, double y, double z, @NotNull RespawnLocationType respawnType) {
        super(null, x, y, z);
        setWorldName(worldName);
        this.respawnType = respawnType;
    }

    public RespawnLocation(@Nullable String worldName, double x, double y, double z, float yaw, float pitch, @NotNull RespawnLocationType respawnType) {
        super(null, x, y, z, yaw, pitch);
        setWorldName(worldName);
        this.respawnType = respawnType;
    }

    public RespawnLocation(@Nullable World world, double x, double y, double z, @NotNull RespawnLocationType respawnType) {
        super(null, x, y, z);
        setWorldName(world == null ? null : world.getName());
        this.respawnType = respawnType;
    }

    public RespawnLocation(@Nullable World world, double x, double y, double z, float yaw, float pitch, @NotNull RespawnLocationType respawnType) {
        super(null, x, y, z, yaw, pitch);
        setWorldName(world == null ? null : world.getName());
        this.respawnType = respawnType;
    }

    public RespawnLocation(@NotNull Location location, @NotNull RespawnLocationType respawnType) {
        this(location.getWorld(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch(), respawnType);
    }

    /**
     * Makes a bukkit {@link Location} copy from this SpawnLocation.
     *
     * @return The bukkit location
     */
    public Location toBukkitLocation() {
        return new Location(getWorld(), getX(), getY(), getZ(), getYaw(), getPitch());
    }

    public void setWorldName(@Nullable String worldName) {
        this.worldName = worldName;
    }

    public String getWorldName() {
        return worldName;
    }

    @Override
    public void setWorld(@Nullable World world) {
        this.worldName = (world == null) ? null : world.getName();
    }

    @Override
    public @Nullable World getWorld() {
        if (worldName == null) {
            return null;
        }
        return Bukkit.getWorld(worldName);
    }

    @Override
    @Utility
    @NotNull
    public Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<>();

        if (this.worldName != null) {
            data.put("world", this.worldName);
        }

        data.put("x", getX());
        data.put("y", getY());
        data.put("z", getZ());

        data.put("yaw", getYaw());
        data.put("pitch", getPitch());

        data.put("respawnType", this.respawnType.name());

        return data;
    }

    /**
     * Required method for deserialization
     *
     * @param args map to deserialize
     * @return deserialized location
     * @throws IllegalArgumentException if the world don't exists
     * @see ConfigurationSerializable
     */
    @NotNull
    public static Location deserialize(@NotNull Map<String, Object> args) {
        return new RespawnLocation(
                args.containsKey("world") ? args.get("world").toString() : null,
                NumberConversions.toDouble(args.get("x")),
                NumberConversions.toDouble(args.get("y")),
                NumberConversions.toDouble(args.get("z")),
                NumberConversions.toFloat(args.get("yaw")),
                NumberConversions.toFloat(args.get("pitch")),
                args.containsKey("respawnType")
                        ? Try.of(() -> RespawnLocationType.valueOf(String.valueOf(args.get("respawnType"))
                                .toUpperCase(Locale.ROOT)))
                        .getOrElse(RespawnLocationType.UNKNOWN)
                        : RespawnLocationType.UNKNOWN
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Location other)) {
            return false;
        }
        String otherWorldName = Try.of(() -> other instanceof RespawnLocation RespawnLocation
                        ? RespawnLocation.worldName
                        : other.getWorld().getName())
                .getOrNull();
        if (!Objects.equals(this.worldName, otherWorldName)) {
            return false;
        }
        if (Double.doubleToLongBits(this.getX()) != Double.doubleToLongBits(other.getX())) {
            return false;
        }
        if (Double.doubleToLongBits(this.getY()) != Double.doubleToLongBits(other.getY())) {
            return false;
        }
        if (Double.doubleToLongBits(this.getZ()) != Double.doubleToLongBits(other.getZ())) {
            return false;
        }
        if (Float.floatToIntBits(this.getPitch()) != Float.floatToIntBits(other.getPitch())) {
            return false;
        }
        if (Float.floatToIntBits(this.getYaw()) != Float.floatToIntBits(other.getYaw())) {
            return false;
        }
        if (this.respawnType != RespawnLocationType.UNKNOWN && other instanceof RespawnLocation otherRespawnLocation) {
            return this.respawnType == otherRespawnLocation.respawnType;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 19 * hash + worldName.hashCode();
        hash = 19 * hash + (int) (Double.doubleToLongBits(this.getX()) ^ (Double.doubleToLongBits(this.getX()) >>> 32));
        hash = 19 * hash + (int) (Double.doubleToLongBits(this.getY()) ^ (Double.doubleToLongBits(this.getY()) >>> 32));
        hash = 19 * hash + (int) (Double.doubleToLongBits(this.getZ()) ^ (Double.doubleToLongBits(this.getZ()) >>> 32));
        hash = 19 * hash + Float.floatToIntBits(this.getPitch());
        hash = 19 * hash + Float.floatToIntBits(this.getYaw());
        hash = 19 * hash + this.respawnType.hashCode();
        return hash;
    }

    @Override
    public String toString() {
        return "RespawnLocation{" +
                "world=" + worldName +
                ",x=" + getX() +
                ",y=" + getY() +
                ",z=" + getZ() +
                ",pitch=" + getPitch() +
                ",yaw=" + getYaw() +
                '}';
    }

    public boolean isValidRespawnLocation() {
        World world = getWorld();
        if (world == null) {
            return false;
        }
        switch (this.respawnType) {
            case BED -> {
                return world.getBlockAt(this).getBlockData() instanceof Bed;
            }
            case ANCHOR -> {
                return world.getBlockAt(this).getType() == Material.RESPAWN_ANCHOR;
            }
            default -> {
                return true;
            }
        }
    }

    @NotNull
    public RespawnLocationType getRespawnType() {
        return respawnType;
    }

    public enum RespawnLocationType {
        WORLD_SPAWN,
        BED,
        ANCHOR,
        UNKNOWN
    }
}
