package org.mvplugins.multiverse.inventories.share;

public final class SharableEntry<T> {

    private final Sharable<T> sharable;
    private final T value;

    public SharableEntry(Sharable<T> sharable, T initialValue) {
        this.sharable = sharable;
        this.value = initialValue;
    }

    public Sharable<T> getSharable() {
        return sharable;
    }

    public T getValue() {
        return value;
    }
}
