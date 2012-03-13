package com.onarandombox.multiverseinventories.share;


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
