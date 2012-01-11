package com.onarandombox.multiverseinventories.listener;

import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.group.WorldGroup;
import com.onarandombox.multiverseinventories.permission.MIPerms;
import com.onarandombox.multiverseinventories.share.Shares;
import com.onarandombox.multiverseinventories.share.SimpleShares;
import com.onarandombox.multiverseinventories.util.MIDebug;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerListener;

import java.util.List;

/**
 * PlayerListener for MultiverseInventories.
 */
public class MIPlayerListener extends PlayerListener {

    private MultiverseInventories plugin;

    public MIPlayerListener(MultiverseInventories plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        World fromWorld = event.getFrom();
        World toWorld = player.getWorld();

        // A precaution..  Will this ever be true?
        if (fromWorld.equals(toWorld)) {
            MIDebug.info("PlayerChangedWorldEvent fired when player travelling in same world.");
            return;
        }
        // Do nothing if dealing with non-managed worlds
        if (this.plugin.getCore().getMVWorldManager().getMVWorld(toWorld) == null
                || this.plugin.getCore().getMVWorldManager().getMVWorld(fromWorld) == null) {
            MIDebug.info("The from or to world is not managed by Multiverse!");
            return;
        }

        boolean hasBypass = MIPerms.BYPASS_WORLD.hasBypass(player, toWorld.getName());
        if (hasBypass) {
            return;
        }
        Shares currentShares = new SimpleShares();
        List<WorldGroup> toWorldGroups = this.plugin.getGroupManager().getWorldGroups(toWorld.getName());
        if (toWorldGroups != null) {
            for (WorldGroup toWorldGroup : toWorldGroups) {
                if (toWorldGroup.getWorlds().contains(fromWorld.getName())) {
                    if (MIPerms.BYPASS_GROUP.hasBypass(player, toWorldGroup.getName())) {
                        hasBypass = true;
                    } else {
                        currentShares.mergeShares(toWorldGroup.getShares());
                    }
                }
            }
        }
        if (hasBypass) {
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
