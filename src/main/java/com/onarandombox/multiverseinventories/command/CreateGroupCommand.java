package com.onarandombox.multiverseinventories.command;

import com.onarandombox.acf.BukkitCommandIssuer;
import com.onarandombox.acf.annotation.CommandAlias;
import com.onarandombox.acf.annotation.Conditions;
import com.onarandombox.acf.annotation.Description;
import com.onarandombox.acf.annotation.Subcommand;
import com.onarandombox.acf.annotation.Syntax;
import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.WorldGroup;
import com.onarandombox.multiverseinventories.locale.Message;
import com.onarandombox.multiverseinventories.util.Perm;
import org.jetbrains.annotations.NotNull;

@CommandAlias("mvinv")
public class CreateGroupCommand extends InventoriesCommand {
    public CreateGroupCommand(@NotNull MultiverseInventories plugin) {
        super(plugin);
        setPerm(Perm.COMMAND_CREATEGROUP);
    }

    @Subcommand("creategroup")
    @Syntax("<name>")
    @Description("Creates a new empty World Group with no worlds and no shares.")
    public void onCreateGroupCommand(BukkitCommandIssuer sender,

                                     @Conditions("newWorldGroupName")
                                     @Syntax("<name>")
                                     @Description("New group name to create.")
                                     String groupName
    ) {
        WorldGroup worldGroup = this.plugin.getGroupManager().newEmptyGroup(groupName);
        this.plugin.getGroupManager().updateGroup(worldGroup);
        this.messager.normal(Message.GROUP_CREATION_COMPLETE, sender.getIssuer());
    }
}
