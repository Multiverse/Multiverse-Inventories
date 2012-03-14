package com.onarandombox.multiverseinventories.api.share;

/**
 * This represents how a Sharable's data will be serialized/deserialized.
 *
 * @param <T> The type of data the {@link Sharable} this belongs to represents.
 */
public interface SharableSerializer<T> {

    /**
     * This deserializes the data for a Sharable.  You must be expecting the type of data coming in in order to process
     * this.  That type will generally be the type that this serializes as with {@link #serialize(Object)}.
     *
     * @param obj The incoming (serialized) data to be deserialized.
     * @return The data represented by the Sharable this object represents in deserialized form.
     */
    T deserialize(Object obj);

    /**
     * This serializes the data for a Sharable.  The output is an Object but what you return is up to you, however,
     * this is limited by the constraints of the persistence method.  Generally, returning a String is the safest way
     * to serialize your data.  Most boxed primitives are accepted as well as Lists of boxed primitives and
     * Map<String, Object>.
     *
     * @param t The value of the data represented by the Sharable.
     * @return The serialized form of the data.
     */
    Object serialize(T t);
}
