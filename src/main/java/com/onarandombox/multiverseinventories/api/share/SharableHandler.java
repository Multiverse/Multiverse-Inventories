package com.onarandombox.multiverseinventories.api.share;

import com.onarandombox.multiverseinventories.api.profile.PlayerProfile;
import org.bukkit.entity.Player;

/**
 * This class is used to handle the transition of data from a player profile to a player and vice versa, typically
 * when changing worlds.
 *
 * @param <T> The type of data the {@link Sharable} this belongs to represents.
 */
public interface SharableHandler<T> {

    /**
     * This method is called during share handling (aka PlayerChangeWorldEvent).  It will perform updates to
     * the {@link com.onarandombox.multiverseinventories.api.profile.PlayerProfile} based on the data contained in the {@link org.bukkit.entity.Player}
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
}
