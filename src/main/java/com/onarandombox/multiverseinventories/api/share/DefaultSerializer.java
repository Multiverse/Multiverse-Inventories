package com.onarandombox.multiverseinventories.api.share;

/**
 * The default Sharable serializer.  It performs no special tasks on the objects being sent to persistence, they are
 * sent as is.
 *
 * @param <T> The type of data this serializer serializes.
 */
final class DefaultSerializer<T> implements SharableSerializer<T> {

    private Class<T> type;

    public DefaultSerializer(Class<T> type) {
        this.type = type;
    }

    private Class<T> getType() {
        return this.type;
    }

    @Override
    public T deserialize(Object obj) {
        return getType().cast(obj);
    }

    @Override
    public Object serialize(T t) {
        return t;
    }
}
