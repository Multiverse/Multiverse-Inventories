package com.onarandombox.multiverseinventories.commands;

import com.onarandombox.acf.annotation.CommandAlias;
import com.onarandombox.acf.annotation.Description;
import com.onarandombox.acf.annotation.Subcommand;
import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.WorldGroup;
import com.onarandombox.multiverseinventories.locale.Message;
import com.onarandombox.multiverseinventories.util.Perm;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

@CommandAlias("mvinv")
public class ListCommand extends InventoriesCommand {

    public ListCommand(MultiverseInventories plugin) {
        super(plugin);
        this.addPermission(Perm.COMMAND_LIST);
    }

    @Subcommand("list")
    @Description("World and Group Information.")
    public void onListCommand(@NotNull CommandSender sender) {
        this.messager.normal(Message.LIST_GROUPS, sender, generateGroupString());
    }

    private String generateGroupString() {
        Collection<WorldGroup> groups = this.plugin.getGroupManager().getGroups();
        if (groups.isEmpty()) {
            return "N/A";
        }
        StringBuilder builder = new StringBuilder();
        for (WorldGroup group : groups) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(group.getName());
        }
        return builder.toString();
    }
}
