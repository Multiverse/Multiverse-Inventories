package com.onarandombox.multiverseinventories.listener;

import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.group.WorldGroup;
import com.onarandombox.multiverseinventories.permission.MVIPerms;
import com.onarandombox.multiverseinventories.share.Shares;
import com.onarandombox.multiverseinventories.share.SimpleShares;
import com.onarandombox.multiverseinventories.util.MVIDebug;
import com.onarandombox.multiverseinventories.util.MVILog;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerListener;

import java.util.List;

/**
 * PlayerListener for MultiverseInventories.
 */
public class MVIPlayerListener extends PlayerListener {

    private MultiverseInventories plugin;

    public MVIPlayerListener(MultiverseInventories plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        World fromWorld = event.getFrom();
        World toWorld = player.getWorld();

        // A precaution..  Will this ever be true?
        if (fromWorld.equals(toWorld)) {
            MVIDebug.info("PlayerChangedWorldEvent fired when player travelling in same world.");
            return;
        }
        // Do nothing if dealing with non-managed worlds
        if (this.plugin.getCore().getMVWorldManager().getMVWorld(toWorld) == null
                || this.plugin.getCore().getMVWorldManager().getMVWorld(fromWorld) == null) {
            MVILog.debug("The from or to world is not managed by Multiverse!");
            return;
        }

        boolean hasBypass = MVIPerms.BYPASS_WORLD.hasBypass(player, toWorld.getName());
        if (hasBypass && this.plugin.getSettings().isUsingBypassPerms()) {
            return;
        }
        Shares currentShares = new SimpleShares();
        List<WorldGroup> toWorldGroups = this.plugin.getGroupManager().getWorldGroups(toWorld.getName());
        if (toWorldGroups != null) {
            for (WorldGroup toWorldGroup : toWorldGroups) {
                if (toWorldGroup.containsWorld(fromWorld.getName())) {
                    if (MVIPerms.BYPASS_GROUP.hasBypass(player, toWorldGroup.getName())) {
                        hasBypass = true;
                    } else {
                        currentShares.mergeShares(toWorldGroup.getShares());
                    }
                }
            }
        }
        if (hasBypass && this.plugin.getSettings().isUsingBypassPerms()) {
            currentShares.mergeShares(this.plugin.getBypassShares());
        }
        /*
        else {
            currentShares.mergeShares(this.plugin.getDefaultShares());
        }
        */

        this.plugin.handleSharing(player, fromWorld, toWorld, currentShares);
    }

}
