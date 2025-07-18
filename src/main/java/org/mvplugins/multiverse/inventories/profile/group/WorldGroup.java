package org.mvplugins.multiverse.inventories.profile.group;

import com.dumptruckman.minecraft.util.Logging;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.ApiStatus;
import org.mvplugins.multiverse.core.utils.matcher.MatcherGroup;
import org.mvplugins.multiverse.inventories.config.InventoriesConfig;
import org.mvplugins.multiverse.inventories.profile.key.ContainerType;
import org.mvplugins.multiverse.inventories.profile.container.ProfileContainer;
import org.mvplugins.multiverse.inventories.profile.container.ProfileContainerStoreProvider;
import org.mvplugins.multiverse.inventories.share.Sharable;
import org.mvplugins.multiverse.inventories.share.Sharables;
import org.mvplugins.multiverse.inventories.share.Shares;
import org.bukkit.World;
import org.bukkit.event.EventPriority;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public final class WorldGroup {

    private final WorldGroupManager worldGroupManager;
    private final ProfileContainerStoreProvider profileContainerStoreProvider;
    private final InventoriesConfig inventoriesConfig;
    private final String name;
    private final Set<String> configWorlds = new LinkedHashSet<>();
    private final Shares shares = Sharables.noneOf();
    private final Shares disabledShares = Sharables.noneOf();
    private String spawnWorld = null;
    private EventPriority spawnPriority = EventPriority.NORMAL;

    // calculated values
    private MatcherGroup matcherGroup = new MatcherGroup();
    private Set<String> applicableWorlds = new LinkedHashSet<>();
    private Shares applicableShares = Sharables.noneOf();

    WorldGroup(
            final WorldGroupManager worldGroupManager,
            final ProfileContainerStoreProvider profileContainerStoreProvider,
            final InventoriesConfig inventoriesConfig,
            final String name) {
        this.worldGroupManager = worldGroupManager;
        this.profileContainerStoreProvider = profileContainerStoreProvider;
        this.inventoriesConfig = inventoriesConfig;
        this.name = name;
    }

    /**
     * Get the name of this World Group.
     *
     * @return Name of this World Group.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Adds a world to this world group and updates it in the Config.
     *
     * @param worldName The name of the world to add.
     */
    public void addWorld(String worldName) {
        this.addWorld(worldName, true);
    }

    /**
     * Adds a world to this world group and optionally updates it in the Config.
     *
     * @param worldName    The name of the world to add.
     * @param updateConfig True to update this group in the config.
     */
    public void addWorld(String worldName, boolean updateConfig) {
        this.configWorlds.add(worldName.toLowerCase());
        if (updateConfig) {
            worldGroupManager.updateGroup(this);
        }
    }

    /**
     * Convenience method to add a {@link org.bukkit.World} to this World Group.
     *
     * @param world The world to add.
     */
    public void addWorld(World world) {
        this.addWorld(world.getName());
    }

    /**
     * Convenience method to add multiple worlds to this World Group and updates it in the Config.
     *
     * @param worlds A collections of worlds to add.
     */
    public void addWorlds(Collection<String> worlds) {
        this.addWorlds(worlds, true);
    }

    /**
     * Convenience method to add multiple worlds to this World Group.
     *
     * @param worlds A collections of worlds to add.
     * @param updateConfig True to update this group in the config.
     */
    public void addWorlds(Collection<String> worlds, boolean updateConfig) {
        configWorlds.addAll(worlds.stream().map(String::toLowerCase).toList());
        if (updateConfig) {
            worldGroupManager.updateGroup(this);
        }
    }

    /**
     * Removes a world from this world group and updates the group in the Config.
     *
     * @param worldName The name of the world to remove.
     */
    public void removeWorld(String worldName) {
        this.removeWorld(worldName, true);
    }

    /**
     * Removes a world from this world group and optionally updates it in the Config.
     *
     * @param worldName    The name of the world to remove.
     * @param updateConfig True to update this group in the config.
     */
    public void removeWorld(String worldName, boolean updateConfig) {
        this.configWorlds.remove(worldName.toLowerCase());
        if (updateConfig) {
            worldGroupManager.updateGroup(this);
        }
    }

    /**
     * Convenience method to remove a {@link org.bukkit.World} from this World Group.
     *
     * @param world The world to remove.
     */
    public void removeWorld(World world) {
        this.removeWorld(world.getName());
    }

    /**
     * Removes multiple worlds from this World Group.
     *
     * @param removeWorlds  A collection of world names to remove.
     * @return True if any of the worlds were removed.
     */
    public boolean removeWorlds(Collection<String> removeWorlds) {
        return removeWorlds(removeWorlds, true);
    }

    /**
     * Removes multiple worlds from this World Group and optionally updates the group in the Config.
     *
     * @param removeWorlds  A collection of world names to remove.
     * @return True if any of the worlds were removed.
     */
    public boolean removeWorlds(Collection<String> removeWorlds, boolean updateConfig) {
        if (this.configWorlds.removeAll(removeWorlds.stream().map(String::toLowerCase).collect(Collectors.toSet()))) {
            if (updateConfig) {
                worldGroupManager.updateGroup(this);
            }
            return true;
        }
        return false;
    }

    /**
     * Remove all the worlds in this World Group.
     */
    public void removeAllWorlds() {
        this.removeAllWorlds(true);
    }

    /**
     * Remove all the worlds in this World Group.
     *
     * @param updateConfig  True to update this group in the config.
     */
    public void removeAllWorlds(boolean updateConfig) {
        this.configWorlds.clear();
        if (updateConfig) {
            worldGroupManager.updateGroup(this);
        }
    }

    /**
     * @param worldName Name of world to check for.
     * @return True if specified world is part of this group.
     */
    public boolean containsWorld(String worldName) {
        return this.applicableWorlds.contains(worldName.toLowerCase());
    }

    /**
     * Retrieves the set of worlds that were configured in the group config.
     *
     * @return The set of worlds that were configured in the group config.
     *
     * @since 5.2
     */
    @ApiStatus.AvailableSince("5.2")
    public Set<String> getConfigWorlds() {
        return this.configWorlds;
    }

    /**
     * Retrieves all the worlds applicable in this World Group, after parsing wildcard and regex matches.
     * Modifying this set will not change the worlds saved in the groups config.
     *
     * @return The worlds of this World Group.
     */
    public Set<String> getApplicableWorlds() {
        return this.applicableWorlds;
    }

    /**
     * Retrieves all of the worlds in this World Group.
     * <br />
     * In 5.2, this method returns the same as {@link #getApplicableWorlds()}.
     * To get the worlds string in groups config, use {@link #getConfigWorlds()} instead.
     *
     * @return The worlds of this World Group.
     *
     * @deprecated Use {@link #getApplicableWorlds()} instead.
     */
    @Deprecated(forRemoval = true, since = "5.2")
    public Set<String> getWorlds() {
        return this.applicableWorlds;
    }

    /**
     * Recalculates the applicable worlds for this World Group based on the configured worlds.
     */
    @ApiStatus.AvailableSince("5.2")
    public void recalculateApplicableWorlds() {
        this.matcherGroup = new MatcherGroup(configWorlds);
        this.applicableWorlds = Bukkit.getWorlds().stream().map(World::getName)
                .map(String::toLowerCase)
                .filter(matcherGroup::matches)
                .collect(Collectors.toSet());
    }

    void addApplicableWorld(String worldName) {
        if (this.matcherGroup.matches(worldName.toLowerCase())) {
            this.applicableWorlds.add(worldName.toLowerCase());
        }
    }

    void removeApplicableWorld(String worldName) {
        this.applicableWorlds.remove(worldName.toLowerCase());
    }

    /**
     * Checks if this group is sharing sharable. This will check both shares and negative shares of the group.
     * This is the preferred method for checking if a group shares something as shares may contain ALL shares while
     * ones indicated in negative shares means those aren't actually shared.
     *
     * @param sharable Sharable to check if sharing.
     * @return true is the sharable is shared for this group.
     */
    public boolean isSharing(Sharable sharable) {
        return getApplicableShares().isSharing(sharable);
    }

    /**
     * Retrieves the shares for this World Group. Any changes to this group must be subsequently saved to the data
     * source for the changes to be permanent.
     *
     * @return The shares for this World Group.
     */
    public Shares getShares() {
        return this.shares;
    }

    public Shares getDisabledShares() {
        return this.disabledShares;
    }

    public Shares getApplicableShares() {
        return this.applicableShares;
    }

    public void recalculateApplicableShares() {
        this.applicableShares = Sharables.fromShares(this.shares);
        this.applicableShares.removeAll(this.disabledShares);
        Shares disabledOptionalShares = Sharables.optionalOf();
        disabledOptionalShares.removeAll(this.inventoriesConfig.getActiveOptionalShares());
        this.applicableShares.removeAll(disabledOptionalShares);
        Logging.finest("Applicable shares for " + this.getName() + ": " + this.applicableShares);
    }

    /**
     * @return The name of the world that will be used as the spawn for this group.
     *         Or null if no world was specified as the group spawn world.
     */
    public String getSpawnWorld() {
        return this.spawnWorld;
    }

    /**
     * @param worldName The name of the world to set this groups spawn to.
     */
    public void setSpawnWorld(String worldName) {
        this.spawnWorld = worldName.toLowerCase();
    }

    /**
     * @return The priority for the respawn event that this spawn will act on.
     */
    public EventPriority getSpawnPriority() {
        return this.spawnPriority;
    }

    /**
     * @param priority The priority that will be used for respawning the player at
     *                 this group's spawn location if there is one set.
     */
    public void setSpawnPriority(EventPriority priority) {
        this.spawnPriority = priority;
    }

    /**
     * Is this a default group.
     *
     * @return true if this is the default group.
     */
    public boolean isDefault() {
        return AbstractWorldGroupManager.DEFAULT_GROUP_NAME.equals(getName());
    }

    /**
     * Returns the profile container for this group.
     *
     * @return the profile container for this group.
     */
    public ProfileContainer getGroupProfileContainer() {
        return profileContainerStoreProvider.getStore(ContainerType.GROUP).getContainer(getName());
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.getName()).append(": {Worlds: [");
        String[] worldsString = this.getConfigWorlds().toArray(new String[0]);
        for (int i = 0; i < worldsString.length; i++) {
            if (i != 0) {
                builder.append(", ");
            }
            builder.append(worldsString[i]);
        }
        builder.append("], Shares: [").append(this.getShares()).append("]");
        if (this.getSpawnWorld() != null) {
            builder.append(", Spawn World: ").append(this.getSpawnWorld());
            builder.append(", Spawn Priority: ").append(this.getSpawnPriority().toString());
        }
        builder.append("}");
        return builder.toString();
    }
}
