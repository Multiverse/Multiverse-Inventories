package com.onarandombox.multiverseinventories.share;

import com.onarandombox.multiverseinventories.api.profile.PlayerProfile;
import org.bukkit.entity.Player;

public interface Sharable {

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
     */
    void updatePlayer(Player player, PlayerProfile profile);

    String[] getNames();
}
