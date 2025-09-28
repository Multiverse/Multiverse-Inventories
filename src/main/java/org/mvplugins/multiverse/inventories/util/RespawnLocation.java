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
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mvplugins.multiverse.external.vavr.control.Try;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Location information with respawn type. See also {@link RespawnLocationType}.
 * <br />
 * TODO: This should extend UnloadedWorldLocation, but that class is currently a final class!!!
 */
@SerializableAs("RespawnLocation")
@ApiStatus.AvailableSince("5.2")
public class RespawnLocation extends Location {

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

    public @Nullable String getWorldName() {
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
        if (other instanceof RespawnLocation otherRespawnLocation
                && this.respawnType != otherRespawnLocation.respawnType) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 19 * hash + String.valueOf(worldName).hashCode();
        hash = 19 * hash + Long.hashCode(Double.doubleToLongBits(this.getX()));
        hash = 19 * hash + Long.hashCode(Double.doubleToLongBits(this.getY()));
        hash = 19 * hash + Long.hashCode(Double.doubleToLongBits(this.getZ()));
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
                ",respawnType=" + this.respawnType +
                '}';
    }

    /**
     * Checks if the respawn location is valid based on the respawn type.
     * <br />
     * For {@link RespawnLocationType#BED}, checks if the block at the location is a bed block.
     * <br />
     * For {@link RespawnLocationType#ANCHOR}, checks if the block at the location is a respawn anchor block.
     * <br />
     * @return true if the respawn location is valid, false otherwise.
     */
    @ApiStatus.AvailableSince("5.2")
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

    /**
     * Gets the respawn location type.
     *
     * @return the respawn location type
     */
    @ApiStatus.AvailableSince("5.2")
    public @NotNull RespawnLocationType getRespawnType() {
        return respawnType;
    }

    /**
     * Sets the respawn location type.
     *
     * @param respawnType the respawn location type
     */
    @ApiStatus.AvailableSince("5.2")
    public  void setRespawnType(@NotNull RespawnLocationType respawnType) {
        this.respawnType = respawnType;
    }

    /**
     * The type of respawn location.
     */
    @ApiStatus.AvailableSince("5.2")
    public enum RespawnLocationType {
        /**
         * The respawn location is a bed, and the location should have a bed block.
         */
        @ApiStatus.AvailableSince("5.2")
        BED,

        /**
         * The respawn location is a respawn anchor, and the location should have a respawn anchor block.
         */
        @ApiStatus.AvailableSince("5.2")
        ANCHOR,

        /**
         * All other possible respawn types, such as custom /spawnpoint or specific location set by other plugins.
         */
        @ApiStatus.AvailableSince("5.2")
        UNKNOWN
    }
}
