package com.onarandombox.multiverseinventories.share;

import com.onarandombox.multiverseinventories.share.handlers.SharableHandler;
import com.onarandombox.multiverseinventories.share.serializers.SharableSerializer;

/**
 * A class used to define a value that can be shared between worlds and world groups in Multiverse-Inventories.
 *
 * @param <T> The type of data this Sharable represents.
 */
final class DefaultSharable<T> implements Sharable<T> {

    private final String[] names;
    private final SharableHandler<T> handler;
    private final SharableSerializer<T> serializer;
    private final ProfileEntry profileEntry;
    private final boolean optional;
    private final Class<T> type;

    DefaultSharable(final String[] names, final Class<T> type, final SharableHandler<T> handler,
                    final SharableSerializer<T> serializer, final ProfileEntry entry, final boolean optional) {
        this.names = names;
        this.handler = handler;
        this.serializer = serializer;
        this.profileEntry = entry;
        this.optional = optional;
        this.type = type;
        Sharables.register(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getNames() {
        return names;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<T> getType() {
        return this.type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return this.names[0];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SharableHandler<T> getHandler() {
        return this.handler;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SharableSerializer<T> getSerializer() {
        return this.serializer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProfileEntry getProfileEntry() {
        return this.profileEntry;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isOptional() {
        return this.optional;
    }
}
