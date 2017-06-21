package com.onarandombox.multiverseinventories;

import com.onarandombox.multiverseinventories.profile.container.GroupProfileContainer;
import com.onarandombox.multiverseinventories.api.share.Sharable;
import com.onarandombox.multiverseinventories.api.share.Sharables;
import com.onarandombox.multiverseinventories.api.share.Shares;
import org.bukkit.World;
import org.bukkit.event.EventPriority;

import java.util.HashSet;
import java.util.Set;

/**
 * Implementation of WorldGroupProfile.
 */
class DefaultGroupProfileContainer extends WeakProfileContainer implements GroupProfileContainer {

    private final String name;
    private final HashSet<String> worlds = new HashSet<String>();
    private final Shares shares = Sharables.noneOf();

    private String spawnWorld = null;
    private EventPriority spawnPriority = EventPriority.NORMAL;

    DefaultGroupProfileContainer(final MultiverseInventories inventories, final String name) {
        super(inventories);
        this.name = name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addWorld(String worldName) {
        this.addWorld(worldName, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addWorld(String worldName, boolean updateConfig) {
        this.getWorlds().add(worldName.toLowerCase());
        if (updateConfig) {
            getInventories().getGroupManager().updateGroup(this);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addWorld(World world) {
        this.addWorld(world.getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeWorld(String worldName) {
        this.removeWorld(worldName, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeWorld(String worldName, boolean updateConfig) {
        this.getWorlds().remove(worldName.toLowerCase());
        if (updateConfig) {
            getInventories().getGroupManager().updateGroup(this);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeWorld(World world) {
        this.removeWorld(world.getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> getWorlds() {
        return this.worlds;
    }

    @Override
    public boolean isSharing(Sharable sharable) {
        return getShares().isSharing(sharable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Shares getShares() {
        return this.shares;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsWorld(String worldName) {
        return this.getWorlds().contains(worldName.toLowerCase());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getContainerName() {
        return this.getName();
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

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSpawnWorld() {
        return this.spawnWorld;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSpawnWorld(String worldName) {
        this.spawnWorld = worldName.toLowerCase();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EventPriority getSpawnPriority() {
        return this.spawnPriority;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSpawnPriority(EventPriority priority) {
        this.spawnPriority = priority;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDefault() {
        return AbstractGroupProfileManager.DEFAULT_GROUP_NAME.equals(getName());
    }
}

