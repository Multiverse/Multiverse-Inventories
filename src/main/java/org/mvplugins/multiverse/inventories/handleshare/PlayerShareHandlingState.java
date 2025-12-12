package org.mvplugins.multiverse.inventories.handleshare;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.inventories.share.Sharable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Keeps track of players who are currently having their sharable handling processed.
 * <br />
 * This is used to prevent infinite loops when updating sharables that may trigger events themselves or
 * when suppressing notifications during the handling process.
 *
 * @since 5.3
 */
@ApiStatus.AvailableSince("5.3")
@Service
public final class PlayerShareHandlingState {

    private final Map<UUID, AffectedProfiles> playerAffectedProfiles;

    @Inject
    PlayerShareHandlingState() {
         this.playerAffectedProfiles = new HashMap<>();
    }

    void setPlayerAffectedProfiles(Player player, AffectedProfiles status) {
        this.playerAffectedProfiles.put(player.getUniqueId(), status);
    }

    void removePlayerAffectedProfiles(Player player) {
        this.playerAffectedProfiles.remove(player.getUniqueId());
    }

    /**
     * Checks if the given player is currently having the given sharable handled.
     *
     * @param player    The player to check.
     * @param sharable  The sharable to check.
     * @return True if the player is having the sharable handled, false otherwise.
     *
     * @since 5.3
     */
    @ApiStatus.AvailableSince("5.3")
    public boolean isHandlingSharable(Player player, Sharable<?> sharable) {
        AffectedProfiles status = this.playerAffectedProfiles.get(player.getUniqueId());
        return status != null && status.isShareToRead(sharable);
    }
}
