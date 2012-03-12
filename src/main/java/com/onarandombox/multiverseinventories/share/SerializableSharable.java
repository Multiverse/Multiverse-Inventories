package com.onarandombox.multiverseinventories.share;

import com.onarandombox.multiverseinventories.api.profile.PlayerProfile;
import org.bukkit.entity.Player;

/**
 * An abstract class used to define a value that can be shared between worlds and world groups in
 * Multiverse-Inventories.  By extending this class and implementing it's abstract methods you will have a way to
 * alter a player's data based on what Multiverse-Inventories world/world group they are in.  Using this over
 * {@link SimpleSharable} will cause the sharable's data to be persisted in the Multiverse-Inventories player files.
 *
 * @param <T> The type of data this Sharable represents.
 */
public abstract class SerializableSharable<T> extends SimpleSharable<T> {

    private String[] names;
    private ProfileEntry profileEntry;

    /**
     * @param type           The class representing the type of data this Sharable represents.
     * @param name           The name of the sharable.  This will be what is displayed in groups in the config.
     * @param profileEntry   This represents where the sharable data will be stored in profile files.
     * @param alternateNames These, along with name, are all strings allowed to specify that this sharabled is shared
     *                       in group setup.
     */
    public SerializableSharable(Class<T> type, String name, ProfileEntry profileEntry, String... alternateNames) {
        super(type, name, alternateNames);
        this.profileEntry = profileEntry;
        ProfileEntry.register(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract void updateProfile(PlayerProfile profile, Player player);

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract boolean updatePlayer(Player player, PlayerProfile profile);

    public final ProfileEntry getProfileEntry() {
        return this.profileEntry;
    }

    public T deserialize(Object obj) {
        return getType().cast(obj);
    }

    public Object serialize(T t) {
        return t;
    }
}
