package com.onarandombox.multiverseinventories.commands_acf;

import com.onarandombox.acf.annotation.CommandAlias;
import com.onarandombox.acf.annotation.CommandCompletion;
import com.onarandombox.acf.annotation.Description;
import com.onarandombox.acf.annotation.Subcommand;
import com.onarandombox.acf.annotation.Syntax;
import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.WorldGroup;
import com.onarandombox.multiverseinventories.locale.Message;
import com.onarandombox.multiverseinventories.util.Perm;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

@CommandAlias("mvinv")
public class DeleteGroupCommand extends InventoriesCommand {

    public DeleteGroupCommand(MultiverseInventories plugin) {
        super(plugin);
        this.addPermission(Perm.COMMAND_CREATEGROUP);
    }

    @Subcommand("deletegroup")
    @Syntax("<group>")
    @CommandCompletion("@worldGroups")
    @Description("Deletes a World Group.")
    public void onDeleteGroupCommand(@NotNull CommandSender sender,

                                     @Syntax("<group>")
                                     @Description("Inventories group to delete.")
                                     @NotNull WorldGroup group) {

        this.plugin.getCore().getMVCommandManager().getQueueManager().addToQueue(
                sender,
                deleteGroupRunnable(sender, group),
            "Are you sure you want to delete group " + ChatColor.AQUA + group.getName() + ChatColor.WHITE + "?"
        );
    }

    private Runnable deleteGroupRunnable(@NotNull CommandSender sender,
                                         @NotNull WorldGroup group) {

        return () -> {
            this.plugin.getGroupManager().removeGroup(group);
            this.messager.normal(Message.GROUP_REMOVED, sender, group.getName());
        };
    }
}
