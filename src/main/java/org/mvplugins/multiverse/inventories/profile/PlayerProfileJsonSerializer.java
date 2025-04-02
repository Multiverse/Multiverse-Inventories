package org.mvplugins.multiverse.inventories.profile;

import com.dumptruckman.minecraft.util.Logging;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.mvplugins.multiverse.inventories.profile.key.ProfileKey;
import org.mvplugins.multiverse.inventories.share.ProfileEntry;
import org.mvplugins.multiverse.inventories.share.Sharable;
import org.mvplugins.multiverse.inventories.util.DataStrings;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;

final class PlayerProfileJsonSerializer {

    static Map<String, Object> serialize(PlayerProfile playerProfile) {
        Map<String, Object> playerData = new LinkedHashMap<>();
        JSONObject jsonStats = new JSONObject();

        for (var entry : playerProfile.getData().entrySet()) {
            Sharable sharable = entry.getKey();
            Object sharableValue = entry.getValue();
            if (sharableValue == null) {
                continue;
            }

            var serializer = sharable.getSerializer();
            var profileEntry = sharable.getProfileEntry();
            if (serializer == null || profileEntry == null) {
                continue;
            }

            String fileTag = profileEntry.fileTag();
            Object serializedValue = serializer.serialize(sharableValue);
            if (profileEntry.isStat()) {
                jsonStats.put(fileTag, serializedValue);
            } else {
                playerData.put(fileTag, serializedValue);
            }
        }

        if (!jsonStats.isEmpty()) {
            playerData.put(DataStrings.PLAYER_STATS, jsonStats);
        }

        return playerData;
    }

    static PlayerProfile deserialize(ProfileKey pKey, Map playerData) {
        PlayerProfile profile = PlayerProfile.createPlayerProfile(pKey.getContainerType(), pKey.getDataName(),
                pKey.getProfileType(), pKey.getPlayerUUID(), pKey.getPlayerName());
        for (Object keyObj : playerData.keySet()) {
            String key = keyObj.toString();
            final Object value = playerData.get(key);
            if (value == null) {
                Logging.fine("Player data '" + key + "' is null for: " + pKey.getPlayerName());
                continue;
            }

            if (key.equalsIgnoreCase(DataStrings.PLAYER_STATS)) {
                if (value instanceof String) {
                    parseJsonPlayerStatsIntoProfile((String) value, profile);
                    continue;
                }
                if (value instanceof Map) {
                    parsePlayerStatsIntoProfile((Map) value, profile);
                } else {
                    Logging.warning("Could not parse stats for " + pKey.getPlayerName());
                }
                continue;
            }

            try {
                Sharable sharable = ProfileEntry.lookup(false, key);
                if (sharable == null) {
                    Logging.fine("Player fileTag '" + key + "' is unrecognized!");
                    continue;
                }
                profile.set(sharable, sharable.getSerializer().deserialize(playerData.get(key)));
            } catch (Exception e) {
                Logging.fine("Could not parse fileTag: '" + key + "' with value '" + playerData.get(key) + "'");
                Logging.getLogger().log(Level.FINE, "Exception: ", e);
                e.printStackTrace();
            }
        }
        Logging.finer("Created player profile from map for '" + pKey.getPlayerName() + "'.");
        return profile;
    }

    private static void parsePlayerStatsIntoProfile(Map stats, PlayerProfile profile) {
        for (Object key : stats.keySet()) {
            Sharable sharable = ProfileEntry.lookup(true, key.toString());
            if (sharable != null) {
                profile.set(sharable, sharable.getSerializer().deserialize(stats.get(key).toString()));
            } else {
                Logging.warning("Could not parse stat: '" + key + "' for player '"
                        + profile.getPlayerName() + "' for " + profile.getContainerType() + " '"
                        + profile.getContainerName() + "'");
            }
        }
    }

    private static void parseJsonPlayerStatsIntoProfile(String stats, PlayerProfile profile) {
        if (stats.isEmpty()) {
            return;
        }
        JSONObject jsonStats = null;
        try {
            jsonStats = (JSONObject) new JSONParser(JSONParser.USE_INTEGER_STORAGE | JSONParser.ACCEPT_TAILLING_SPACE).parse(stats);
        } catch (ParseException | ClassCastException e) {
            Logging.warning("Could not parse stats for player'" + profile.getPlayerName() + "' for " +
                    profile.getContainerType() + " '" + profile.getContainerName() + "': " + e.getMessage());
        }
        if (jsonStats == null) {
            Logging.warning("Could not parse stats for player'" + profile.getPlayerName() + "' for " +
                    profile.getContainerType() + " '" + profile.getContainerName() + "'");
            return;
        }
        parsePlayerStatsIntoProfile(jsonStats, profile);
    }

}
