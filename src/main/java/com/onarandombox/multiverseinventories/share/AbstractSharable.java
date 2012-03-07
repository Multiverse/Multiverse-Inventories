package com.onarandombox.multiverseinventories.share;

import com.onarandombox.multiverseinventories.api.profile.PlayerProfile;
import org.bukkit.entity.Player;

public abstract class AbstractSharable<T> implements Sharable<T> {

    private String[] names;
    private ProfileEntry profileEntry;
    private Class<T> type;

    public AbstractSharable(Class<T> type, String name, ProfileEntry profileEntry, String... alternateNames) {
        this.type = type;
        names = new String[alternateNames.length + 1];
        names[0] = name;
        System.arraycopy(alternateNames, 0, names, 1, alternateNames.length);
        this.profileEntry = profileEntry;
        Sharables.register(this);
    }

    @Override
    public abstract void updateProfile(PlayerProfile profile, Player player);

    @Override
    public abstract void updatePlayer(Player player, PlayerProfile profile);

    /**
     * @return The names of this Sharable for setting as shared in the config.
     */
    @Override
    public String[] getNames() {
        return names;
    }

    @Override
    public ProfileEntry getProfileEntry() {
        return this.profileEntry;
    }

    public String toString() {
        return this.names[0];
    }

    @Override
    public Class<T> getType() {
        return this.type;
    }
}
