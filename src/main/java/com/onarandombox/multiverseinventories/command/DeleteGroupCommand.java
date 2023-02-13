package com.onarandombox.multiverseinventories.command;

import com.onarandombox.MultiverseCore.commandtools.queue.QueuedCommand;
import com.onarandombox.acf.BukkitCommandIssuer;
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
import org.jetbrains.annotations.NotNull;

@CommandAlias("mvinv")
public class DeleteGroupCommand extends InventoriesCommand {
    public DeleteGroupCommand(@NotNull MultiverseInventories plugin) {
        super(plugin);
        setPerm(Perm.COMMAND_DELETEGROUP);
    }

    @Subcommand("deletegroup")
    @CommandCompletion("@worldGroups")
    @Syntax("<group>")
    @Description("Deletes a World Group.")
    public void onDeleteGroupCommand(BukkitCommandIssuer issuer,

                                     @Syntax("<group>")
                                     @Description("Inventories group to delete.")
                                     WorldGroup group
    ) {
        this.plugin.getCore().getMVCommandManager().getCommandQueueManager().addToQueue(new QueuedCommand(
                issuer,
                deleteGroupRunnable(issuer, group),
                "Are you sure you want to delete group " + ChatColor.AQUA + group.getName() + ChatColor.WHITE + "?" //TODO Localize
        ));
    }

    private Runnable deleteGroupRunnable(BukkitCommandIssuer issuer, WorldGroup group) {
        return () -> {
            this.plugin.getGroupManager().removeGroup(group);
            this.messager.normal(Message.GROUP_REMOVED, issuer.getIssuer(), group.getName());
        };
    }
}
