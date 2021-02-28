package com.onarandombox.multiverseinventories.command;

import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.groups.WorldGroup;
import com.onarandombox.multiverseinventories.locale.Message;
import com.onarandombox.multiverseinventories.util.Perm;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * The /mvinv deletegroup Command.
 */
public class DeleteGroupCommand extends InventoriesCommand {

    public DeleteGroupCommand(MultiverseInventories plugin) {
        super(plugin);
        this.setName("Deletes a World Group.");
        this.setCommandUsage("/mvinv deletegroup {NAME}");
        this.setArgRange(1, 1);
        this.addKey("mvinv deletegroup");
        this.addKey("mvinv deleteg");
        this.addKey("mvinv dg");
        this.addKey("mvinvdeletegroup");
        this.addKey("mvinvdeleteg");
        this.addKey("mvinvdg");
        this.setPermission(Perm.COMMAND_DELETEGROUP.getPermission());
    }

    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        WorldGroup worldGroup = this.plugin.getGroupManager().getGroup(args.get(0));
        if (worldGroup == null) {
            this.messager.normal(Message.ERROR_NO_GROUP, sender, args.get(0));
            return;
        }

        this.plugin.getGroupManager().removeGroup(worldGroup);
        this.messager.normal(Message.GROUP_REMOVED, sender, args.get(0));
    }
}

