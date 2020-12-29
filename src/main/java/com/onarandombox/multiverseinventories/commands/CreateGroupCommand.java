package com.onarandombox.multiverseinventories.commands;

import com.onarandombox.acf.annotation.CommandAlias;
import com.onarandombox.acf.annotation.Conditions;
import com.onarandombox.acf.annotation.Description;
import com.onarandombox.acf.annotation.Single;
import com.onarandombox.acf.annotation.Subcommand;
import com.onarandombox.acf.annotation.Syntax;
import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.WorldGroup;
import com.onarandombox.multiverseinventories.locale.Message;
import com.onarandombox.multiverseinventories.util.Perm;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

@CommandAlias("mvinv")
public class CreateGroupCommand extends InventoriesCommand {

    public CreateGroupCommand(MultiverseInventories plugin) {
        super(plugin);
        this.addPermission(Perm.COMMAND_CREATEGROUP);
    }

    @Subcommand("creategroup")
    @Syntax("<name>")
    @Description("Creates a new empty World Group with no worlds and no shares.")
    public void onCreateGroupCommand(@NotNull CommandSender sender,

                                     @Syntax("<name>")
                                     @Description("New group name to create.")
                                     @NotNull @Single @Conditions("creatableGroupName") String groupName) {

        WorldGroup worldGroup = this.plugin.getGroupManager().newEmptyGroup(groupName);
        this.plugin.getGroupManager().updateGroup(worldGroup);
        this.messager.normal(Message.GROUP_CREATION_COMPLETE, sender);
    }
}
