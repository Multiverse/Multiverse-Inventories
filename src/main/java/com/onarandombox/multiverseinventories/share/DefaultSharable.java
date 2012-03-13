package com.onarandombox.multiverseinventories.share;

/**
 * An abstract class used to define a value that can be shared between worlds and world groups in
 * Multiverse-Inventories.  By extending this class and implementing it's abstract methods you will have a way to
 * alter a player's data based on what Multiverse-Inventories world/world group they are in.  This does not
 * automatically persist any data.
 *
 * @param <T> The type of data this Sharable represents.
 */
class DefaultSharable<T> implements Sharable<T> {

    private String[] names;
    private SharableHandler<T> handler;
    private SharableSerializer<T> serializer;
    private ProfileEntry profileEntry;
    private boolean optional;
    private Class<T> type;

    DefaultSharable(String[] names, Class<T> type, SharableHandler<T> handler, SharableSerializer<T> serializer,
                    ProfileEntry entry, boolean optional) {
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
    public final String[] getNames() {
        return names;
    }

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
