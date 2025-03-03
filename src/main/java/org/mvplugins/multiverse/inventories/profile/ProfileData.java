package org.mvplugins.multiverse.inventories.profile;

import org.mvplugins.multiverse.inventories.share.Sharable;

import java.util.Map;

public interface ProfileData {
    /**
     * Retrieves the profile's value of the {@link Sharable} passed in.
     *
     * @param sharable Represents the key for the data wanted from the profile.
     * @param <T>      This indicates the type of return value to be expected.
     * @return The value of the sharable for this profile. Null if no value is set.
     */
    <T> T get(Sharable<T> sharable);

    /**
     * Sets the profile's value for the {@link Sharable} passed in.
     *
     * @param sharable Represents the key for the data to store.
     * @param value    The value of the data.
     * @param <T>      The type of value to be expected.
     */
    <T> void set(Sharable<T> sharable, T value);

    Map<Sharable, Object> getData();
}
