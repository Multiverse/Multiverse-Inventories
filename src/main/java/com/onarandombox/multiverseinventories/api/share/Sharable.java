package com.onarandombox.multiverseinventories.api.share;

import java.util.ArrayList;
import java.util.List;

/**
 * An interface for any attribute that can be shared between worlds in a world group.  These objects are intended to
 * be used as constants and may not function properly otherwise.
 *
 * @param <T> The type of data that this sharable represents.
 */
public interface Sharable<T> {

    /**
     * @return The names of this Sharable for setting as shared in the config.  There should ALWAYS be a index-0 which
     * represents the main name, and the one that will be used for storing the sharable in a groups shares list in
     * the config file.  All names in this array may be used to set a group as sharing this Sharable.
     */
    String[] getNames();

    /**
     * @return The object that will handle changing out the player's data with the profile's data and vice versa when
     * a player changes worlds.
     */
    SharableHandler<T> getHandler();

    /**
     * @return The object that will handle serializing a profile's data for this sharable for saving/loading in the
     * profile's yaml file.  If this is null it means that persistence is not handled by Multiverse-Inventories for
     * this Sharable.
     */
    SharableSerializer<T> getSerializer();

    /**
     * @return The profile entry that describes how to store this Sharable in a profile's yaml file.  This may NOT be
     * null if this Sharable getSerializer() is not null.  If getSerializer() IS null, this method is never called.
     */
    ProfileEntry getProfileEntry();

    /**
     * @return The type of data this Sharable represents.  Used primarily for casting.
     */
    Class<T> getType();

    /**
     * @return True if this Sharable is optional.  That is to say that it is completely ignored when share handling
     * takes place UNLESS it is present in
     * {@link com.onarandombox.multiverseinventories.api.InventoriesConfig#getOptionalShares()}.
     */
    boolean isOptional();

    /**
     * This class is used to build new {@link Sharable}s.  Simply instantiate this and use method chaining to set
     * all the options for your Sharable.
     *
     * @param <T> The type of data the new Sharable will represent.
     */
    class Builder<T> {

        private List<String> names = new ArrayList<String>();
        private ProfileEntry profileEntry = null;
        private SharableHandler<T> handler;
        private SharableSerializer<T> serializer = null;
        private boolean optional = false;
        private Class<T> type;

        /**
         * @param name The primary name of the new Sharable.
         * @param type The type of data the Sharable represents.
         * @param handler The object that will handle switching the Sharable data between player and profile.
         */
        public Builder(String name, Class<T> type, SharableHandler<T> handler) {
            this.names.add(name);
            this.handler = handler;
            this.type = type;
        }

        /**
         * Indicates that the new Sharable is optional as described in {@link Sharable#isOptional()}.
         *
         * @return This builder object for method chaining.
         */
        public Builder<T> optional() {
            this.optional = true;
            return this;
        }

        /**
         * @param name An alternate name for this Sharable which can be used to indicate a group is sharing this
         *             Sharable.
         * @return This builder object for method chaining.
         */
        public Builder<T> altName(String name) {
            this.names.add(name);
            return this;
        }

        /**
         * Sets this sharable to be serialized as a string in the profile yaml file.  To use this, the class type
         * indicates in the Builder's constructor MUST have a static .valueOf(String) method that returns it's type.
         *
         * @param entry The profile entry describing where this Sharable is located in the profile file.
         * @return This builder object for method chaining.
         * @throws IllegalArgumentException This is thrown if the type indicated in the Builder's constructor does not
         * fit the constraints indicated above.
         */
        public Builder<T> stringSerializer(ProfileEntry entry) {
            this.serializer = new DefaultStringSerializer<T>(this.type);
            this.profileEntry = entry;
            return this;
        }

        /**
         * This will make the Sharable use the default serializer which simply passes the data as is to the persistence
         * object for persistence.  This will only work depending on the data type this Sharable represents and further
         * depending on the types the persistence methods accept.  Generally, boxed primitives are okay as well as
         * Lists of boxed primitives and Map<String, Object>.  All other types will likely require a custom
         * {@link SharableSerializer} indicated with {@link #serializer(ProfileEntry, SharableSerializer)}.
         *
         * @param entry The profile entry describing where this Sharable is located in the profile file.
         * @return This builder object for method chaining.
         */
        public Builder<T> defaultSerializer(ProfileEntry entry) {
            this.serializer = new DefaultSerializer<T>(this.type);
            this.profileEntry = entry;
            return this;
        }

        /**
         * This allows you to specify a custom {@link SharableSerializer} to use to convert the data represented by
         * this Sharable into something acceptable by persistence.
         *
         * @param entry The profile entry describing where this Sharable is located in the profile file.
         * @param serializer A custom serializer describing how to handle the data in order for it to be persisted in
         *                   the profile.
         * @return This builder object for method chaining.
         */
        public Builder<T> serializer(ProfileEntry entry, SharableSerializer<T> serializer) {
            this.serializer = serializer;
            this.profileEntry = entry;
            return this;
        }

        /**
         * @return The new Sharable object built by this Builder.
         */
        public Sharable<T> build() {
            Sharable<T> sharable = new DefaultSharable<T>(names.toArray(new String[names.size()]), type,
                    handler, serializer, profileEntry, optional);
            ProfileEntry.register(sharable);
            return sharable;
        }
    }
}
