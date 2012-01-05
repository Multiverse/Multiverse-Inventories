package com.onarandombox.multiverseprofiles.world;

import com.onarandombox.multiverseprofiles.player.PlayerProfile;
import com.onarandombox.multiverseprofiles.player.SimplePlayerProfile;
import com.onarandombox.multiverseprofiles.util.ProfilesLog;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author dumptruckman
 */
public class SimpleWorldProfile implements WorldProfile {

    private HashMap<OfflinePlayer, PlayerProfile> playerData = new HashMap<OfflinePlayer, PlayerProfile>();
    private World world;

    public SimpleWorldProfile(World world) {
        this.world = world;
    }

    public Map<String, Object> serialize() {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("worldName", this.getWorld().getName());
        result.put("playerData", this.getPlayerData().values());

        return result;
    }

    public static WorldProfile deserialize(Map<String, Object> args) {
        WorldProfile worldProfile = new SimpleWorldProfile(Bukkit.getWorld(args.get("worldName").toString()));
        Object object = args.get("playerData");
        if (object instanceof Collection) {
            ProfilesLog.info("playerData IS Collection");
            Collection playerCollection = (Collection) object;
            for (Object collectionObject : playerCollection) {
                if (collectionObject instanceof Map) {
                    ProfilesLog.info("collectionObject IS Map");
                    PlayerProfile playerProfile = SimplePlayerProfile.deserialize((Map) collectionObject);
                    if (playerProfile != null) {
                        worldProfile.addPlayerData(playerProfile);
                    } else {
                        ProfilesLog.warning("Unable to load a player's data for world: " + worldProfile.getWorld().getName());
                    }
                }
            }
        }
        ProfilesLog.info("Deserialized WorldProfile for world: " + worldProfile.getWorld().getName());
        return worldProfile;
    }

    public World getWorld() {
        return this.world;
    }

    public void setWorld(World world) {
        this.world = world;
    }

    public HashMap<OfflinePlayer, PlayerProfile> getPlayerData() {
        return this.playerData;
    }

    public PlayerProfile getPlayerData(OfflinePlayer player) {
        PlayerProfile playerProfile = this.playerData.get(player);
        if (playerProfile == null) {
            playerProfile = new SimplePlayerProfile(player);
            this.playerData.put(player, playerProfile);
        }
        return playerProfile;
    }

    public void addPlayerData(PlayerProfile playerProfile) {
        this.getPlayerData().put(playerProfile.getPlayer(), playerProfile);
    }
}
