package com.onarandombox.multiverseinventories.command;

import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.group.WorldGroup;
import com.onarandombox.multiverseinventories.locale.MultiverseMessage;
import com.onarandombox.multiverseinventories.permission.MVIPerms;
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
        this.setPermission(MVIPerms.COMMAND_RMWORLD.getPerm());
    }

    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        World world = Bukkit.getWorld(args.get(0));
        if (world == null) {
            this.getPlugin().getMessager().normal(MultiverseMessage.ERROR_NO_WORLD, sender, args.get(0));
            return;
        }
        WorldGroup worldGroup = this.getPlugin().getGroupManager().getGroup(args.get(1));
        if (worldGroup == null) {
            this.getPlugin().getMessager().normal(MultiverseMessage.ERROR_NO_GROUP, sender, args.get(1));
            return;
        }
        if (!worldGroup.containsWorld(world.getName())) {
            this.getPlugin().getMessager().normal(MultiverseMessage.WORLD_NOT_IN_GROUP, sender, world.getName(),
                    worldGroup.getName());
            return;
        }
        worldGroup.removeWorld(world);
        this.getPlugin().getSettings().save();
        this.getPlugin().getMessager().normal(MultiverseMessage.WORLD_REMOVED, sender, world.getName(),
                worldGroup.getName());
    }
}

