package com.onarandombox.multiverseprofiles.listener;

import com.onarandombox.multiverseprofiles.MultiverseProfiles;
import com.onarandombox.multiverseprofiles.data.PlayerProfile;
import com.onarandombox.multiverseprofiles.data.Shares;
import com.onarandombox.multiverseprofiles.data.WorldGroup;
import com.onarandombox.multiverseprofiles.util.ProfilesLog;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerLoginEvent;

import java.util.List;

/**
 * @author dumptruckman
 */
public class ProfilesPlayerListener extends PlayerListener {

    public void onPlayerLogin(PlayerLoginEvent event) {
    }

    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        World fromWorld = event.getFrom();
        World toWorld = player.getWorld();

        // A precaution..  Will this ever be true?
        if (fromWorld.equals(toWorld)) {
            ProfilesLog.debug("PlayerChangedWorldEvent fired when player travelling in same world.");
            return;
        }
        // Do nothing if dealing with non-managed worlds
        if (MultiverseProfiles.getCore().getMVWorldManager().getMVWorld(toWorld) == null ||
                MultiverseProfiles.getCore().getMVWorldManager().getMVWorld(fromWorld) == null) {
            ProfilesLog.debug("The from or to world is not managed by Multiverse!");
            return;
        }

        Shares currentShares = new Shares();
        List<WorldGroup> toWorldGroups = MultiverseProfiles.getWorldGroups().get(toWorld);
        for (WorldGroup toWorldGroup : toWorldGroups) {
            if (toWorldGroup.getWorlds().contains(fromWorld)) {
                currentShares.mergeShares(toWorldGroup.getShares());
            }
        }

        PlayerProfile fromWorldProfile = MultiverseProfiles.getWorldProfile(fromWorld).getPlayerData(player);
        PlayerProfile toWorldProfile = MultiverseProfiles.getWorldProfile(toWorld).getPlayerData(player);

        // persist current stats for previous world if not sharing
        // then load any saved data
        if (!currentShares.isSharingInventory()) {
            fromWorldProfile.setInventoryContents(player.getInventory().getContents());
            fromWorldProfile.setArmorContents(player.getInventory().getArmorContents());
            player.getInventory().clear();
            player.getInventory().setContents(toWorldProfile.getInventoryContents());
            player.getInventory().setArmorContents(toWorldProfile.getArmorContents());
        }
        if (!currentShares.isSharingHealth()) {
            fromWorldProfile.setHealth(player.getHealth());
            player.setHealth(toWorldProfile.getHealth());
        }
        if (!currentShares.isSharingHunger()) {
            fromWorldProfile.setFoodLevel(player.getFoodLevel());
            fromWorldProfile.setExhaustion(player.getExhaustion());
            fromWorldProfile.setSaturation(player.getSaturation());
            player.setFoodLevel(toWorldProfile.getFoodLevel());
            player.setExhaustion(toWorldProfile.getExhaustion());
            player.setSaturation(toWorldProfile.getSaturation());
        }
        if (!currentShares.isSharingExp()) {
            fromWorldProfile.setExp(player.getExp());
            fromWorldProfile.setLevel(player.getLevel());
            player.setExp(toWorldProfile.getExp());
            player.setLevel(toWorldProfile.getLevel());
        }
        if (!currentShares.isSharingEffects()) {
            // Where is the effects API??
        }
    }
}
