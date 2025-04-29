package org.mvplugins.multiverse.inventories.profile.data;

import org.mvplugins.multiverse.inventories.share.Sharable;
import org.mvplugins.multiverse.inventories.share.Shares;

import java.util.Map;

public interface ProfileData {

    /**
     * Gets an immutable empty profile data constant.
     *
     * @return An empty profile data.
     */
    static ProfileData empty() {
        return EmptyProfileData.getInstance();
    }

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

    /**
     * Updates this profile with the data from another profile data.
     *
     * @param snapshot The snapshot data to update from.
     */
    void update(ProfileData snapshot);

    /**
     * Updates this profile with the data from another profile data for a specific set of {@link Sharable}s.
     * @param snapshot  The snapshot data to update from.
     * @param shares    The set of {@link Sharable}s to update.
     */
    void update(ProfileData snapshot, Shares shares);

    /**
     * Checks whether this profile contains any data.
     *
     * @return True if the profile is empty.
     */
    boolean isEmpty();
}
