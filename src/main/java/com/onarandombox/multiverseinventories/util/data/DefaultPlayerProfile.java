package com.onarandombox.multiverseinventories.util.data;

import com.onarandombox.multiverseinventories.api.share.SharableEntry;
import com.onarandombox.multiverseinventories.profile.ContainerType;
import com.onarandombox.multiverseinventories.api.profile.PlayerProfile;
import com.onarandombox.multiverseinventories.api.profile.ProfileType;
import com.onarandombox.multiverseinventories.api.share.Sharable;
import org.bukkit.OfflinePlayer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Default implementation of a player profile, that is, the data per world/group/gamemode.
 */
class DefaultPlayerProfile implements PlayerProfile {

    private Map<Sharable, SharableEntry> data = new HashMap<Sharable, SharableEntry>();

    private OfflinePlayer player;
    private ContainerType containerType;
    private String containerName;
    private ProfileType profileType;

    DefaultPlayerProfile(ContainerType containerType, String containerName, ProfileType profileType, OfflinePlayer player) {
        this.containerType = containerType;
        this.profileType = profileType;
        this.containerName = containerName;
        this.player = player;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ContainerType getContainerType() {
        return this.containerType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getContainerName() {
        return this.containerName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OfflinePlayer getPlayer() {
        return this.player;
    }

    @Override
    public <T> T get(Sharable<T> sharable) {
        SharableEntry entry = this.data.get(sharable);
        return sharable.getType().cast(entry != null ? entry.getValue() : null);
    }

    @Override
    public <T> void set(Sharable<T> sharable, T value) {
        this.data.put(sharable, new SharableEntry<T>(sharable, value));
    }

    @Override
    public ProfileType getProfileType() {
        return this.profileType;
    }

    @Override
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

