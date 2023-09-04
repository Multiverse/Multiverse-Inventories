package com.onarandombox.multiverseinventories.command;

import com.onarandombox.commandhandler.CommandHandler;
import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.WorldGroup;
import com.onarandombox.multiverseinventories.locale.Message;
import com.onarandombox.multiverseinventories.util.Perm;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * The /mvinv addworld Command.
 * @deprecated Deprecated in favor of /mvinv group.
 */
@Deprecated
public class AddWorldCommand extends InventoriesCommand {

    public AddWorldCommand(MultiverseInventories plugin) {
        super(plugin);
        this.setName("Adds a World to a World Group.");
        this.setCommandUsage("/mvinv addworld {WORLD} {GROUP} [--ignore-exist]");
        this.setArgRange(2, 3);
        this.addKey("mvinv addworld");
        this.addKey("mvinv addw");
        this.addKey("mvinvaw");
        this.addKey("mvinvaddw");
        this.addKey("mvinvaddworld");
        this.setPermission(Perm.COMMAND_ADDWORLD.getPermission());
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
        if (worldGroup.containsWorld(worldName)) {
            this.messager.normal(Message.WORLD_ALREADY_EXISTS, sender, worldName,
                    worldGroup.getName());
            return;
        }
        worldGroup.addWorld(worldName);
        this.plugin.getGroupManager().updateGroup(worldGroup);
        this.plugin.getMVIConfig().save();
        this.messager.normal(Message.WORLD_ADDED, sender, worldName,
                worldGroup.getName());
    }
}

