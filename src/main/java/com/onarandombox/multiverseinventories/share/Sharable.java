package com.onarandombox.multiverseinventories.share;

import java.util.ArrayList;
import java.util.List;

/**
 * An interface for any attribute that can be shared between worlds in a world group.
 *
 * @param <T> The type of data that this sharable represents.
 */
public interface Sharable<T> {

    /**
     * @return The names of this Sharable for setting as shared in the config.
     */
    String[] getNames();

    SharableHandler<T> getHandler();

    SharableSerializer<T> getSerializer();

    ProfileEntry getProfileEntry();

    Class<T> getType();

    boolean isOptional();

    class Builder<T> {

        private List<String> names = new ArrayList<String>();
        private ProfileEntry profileEntry = null;
        private SharableHandler<T> handler;
        private SharableSerializer<T> serializer = null;
        private boolean optional = false;
        private Class<T> type;

        public Builder(String name, Class<T> type, SharableHandler<T> handler) {
            this.names.add(name);
            this.handler = handler;
            this.type = type;
        }

        public Builder<T> optional(boolean optional) {
            this.optional = optional;
            return this;
        }

        public Builder<T> altName(String name) {
            this.names.add(name);
            return this;
        }

        public Builder<T> stringSerializer(ProfileEntry entry) throws IllegalArgumentException {
            this.serializer = new DefaultStringSerializer<T>(this.type);
            this.profileEntry = entry;
            return this;
        }

        public Builder<T> serializer(ProfileEntry entry) {
            this.serializer = new DefaultSerializer<T>(this.type);
            this.profileEntry = entry;
            return this;
        }

        public Builder<T> serializer(ProfileEntry entry, SharableSerializer<T> serializer) {
            this.serializer = serializer;
            this.profileEntry = entry;
            return this;
        }

        public Sharable<T> build() {
            Sharable<T> sharable = new DefaultSharable<T>(names.toArray(new String[names.size()]), type,
                    handler, serializer, profileEntry, optional);
            ProfileEntry.register(sharable);
            return sharable;
        }
    }
}
