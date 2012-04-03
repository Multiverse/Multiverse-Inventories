package com.onarandombox.multiverseinventories;

import com.onarandombox.multiverseinventories.api.GroupManager;
import com.onarandombox.multiverseinventories.api.Inventories;
import com.onarandombox.multiverseinventories.api.profile.ContainerType;
import com.onarandombox.multiverseinventories.api.profile.PlayerData;
import com.onarandombox.multiverseinventories.api.profile.PlayerProfile;
import com.onarandombox.multiverseinventories.api.profile.ProfileContainer;
import com.onarandombox.multiverseinventories.api.profile.WorldProfileManager;
import com.onarandombox.multiverseinventories.util.Logging;
import org.bukkit.OfflinePlayer;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Implementation of ProfileContainer using WeakHashMaps to keep memory usage to a minimum.
 */
abstract class WeakProfileContainer implements ProfileContainer {

    private Map<OfflinePlayer, PlayerProfile> playerData = new WeakHashMap<OfflinePlayer, PlayerProfile>();
    private Inventories inventories;
    private ContainerType type;

    public WeakProfileContainer(Inventories inventories, ContainerType type) {
        this.inventories = inventories;
        this.type = type;
    }

    /**
     * @return The map of bukkit players to their player profiles for this world player.
     */
    protected Map<OfflinePlayer, PlayerProfile> getPlayerData() {
        return this.playerData;
    }

    /**
     * @return The data class for MultiverseInventories.
     */
    protected PlayerData getData() {
        return this.getInventories().getData();
    }

    /**
     * @return The GroupManager for MultiverseInventories.
     */
    protected GroupManager getGroupManager() {
        return this.getInventories().getGroupManager();
    }

    /**
     * @return The WorldProfileManager for MultiverseInventories.
     */
    protected WorldProfileManager getProfileManager() {
        return this.getInventories().getWorldManager();
    }

    /**
     * @return The instance of MultiverseInventories.
     */
    protected Inventories getInventories() {
        return this.inventories;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PlayerProfile getPlayerData(OfflinePlayer player) {
        PlayerProfile playerProfile = this.playerData.get(player);
        if (playerProfile == null) {
            Logging.finer("Profile for " + player.getName() + " not cached, loading from disk...");
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void removePlayerData(OfflinePlayer player) {
        this.getPlayerData().remove(player);
        this.getData().removePlayerData(this.type, this.getDataName(), player.getName());
    }
}

