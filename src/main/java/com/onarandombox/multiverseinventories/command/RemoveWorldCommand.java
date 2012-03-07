package com.onarandombox.multiverseinventories.command;

import com.onarandombox.multiverseinventories.api.profile.WorldGroupProfile;
import com.onarandombox.multiverseinventories.util.Perm;
import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.locale.Message;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * The /mvi info Command.
 */
public class RemoveWorldCommand extends InventoriesCommand {

    public RemoveWorldCommand(MultiverseInventories plugin) {
        super(plugin);
        this.setName("Removes a World from a World Group.");
        this.setCommandUsage("/mvinv removeworld {WORLD} {GROUP}");
        this.setArgRange(2, 2);
        this.addKey("mvinv removeworld");
        this.addKey("mvinv rmworld");
        this.addKey("mvinv removew");
        this.addKey("mvinv rmw");
        this.addKey("mvinvrw");
        this.addKey("mvinvrmw");
        this.addKey("mvinvremovew");
        this.addKey("mvinvremoveworld");
        this.addKey("mvinvrmworld");
        this.setPermission(Perm.COMMAND_RMWORLD.getPermission());
    }

    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        World world = Bukkit.getWorld(args.get(0));
        if (world == null) {
            this.messager.normal(Message.ERROR_NO_WORLD, sender, args.get(0));
            return;
        }
        WorldGroupProfile worldGroup = this.plugin.getGroupManager().getGroup(args.get(1));
        if (worldGroup == null) {
            this.messager.normal(Message.ERROR_NO_GROUP, sender, args.get(1));
            return;
        }
        if (!worldGroup.containsWorld(world.getName())) {
            this.messager.normal(Message.WORLD_NOT_IN_GROUP, sender, world.getName(),
                    worldGroup.getName());
            return;
        }
        worldGroup.removeWorld(world);
        this.plugin.getMVIConfig().updateWorldGroup(worldGroup);
        this.plugin.getMVIConfig().save();
        this.messager.normal(Message.WORLD_REMOVED, sender, world.getName(),
                worldGroup.getName());
    }
}

