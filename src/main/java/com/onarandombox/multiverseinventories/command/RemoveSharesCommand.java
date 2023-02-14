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
public class RemoveSharesCommand extends InventoriesCommand {
    public RemoveSharesCommand(@NotNull MultiverseInventories plugin) {
        super(plugin);
        setPerm(Perm.COMMAND_ADDSHARES);
    }

    @Subcommand("removeshares")
    @CommandCompletion("@shares @worldGroups")
    @Syntax("<share[,extra]> <group>")
    public void onRemoveSharesCommand(BukkitCommandIssuer issuer,

                                      @Syntax("<share[,extra]>")
                                      @Description("One or more sharables to remove.")
                                      Shares shares,

                                      @Syntax("<group>")
                                      @Description("Group you want to remove the shares from.")
                                      WorldGroup group
    ) {
        group.getShares().setSharing(shares, false);
        this.plugin.getGroupManager().updateGroup(group);
        this.plugin.getMVIConfig().save();
        this.messager.normal(Message.NOW_SHARING, issuer.getIssuer(), group.getName(), group.getShares().toString());
    }
}
