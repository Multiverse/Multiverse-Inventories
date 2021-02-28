package com.onarandombox.multiverseinventories;

import com.onarandombox.multiverseinventories.profile.container.ProfileContainer;
import com.onarandombox.multiverseinventories.share.Sharable;
import com.onarandombox.multiverseinventories.share.Sharables;
import com.onarandombox.multiverseinventories.share.Shares;
import com.onarandombox.multiverseinventories.util.WorldsSet;
import org.bukkit.World;
import org.bukkit.event.EventPriority;

import java.util.function.Consumer;

public final class WorldGroup {

    private final MultiverseInventories plugin;
    private final String name;
    private final WorldsSet worlds = new WorldsSet();
    private final Shares shares = Sharables.noneOf();

    private String spawnWorld = null;
    private EventPriority spawnPriority = EventPriority.NORMAL;

    WorldGroup(final MultiverseInventories inventories, final String name) {
        this.plugin = inventories;
        this.name = name;
    }

    /**
     * Run changes to the World Group, then save it to data source for the changes to be permanent.
     *
     * @param modifications Consume any changes to the {@link WorldGroup}.
     */
    public void modify(Consumer<WorldGroup> modifications) {
        modifications.accept(this);
        this.save();
    }

    /**
     * Save World Group to data source for the changes to be permanent.
     */
    public void save() {
        this.plugin.getGroupManager().updateGroup(this);
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
     *
     * @deprecated Use getWorlds().add(String).
     */
    @Deprecated
    public void addWorld(String worldName) {
        this.addWorld(worldName, true);
    }

    /**
     * Adds a world to this world group and optionally updates it in the Config.
     *
     * @param worldName    The name of the world to add.
     * @param updateConfig True to update this group in the config.
     *
     * @deprecated Use getWorlds().add(String).
     */
    @Deprecated
    public void addWorld(String worldName, boolean updateConfig) {
        this.getWorlds().add(worldName.toLowerCase());
        if (updateConfig) {
            plugin.getGroupManager().updateGroup(this);
        }
    }

    /**
     * Convenience method to add a {@link org.bukkit.World} to this World Group.
     *
     * @param world The world to add.
     *
     * @deprecated Use getWorlds().add(World).
     */
    @Deprecated
    public void addWorld(World world) {
        this.addWorld(world.getName());
    }

    /**
     * Removes a world from this world group and updates the group in the Config.
     *
     * @param worldName The name of the world to remove.
     *
     * @deprecated Use getWorlds().remove(String).
     */
    @Deprecated
    public void removeWorld(String worldName) {
        this.removeWorld(worldName, true);
    }

    /**
     * Removes a world from this world group and optionally updates it in the Config.
     *
     * @param worldName    The name of the world to remove.
     * @param updateConfig True to update this group in the config.
     *
     * @deprecated Use getWorlds().remove(String).
     */
    @Deprecated
    public void removeWorld(String worldName, boolean updateConfig) {
        this.getWorlds().remove(worldName.toLowerCase());
        if (updateConfig) {
            plugin.getGroupManager().updateGroup(this);
        }
    }

    /**
     * Convenience method to remove a {@link org.bukkit.World} from this World Group.
     *
     * @param world The world to remove.
     *
     * @deprecated Use getWorlds().remove(World).
     */
    @Deprecated
    public void removeWorld(World world) {
        this.removeWorld(world.getName());
    }

    /**
     * Retrieves all of the worlds in this World Group. Any changes made are not permanently saved to data source.
     * <br>
     * To permanently save changes, either do {@link #save()} after this method or call this method in
     * Consumer of {@link #modify(Consumer)}.
     *
     * @return The worlds of this World Group.
     */
    public WorldsSet getWorlds() {
        return this.worlds;
    }

    /**
     * Checks if this group is sharing sharable. This will check both shares and negative shares of the group.
     * This is the preferred method for checking if a group shares something as shares may contain ALL shares while
     * ones indicated in negative shares means those aren't actually shared.
     *
     * @param sharable Sharable to check if sharing.
     * @return true is the sharable is shared for this group.
     *
     * @deprecated Use getShares().isSharing(Sharable).
     */
    @Deprecated
    public boolean isSharing(Sharable sharable) {
        return getShares().isSharing(sharable);
    }

    /**
     * Retrieves the shares for this World Group. Any changes made are not permanently saved to data source.
     * <br>
     * To permanently save changes, either do {@link #save()} after this method or call this method in
     * Consumer of {@link #modify(Consumer)}.
     *
     * @return The shares for this World Group.
     */
    public Shares getShares() {
        return this.shares;
    }

    /**
     * @param worldName Name of world to check for.
     * @return True if specified world is part of this group.
     *
     * @deprecated Use getWorlds().contains(String).
     */
    @Deprecated
    public boolean containsWorld(String worldName) {
        return this.getWorlds().contains(worldName.toLowerCase());
    }

    /**
     * Gets spawn world of this World Group.
     *
     * @return The name of the world that will be used as the spawn for this group.
     *         Or null if no world was specified as the group spawn world.
     */
    public String getSpawnWorld() {
        return this.spawnWorld;
    }

    /**
     * Sets spawn world of this World Group. Changes are not permanently saved to data source.
     * <br>
     * To permanently save changes, either do {@link #save()} after this method or call this method in
     * Consumer of {@link #modify(Consumer)}.
     *
     * @param worldName The name of the world to set this groups spawn to.
     */
    public void setSpawnWorld(String worldName) {
        this.spawnWorld = worldName.toLowerCase();
    }

    /**
     * Gets event priority to be used during player respawn.
     *
     * @return The priority for the respawn event that this spawn will act on.
     */
    public EventPriority getSpawnPriority() {
        return this.spawnPriority;
    }

    /**
     * Sets event priority to be used during player respawn. Changes are not permanently saved to data source.
     * <br>
     * To permanently save changes, either do {@link #save()} after this method or call this method in
     * Consumer of {@link #modify(Consumer)}.
     *
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
     * Gets the profile container for this group.
     *
     * @return the profile container for this group.
     */
    public ProfileContainer getGroupProfileContainer() {
        return plugin.getGroupProfileContainerStore().getContainer(getName());
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder()
                .append(this.getName()).append(": {Worlds: [")
                .append(this.getWorlds().toString())
                .append("], Shares: [").append(this.getShares().toString()).append("]");
        if (this.getSpawnWorld() != null) {
            builder.append(", Spawn World: ").append(this.getSpawnWorld())
                    .append(", Spawn Priority: ").append(this.getSpawnPriority().toString());
        }
        builder.append("}");
        return builder.toString();
    }
}
