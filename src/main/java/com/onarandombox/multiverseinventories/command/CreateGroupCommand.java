package com.onarandombox.multiverseinventories.command;

import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.WorldGroup;
import com.onarandombox.multiverseinventories.locale.Message;
import com.onarandombox.multiverseinventories.util.Perm;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * The /mvinv creategroup Command.
 */
public class CreateGroupCommand extends InventoriesCommand {

    public CreateGroupCommand(MultiverseInventories plugin) {
        super(plugin);
        this.setName("Creates a new World Group with no worlds and no shares.");
        this.setCommandUsage("/mvinv creategroup {NAME}");
        this.setArgRange(1, 1);
        this.addKey("mvinv creategroup");
        this.addKey("mvinv createg");
        this.addKey("mvinv cg");
        this.addKey("mvinvcreategroup");
        this.addKey("mvinvcreateg");
        this.addKey("mvinvcg");
        this.setPermission(Perm.COMMAND_CREATEGROUP.getPermission());
    }

    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        WorldGroup worldGroup = this.plugin.getGroupManager().getGroup(args.get(0));
        if (worldGroup != null) {
            this.messager.normal(Message.GROUP_EXISTS, sender, args.get(0));
            return;
        }

        worldGroup = this.plugin.getGroupManager().newEmptyGroup(args.get(0));
        worldGroup.save();

        this.messager.normal(Message.GROUP_CREATION_COMPLETE, sender);
    }
}

