package com.onarandombox.multiverseinventories;

import com.dumptruckman.minecraft.util.Logging;
import com.onarandombox.multiverseinventories.api.GroupManager;
import com.onarandombox.multiverseinventories.profile.ProfileTypes;
import com.onarandombox.multiverseinventories.util.data.PlayerData;
import com.onarandombox.multiverseinventories.profile.PlayerProfile;
import com.onarandombox.multiverseinventories.profile.container.ProfileContainer;
import com.onarandombox.multiverseinventories.profile.ProfileType;
import com.onarandombox.multiverseinventories.profile.WorldProfileManager;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Implementation of ProfileContainer using WeakHashMaps to keep memory usage to a minimum.
 */
abstract class WeakProfileContainer implements ProfileContainer {

    private Map<String, Map<ProfileType, PlayerProfile>> playerData = new WeakHashMap<>();
    private final MultiverseInventories inventories;

    WeakProfileContainer(MultiverseInventories inventories) {
        this.inventories = inventories;
    }

    /**
     * Gets the stored profiles for this player, mapped by ProfileType.
     *
     * @param name The name of player to get profile map for.
     * @return The profile map for the given player.
     */
    protected Map<ProfileType, PlayerProfile> getPlayerData(String name) {
        return this.playerData.computeIfAbsent(name, k -> new HashMap<>());
    }

    protected PlayerData getDataSource() {
        return this.getInventories().getData();
    }

    protected GroupManager getGroupManager() {
        return this.getInventories().getGroupManager();
    }

    protected WorldProfileManager getProfileManager() {
        return this.getInventories().getWorldManager();
    }

    protected MultiverseInventories getInventories() {
        return this.inventories;
    }

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

    @Override
    public PlayerProfile getPlayerData(ProfileType profileType, OfflinePlayer player) {
        Map<ProfileType, PlayerProfile> profileMap = this.getPlayerData(player.getName());
        PlayerProfile playerProfile = profileMap.get(profileType);
        if (playerProfile == null) {
            playerProfile = getDataSource().getPlayerData(getContainerType(),
                    getContainerName(), profileType, player.getName());
            Logging.finer("[%s - %s - %s - %s] not cached, loading from disk...",
                    profileType, getContainerType(), playerProfile.getContainerName(), player.getName());
            profileMap.put(profileType, playerProfile);
        }
        return playerProfile;
    }

    @Override
    public void addPlayerData(PlayerProfile playerProfile) {
        this.getPlayerData(playerProfile.getPlayer().getName()).put(playerProfile.getProfileType(), playerProfile);
    }

    @Override
    public void removeAllPlayerData(OfflinePlayer player) {
        this.getPlayerData(player.getName()).clear();
        this.getDataSource().removePlayerData(getContainerType(), getContainerName(), null, player.getName());
    }

    @Override
    public void removePlayerData(ProfileType profileType, OfflinePlayer player) {
        this.getPlayerData(player.getName()).remove(profileType);
        this.getDataSource().removePlayerData(getContainerType(), getContainerName(), profileType, player.getName());
    }
}

