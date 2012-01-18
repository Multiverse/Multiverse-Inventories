package com.onarandombox.multiverseinventories.command;

import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.group.WorldGroup;
import com.onarandombox.multiverseinventories.locale.MultiverseMessage;
import com.onarandombox.multiverseinventories.permission.MVIPerms;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * The /mvi info Command.
 */
public class ListCommand extends InventoriesCommand {

    public ListCommand(MultiverseInventories plugin) {
        super(plugin);
        this.setName("World and Group Information");
        this.setCommandUsage("/mvinv list");
        this.setArgRange(0, 0);
        this.addKey("mvinv list");
        this.addKey("mvinvl");
        this.addKey("mvinvlist");
        this.setPermission(MVIPerms.COMMAND_LIST.getPerm());
    }

    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        List<WorldGroup> groups = this.getPlugin().getGroupManager().getGroups();
        String groupsString = "N/A";
        if (!groups.isEmpty()) {
            StringBuilder builder = new StringBuilder();
            for (WorldGroup group : groups) {
                if (!builder.toString().isEmpty()) {
                    builder.append(", ");
                }
                builder.append(group.getName());
            }
            groupsString = builder.toString();
        }
        this.getPlugin().getMessager().normal(MultiverseMessage.LIST_GROUPS, sender, groupsString);
    }
}
