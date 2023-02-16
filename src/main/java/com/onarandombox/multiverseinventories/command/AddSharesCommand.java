package com.onarandombox.multiverseinventories.command;

import com.onarandombox.acf.BukkitCommandIssuer;
import com.onarandombox.acf.annotation.CommandAlias;
import com.onarandombox.acf.annotation.CommandCompletion;
import com.onarandombox.acf.annotation.Description;
import com.onarandombox.acf.annotation.Subcommand;
import com.onarandombox.acf.annotation.Syntax;
import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.WorldGroup;
import com.onarandombox.multiverseinventories.locale.Message;
import com.onarandombox.multiverseinventories.share.Shares;
import com.onarandombox.multiverseinventories.util.Perm;
import org.jetbrains.annotations.NotNull;

@CommandAlias("mvinv")
public class AddSharesCommand extends InventoriesCommand {
    public AddSharesCommand(@NotNull MultiverseInventories plugin) {
        super(plugin);
        setPerm(Perm.COMMAND_ADDSHARES);
    }

    @Subcommand("addshares")
    @CommandCompletion("@shares @worldGroups")
    @Syntax("<share[,extra]> <group>")
    @Description("Add one or more shares to a group.")
    public void onAddSharesCommand(BukkitCommandIssuer issuer,

                                   @Syntax("<share[,extra]>")
                                   @Description("One or more sharables to add.")
                                   Shares shares,

                                   @Syntax("<group>")
                                   @Description("Group you want to add the shares to.")
                                   WorldGroup group
    ) {
        group.getShares().mergeShares(shares);
        this.plugin.getGroupManager().updateGroup(group);
        this.plugin.getMVIConfig().save();
        this.messager.normal(Message.NOW_SHARING, issuer.getIssuer(), group.getName(), group.getShares().toString());
    }
}
