package com.onarandombox.multiverseinventories.api.share;

import com.onarandombox.multiverseinventories.api.profile.PlayerProfile;
import org.bukkit.entity.Player;

public interface ISharable {

    /**
     * @param profile Updates the data of this profile according to the Sharable
     *                with the values of the player.
     * @param player  The player whose values will be used to update the given profile.
     */
    void updateProfile(PlayerProfile profile, Player player);

    /**
     * @param player  Updates the data of this player according to the Sharable
     *                with the values of the given profile.
     * @param profile The profile whose values will be used to update the give player.
     */
    void updatePlayer(Player player, PlayerProfile profile);
}
