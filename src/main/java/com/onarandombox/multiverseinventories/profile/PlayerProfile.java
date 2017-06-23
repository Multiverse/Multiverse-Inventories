package com.onarandombox.multiverseinventories.profile;

import com.onarandombox.multiverseinventories.share.Sharable;
import com.onarandombox.multiverseinventories.share.SharableEntry;
import com.onarandombox.multiverseinventories.profile.container.ContainerType;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Contains all the world/group specific data for a player.
 */
public final class PlayerProfile implements Cloneable, Iterable<SharableEntry> {

    public static PlayerProfile createPlayerProfile(ContainerType containerType, String containerName,
                                                    ProfileType profileType, OfflinePlayer player) {
        return new PlayerProfile(containerType, containerName, profileType, player);
    }

    @Deprecated
    public static PlayerProfile createPlayerProfile(ContainerType containerType, String containerName,
                                                    ProfileType profileType, String playerName) {
        return new PlayerProfile(containerType, containerName, profileType, Bukkit.getOfflinePlayer(playerName));
    }

    private Map<Sharable, SharableEntry> data = new HashMap<Sharable, SharableEntry>();

    private final OfflinePlayer player;
    private final ContainerType containerType;
    private final String containerName;
    private final ProfileType profileType;

    private PlayerProfile(ContainerType containerType, String containerName, ProfileType profileType, OfflinePlayer player) {
        this.containerType = containerType;
        this.profileType = profileType;
        this.containerName = containerName;
        this.player = player;
    }

    /**
     * @return The container type of profile, a group or world.
     */
    public ContainerType getContainerType() {
        return this.containerType;
    }

    /**
     * @return The name of the container, world or group, containing this profile.
     */
    public String getContainerName() {
        return this.containerName;
    }

    /**
     * @return the Player associated with this profile.
     */
    public OfflinePlayer getPlayer() {
        return this.player;
    }

    /**
     * @return The type of profile this object represents.
     */
    public ProfileType getProfileType() {
        return this.profileType;
    }

    /**
     * Retrieves the profile's value of the {@link Sharable} passed in.
     *
     * @param sharable Represents the key for the data wanted from the profile.
     * @param <T>      This indicates the type of return value to be expected.
     * @return The value of the sharable for this profile.  Null if no value is set.
     */
    public <T> T get(Sharable<T> sharable) {
        SharableEntry entry = this.data.get(sharable);
        return sharable.getType().cast(entry != null ? entry.getValue() : null);
    }

    /**
     * Sets the profile's value for the {@link Sharable} passed in.
     *
     * @param sharable Represents the key for the data to store.
     * @param value    The value of the data.
     * @param <T>      The type of value to be expected.
     */
    public <T> void set(Sharable<T> sharable, T value) {
        this.data.put(sharable, new SharableEntry<T>(sharable, value));
    }

    public PlayerProfile clone() throws CloneNotSupportedException {
        return (PlayerProfile) super.clone();
    }

    @Override
    public Iterator<SharableEntry> iterator() {
        return new SharablesIterator(data.values().iterator());
    }

    private static class SharablesIterator implements Iterator<SharableEntry> {

        private final Iterator<SharableEntry> backingIterator;

        private SharablesIterator(Iterator<SharableEntry> backingIterator) {
            this.backingIterator = backingIterator;
        }

        @Override
        public boolean hasNext() {
            return backingIterator.hasNext();
        }

        @Override
        public SharableEntry next() {
            return backingIterator.next();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}

