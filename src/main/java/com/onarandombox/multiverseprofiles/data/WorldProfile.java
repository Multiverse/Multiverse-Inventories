package com.onarandombox.multiverseprofiles.data;

import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.multiverseprofiles.MultiverseProfiles;
import com.onarandombox.multiverseprofiles.util.ProfilesDebug;
import com.onarandombox.multiverseprofiles.util.ProfilesLog;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.*;

/**
 * @author dumptruckman
 */
public class WorldProfile implements ConfigurationSerializable {

    private HashMap<OfflinePlayer, PlayerProfile> playerData = new HashMap<OfflinePlayer, PlayerProfile>();
    private MultiverseWorld world;

    public WorldProfile(MultiverseWorld world) {
        this.world = world;
    }

    public Map<String, Object> serialize() {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("worldName", this.getMVWorld().getName());
        result.put("playerData", this.getPlayerData().values());

        return result;
    }

    public static WorldProfile deserialize(Map<String, Object> args) {
        WorldProfile worldProfile = new WorldProfile(MultiverseProfiles.getCore().getMVWorldManager().getMVWorld(args.get("worldName").toString()));
        Object object = args.get("playerData");
        if (object instanceof Collection) {
            ProfilesDebug.info("playerData IS Collection");
            Collection playerCollection = (Collection) object;
            for (Object collectionObject : playerCollection) {
                if (collectionObject instanceof PlayerProfile) {
                    ProfilesDebug.info("collectionObject IS PlayerProfile");
                    PlayerProfile playerProfile = (PlayerProfile) collectionObject;
                    if (playerProfile != null) {
                        worldProfile.addPlayerData(playerProfile);
                    } else {
                        ProfilesLog.warning("Unable to load a player's data for world: " + worldProfile.getMVWorld().getName());
                    }
                }
            }
        }
        ProfilesDebug.info("Deserialized WorldProfile for world: " + worldProfile.getMVWorld().getName());
        return worldProfile;
    }

    public MultiverseWorld getMVWorld() {
        return this.world;
    }

    public void setMVWorld(MultiverseWorld world) {
        this.world = world;
    }

    public List<WorldGroup> getWorldGroups() {
        return MultiverseProfiles.getWorldGroups().get(this.getMVWorld());
    }

    public HashMap<OfflinePlayer, PlayerProfile> getPlayerData() {
        return this.playerData;
    }

    public PlayerProfile getPlayerData(OfflinePlayer player) {
        PlayerProfile playerProfile = this.playerData.get(player);
        if (playerProfile == null) {
            playerProfile = new PlayerProfile(player);
            this.playerData.put(player, playerProfile);
        }
        return playerProfile;
    }

    public void addPlayerData(PlayerProfile playerProfile) {
        this.getPlayerData().put(playerProfile.getPlayer(), playerProfile);
    }
}
