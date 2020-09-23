package com.onarandombox.multiverseinventories;

import com.dumptruckman.minecraft.util.Logging;
import com.onarandombox.multiverseinventories.profile.ProfileDataSource;
import com.onarandombox.multiverseinventories.profile.WorldGroupManager;
import com.onarandombox.multiverseinventories.profile.ProfileTypes;
import com.onarandombox.multiverseinventories.profile.container.ContainerType;
import com.onarandombox.multiverseinventories.profile.PlayerProfile;
import com.onarandombox.multiverseinventories.profile.container.ProfileContainer;
import com.onarandombox.multiverseinventories.profile.ProfileType;
import com.onarandombox.multiverseinventories.profile.container.ProfileContainerStore;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Implementation of ProfileContainer using WeakHashMaps to keep memory usage to a minimum.
 */
final class WeakProfileContainer implements ProfileContainer {

    private Map<String, Map<ProfileType, PlayerProfile>> playerData = new WeakHashMap<>();
    private final MultiverseInventories inventories;
    private final String name;
    private final ContainerType type;

    WeakProfileContainer(MultiverseInventories inventories, String name, ContainerType type) {
        this.inventories = inventories;
        this.name = name;
        this.type = type;
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

    protected ProfileDataSource getDataSource() {
        return this.getInventories().getData();
    }

    protected WorldGroupManager getGroupManager() {
        return this.getInventories().getGroupManager();
    }

    protected ProfileContainerStore getProfileManager() {
        return this.getInventories().getWorldProfileContainerStore();
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
                    getContainerName(), profileType, player);
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
        this.getDataSource().removePlayerData(getContainerType(), getContainerName(), null, player);
    }

    @Override
    public void removePlayerData(ProfileType profileType, OfflinePlayer player) {
        this.getPlayerData(player.getName()).remove(profileType);
        this.getDataSource().removePlayerData(getContainerType(), getContainerName(), profileType, player);
    }

    @Override
    public String getContainerName() {
        return name;
    }

    @Override
    public ContainerType getContainerType() {
        return type;
    }
}

