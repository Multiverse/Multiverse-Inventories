package com.onarandombox.multiverseinventories.command;

import com.onarandombox.commandhandler.CommandHandler;
import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.WorldGroup;
import com.onarandombox.multiverseinventories.locale.Message;
import com.onarandombox.multiverseinventories.util.Perm;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * The /mvinv rmworld Command.
 * @deprecated Deprecated in favor of /mvinv group.
 */
@Deprecated
public class RemoveWorldCommand extends InventoriesCommand {

    public RemoveWorldCommand(MultiverseInventories plugin) {
        super(plugin);
        this.setName("Removes a World from a World Group.");
        this.setCommandUsage("/mvinv removeworld {WORLD} {GROUP} [--ignore-exist]");
        this.setArgRange(2, 3);
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
        String worldName = args.get(0);
        if (!CommandHandler.hasFlag("--ignore-exist", args) && Bukkit.getWorld(worldName) == null) {
            this.messager.normal(Message.ERROR_NO_WORLD, sender, args.get(0));
            return;
        }
        WorldGroup worldGroup = this.plugin.getGroupManager().getGroup(args.get(1));
        if (worldGroup == null) {
            this.messager.normal(Message.ERROR_NO_GROUP, sender, args.get(1));
            return;
        }
        if (!worldGroup.containsWorld(worldName)) {
            this.messager.normal(Message.WORLD_NOT_IN_GROUP, sender, worldName,
                    worldGroup.getName());
            return;
        }
        worldGroup.removeWorld(worldName);
        this.plugin.getGroupManager().updateGroup(worldGroup);
        this.plugin.getMVIConfig().save();
        this.messager.normal(Message.WORLD_REMOVED, sender, worldName,
                worldGroup.getName());
    }
}

