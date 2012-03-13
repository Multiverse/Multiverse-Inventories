package com.onarandombox.multiverseinventories.share;

public interface SharableSerializer<T> {

    public T deserialize(Object obj);

    public Object serialize(T t);
}
