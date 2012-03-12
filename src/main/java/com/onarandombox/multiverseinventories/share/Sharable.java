package com.onarandombox.multiverseinventories.share;

import com.onarandombox.multiverseinventories.api.profile.PlayerProfile;
import org.bukkit.entity.Player;

/**
 * An interface for any attribute that can be shared between worlds in a world group.
 *
 * @param <T> The type of data that this sharable represents.
 */
public interface Sharable<T> {

    /**
     * This method is called during share handling (aka PlayerChangeWorldEvent).  It will perform updates to
     * the {@link PlayerProfile} based on the data contained in the {@link Player}
     *
     * @param profile Updates the data of this profile according to the Sharable
     *                with the values of the player.
     * @param player  The player whose values will be used to update the given profile.
     */
    void updateProfile(PlayerProfile profile, Player player);

    /**
     * This method is called during share handling (aka PlayerChangeWorldEvent).  It will perform updates to
     * the {@link Player} based on the data contained in the {@link PlayerProfile}
     *
     * @param player  Updates the data of this player according to the Sharable
     *                with the values of the given profile.
     * @param profile The profile whose values will be used to update the give player.
     * @return True if player was updated from existing profile.  False if default was used (new profile).
     */
    boolean updatePlayer(Player player, PlayerProfile profile);

    /**
     * @return The names of this Sharable for setting as shared in the config.
     */
    String[] getNames();

    Class<T> getType();

    //T deserialize(Object obj);

    //Object serialize(T t);
}
