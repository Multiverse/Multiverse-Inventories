package com.onarandombox.multiverseprofiles.listener;

import com.onarandombox.multiverseprofiles.MultiverseProfiles;
import com.onarandombox.multiverseprofiles.player.PlayerProfile;
import com.onarandombox.multiverseprofiles.util.ProfilesDebug;
import com.onarandombox.multiverseprofiles.world.Shares;
import com.onarandombox.multiverseprofiles.world.SimpleShares;
import com.onarandombox.multiverseprofiles.world.WorldGroup;
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

    MultiverseProfiles plugin;
    
    public ProfilesPlayerListener(MultiverseProfiles plugin) {
        this.plugin = plugin;
    }

    public void onPlayerLogin(PlayerLoginEvent event) {
    }

    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        World fromWorld = event.getFrom();
        World toWorld = player.getWorld();

        // A precaution..  Will this ever be true?
        if (fromWorld.equals(toWorld)) {
            ProfilesDebug.info("PlayerChangedWorldEvent fired when player travelling in same world.");
            return;
        }
        // Do nothing if dealing with non-managed worlds
        if (this.plugin.getCore().getMVWorldManager().getMVWorld(toWorld) == null ||
                this.plugin.getCore().getMVWorldManager().getMVWorld(fromWorld) == null) {
            ProfilesDebug.info("The from or to world is not managed by Multiverse!");
            return;
        }

        Shares currentShares = new SimpleShares();
        List<WorldGroup> toWorldGroups = this.plugin.getWorldGroups().get(toWorld);
        for (WorldGroup toWorldGroup : toWorldGroups) {
            if (toWorldGroup.getWorlds().contains(fromWorld)) {
                currentShares.mergeShares(toWorldGroup.getShares());
            }
        }

        PlayerProfile fromWorldProfile = this.plugin.getWorldProfile(fromWorld).getPlayerData(player);
        PlayerProfile toWorldProfile = this.plugin.getWorldProfile(toWorld).getPlayerData(player);

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
        
        this.plugin.getData().save(false);
    }
}
