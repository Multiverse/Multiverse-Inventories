package com.onarandombox.multiverseinventories.command;

import com.onarandombox.multiverseinventories.api.profile.WorldGroupProfile;
import com.onarandombox.multiverseinventories.util.Perm;
import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.locale.Message;
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
        this.setPermission(Perm.COMMAND_LIST.getPermission());
    }

    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        List<WorldGroupProfile> groups = this.plugin.getGroupManager().getGroups();
        String groupsString = "N/A";
        if (!groups.isEmpty()) {
            StringBuilder builder = new StringBuilder();
            for (WorldGroupProfile group : groups) {
                if (!builder.toString().isEmpty()) {
                    builder.append(", ");
                }
                builder.append(group.getName());
            }
            groupsString = builder.toString();
        }
        this.messager.normal(Message.LIST_GROUPS, sender, groupsString);
    }
}

