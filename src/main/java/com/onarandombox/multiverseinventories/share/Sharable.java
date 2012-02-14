package com.onarandombox.multiverseinventories.share;

import com.onarandombox.multiverseinventories.api.profile.PlayerProfile;
import org.bukkit.entity.Player;

import java.util.Map;

public interface Sharable<P extends PlayerProfile> {

    /**
     * This method is called during share handling (aka PlayerChangeWorldEvent).  It will perform updates to
     * the {@link PlayerProfile} based on the data contained in the {@link Player}
     *
     * @param profile Updates the data of this profile according to the Sharable
     *                with the values of the player.
     * @param player  The player whose values will be used to update the given profile.
     */
    void updateProfile(P profile, Player player);

    /**
     * This method is called during share handling (aka PlayerChangeWorldEvent).  It will perform updates to
     * the {@link Player} based on the data contained in the {@link PlayerProfile}
     *
     * @param player  Updates the data of this player according to the Sharable
     *                with the values of the given profile.
     * @param profile The profile whose values will be used to update the give player.
     */
    void updatePlayer(Player player, P profile);

    String[] getNames();
    
    void addToMap(P profile, Map<String, Object> playerData);
    
    void addToProfile(Map<String, Object> playerData, P profile);
}
