package com.onarandombox.multiverseinventories.share;

import com.onarandombox.multiverseinventories.profile.PersistingProfile;
import com.onarandombox.multiverseinventories.profile.PlayerProfile;
import com.onarandombox.multiverseinventories.profile.ProfileContainer;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * An interface for the handling of sharing.
 */
public interface ShareHandler {

    /**
     * @param container The group/world the player's data is associated with.
     * @param shares    What from this group needs to be saved.
     * @param profile   The player profile that will need data saved to.
     */
    void addFromProfile(ProfileContainer container, Shares shares, PlayerProfile profile);

    /**
     * @param container The group/world the player's data is associated with.
     * @param shares    What from this group needs to be loaded.
     * @param profile   The player profile that will need data loaded from.
     */
    void addToProfile(ProfileContainer container, Shares shares, PlayerProfile profile);

    /**
     * @return The groups the player is coming from.
     */
    List<PersistingProfile> getFromProfiles();

    /**
     * @return The groups the player is going to.
     */
    List<PersistingProfile> getToProfiles();

    /**
     * @return The world travelling from.
     */
    World getFromWorld();

    /**
     * @return The world travelling to.
     */
    World getToWorld();

    /**
     * @return The player involved in this sharing transaction.
     */
    Player getPlayer();

    /**
     * Finalizes the transfer from one world to another.  This handles the switching
     * inventories/stats for a player and persisting the changes.
     */
    void handleSharing();
}

