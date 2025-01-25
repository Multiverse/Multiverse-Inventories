package org.mvplugins.multiverse.inventories.profile.group;

import org.mvplugins.multiverse.inventories.MultiverseInventories;
import org.mvplugins.multiverse.inventories.profile.container.ContainerType;
import org.mvplugins.multiverse.inventories.profile.container.ProfileContainer;
import org.mvplugins.multiverse.inventories.profile.container.ProfileContainerStoreProvider;
import org.mvplugins.multiverse.inventories.share.Sharable;
import org.mvplugins.multiverse.inventories.share.Sharables;
import org.mvplugins.multiverse.inventories.share.Shares;
import org.bukkit.World;
import org.bukkit.event.EventPriority;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public final class WorldGroup {

    private final WorldGroupManager worldGroupManager;
    private final ProfileContainerStoreProvider profileContainerStoreProvider;
    private final String name;
    private final HashSet<String> worlds = new HashSet<>();
    private final Shares shares = Sharables.noneOf();

    private String spawnWorld = null;
    private EventPriority spawnPriority = EventPriority.NORMAL;

    WorldGroup(
            final WorldGroupManager worldGroupManager,
            final ProfileContainerStoreProvider profileContainerStoreProvider,
            final String name) {
        this.worldGroupManager = worldGroupManager;
        this.profileContainerStoreProvider = profileContainerStoreProvider;
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
        this.getWorlds().add(worldName.toLowerCase());
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
        worlds.forEach(worldName -> this.addWorld(worldName, false));
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
        this.getWorlds().remove(worldName.toLowerCase());
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
        this.worlds.clear();
        if (updateConfig) {
            worldGroupManager.updateGroup(this);
        }
    }

    /**
     * Retrieves all of the worlds in this World Group.
     *
     * @return The worlds of this World Group.
     */
    public Set<String> getWorlds() {
        return this.worlds;
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
        return getShares().isSharing(sharable);
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

    /**
     * @param worldName Name of world to check for.
     * @return True if specified world is part of this group.
     */
    public boolean containsWorld(String worldName) {
        return this.getWorlds().contains(worldName.toLowerCase());
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
        return WorldGroupManager.DEFAULT_GROUP_NAME.equals(getName());
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
        String[] worldsString = this.getWorlds().toArray(new String[this.getWorlds().size()]);
        for (int i = 0; i < worldsString.length; i++) {
            if (i != 0) {
                builder.append(", ");
            }
            builder.append(worldsString[i]);
        }
        builder.append("], Shares: [").append(this.getShares().toString()).append("]");
        if (this.getSpawnWorld() != null) {
            builder.append(", Spawn World: ").append(this.getSpawnWorld());
            builder.append(", Spawn Priority: ").append(this.getSpawnPriority().toString());
        }
        builder.append("}");
        return builder.toString();
    }
}
