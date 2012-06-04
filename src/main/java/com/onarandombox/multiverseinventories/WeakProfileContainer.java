package com.onarandombox.multiverseinventories;

import com.onarandombox.multiverseinventories.api.GroupManager;
import com.onarandombox.multiverseinventories.api.Inventories;
import com.onarandombox.multiverseinventories.api.profile.ContainerType;
import com.onarandombox.multiverseinventories.api.profile.PlayerData;
import com.onarandombox.multiverseinventories.api.profile.PlayerProfile;
import com.onarandombox.multiverseinventories.api.profile.ProfileContainer;
import com.onarandombox.multiverseinventories.api.profile.ProfileType;
import com.onarandombox.multiverseinventories.api.profile.WorldProfileManager;
import com.onarandombox.multiverseinventories.util.Logging;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Implementation of ProfileContainer using WeakHashMaps to keep memory usage to a minimum.
 */
abstract class WeakProfileContainer implements ProfileContainer {

    private Map<String, Map<ProfileType, PlayerProfile>> playerData = new WeakHashMap<String, Map<ProfileType, PlayerProfile>>();
    private Inventories inventories;
    private ContainerType type;

    public WeakProfileContainer(Inventories inventories, ContainerType type) {
        this.inventories = inventories;
        this.type = type;
    }

    /**
     * Gets the stored profiles for this player, mapped by ProfileType.
     *
     * @param name The name of player to get profile map for.
     * @return The profile map for the given player.
     */
    protected Map<ProfileType, PlayerProfile> getPlayerData(String name) {
        Map<ProfileType, PlayerProfile> data = this.playerData.get(name);
        if (data == null) {
            data = new HashMap<ProfileType, PlayerProfile>();
            this.playerData.put(name, data);
        }
        return data;
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
    public PlayerProfile getPlayerData(Player player) {
        ProfileType type;
        if (inventories.getMVIConfig().isUsingGameModeProfiles()) {
            type = ProfileTypes.forGameMode(player.getGameMode());
        } else {
            type = ProfileTypes.SURVIVAL;
        }
        return getPlayerData(type, player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PlayerProfile getPlayerData(ProfileType profileType, OfflinePlayer player) {
        Map<ProfileType, PlayerProfile> profileMap = this.getPlayerData(player.getName());
        PlayerProfile playerProfile = profileMap.get(profileType);
        if (playerProfile == null) {
            Logging.finer("Profile (" + profileType + ") for " + player.getName() + " not cached, loading from disk...");
            playerProfile = this.getData().getPlayerData(this.type,
                    this.getDataName(), profileType, player.getName());
            profileMap.put(profileType, playerProfile);
        }
        return playerProfile;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addPlayerData(PlayerProfile playerProfile) {
        this.getPlayerData(playerProfile.getPlayer().getName()).put(playerProfile.getProfileType(), playerProfile);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeAllPlayerData(OfflinePlayer player) {
        this.getPlayerData(player.getName()).clear();
        this.getData().removePlayerData(this.type, this.getDataName(), null, player.getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removePlayerData(ProfileType profileType, OfflinePlayer player) {
        this.getPlayerData(player.getName()).remove(profileType);
        this.getData().removePlayerData(this.type, this.getDataName(), profileType, player.getName());
    }
}

