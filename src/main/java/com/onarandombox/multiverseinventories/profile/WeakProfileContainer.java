package com.onarandombox.multiverseinventories.profile;

import com.onarandombox.multiverseinventories.data.MVIData;
import org.bukkit.OfflinePlayer;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Implementation of ProfileContainer using WeakHashMaps to keep memory usage to a minimum.
 */
public abstract class WeakProfileContainer implements ProfileContainer {

    private Map<OfflinePlayer, PlayerProfile> playerData = new WeakHashMap<OfflinePlayer, PlayerProfile>();
    private MVIData data;
    private ProfileType type;

    public WeakProfileContainer(MVIData data, ProfileType type) {
        this.data = data;
        this.type = type;
    }

    /**
     * @return The map of bukkit players to their player profiles for this world profile.
     */
    protected Map<OfflinePlayer, PlayerProfile> getPlayerData() {
        return this.playerData;
    }

    /**
     * @return The data class for MultiverseInventories.
     */
    protected MVIData getData() {
        return this.data;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PlayerProfile getPlayerData(OfflinePlayer player) {
        PlayerProfile playerProfile = this.playerData.get(player);
        if (playerProfile == null) {
            playerProfile = this.getData().getPlayerData(this.type,
                    this.getDataName(), player.getName());
            this.playerData.put(player, playerProfile);
        }
        return playerProfile;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addPlayerData(PlayerProfile playerProfile) {
        this.getPlayerData().put(playerProfile.getPlayer(), playerProfile);
    }
}
