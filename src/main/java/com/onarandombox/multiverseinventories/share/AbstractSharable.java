package com.onarandombox.multiverseinventories.share;

import com.onarandombox.multiverseinventories.api.profile.PlayerProfile;
import org.bukkit.entity.Player;

/**
 * An abstract class used to define a value that can be shared between worlds and world groups in
 * Multiverse-Inventories.  By extending this class and implementing it's abstract methods you will have a way to
 * alter a player's data based on what Multiverse-Inventories world/world group they are in.
 *
 * @param <T> The type of data this Sharable represents.
 */
public abstract class AbstractSharable<T> implements Sharable<T> {

    private String[] names;
    private ProfileEntry profileEntry;
    private Class<T> type;

    /**
     * @param type           The class representing the type of data this Sharable represents.
     * @param name           The name of the sharable.  This will be what is displayed in groups in the config.
     * @param profileEntry   This represents where the sharable data will be stored in profile files.  null is allowed if
     *                       you do not wish the data to be stored automatically into profile files.
     * @param alternateNames These, along with name, are all strings allowed to specify that this sharabled is shared
     *                       in group setup.
     */
    public AbstractSharable(Class<T> type, String name, ProfileEntry profileEntry, String... alternateNames) {
        this.type = type;
        names = new String[alternateNames.length + 1];
        names[0] = name;
        System.arraycopy(alternateNames, 0, names, 1, alternateNames.length);
        this.profileEntry = profileEntry;
        Sharables.register(this);
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

    /**
     * {@inheritDoc}
     */
    @Override
    public final String[] getNames() {
        return names;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final ProfileEntry getProfileEntry() {
        return this.profileEntry;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return this.names[0];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Class<T> getType() {
        return this.type;
    }
}
