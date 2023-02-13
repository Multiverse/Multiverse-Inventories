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
import com.onarandombox.multiverseinventories.share.Sharable;
import com.onarandombox.multiverseinventories.share.Sharables;
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
    @CommandCompletion("@shares|@sharables @worldGroups")
    @Syntax("<share[,extra]> <group>")
    public void onRemoveSharesCommand(BukkitCommandIssuer issuer,

                                      @Syntax("<share[,extra]>")
                                      @Description("One or more sharables to remove.")
                                      String sharesString,

                                      @Syntax("<group>")
                                      @Description("Group you want to remove the shares from.")
                                      WorldGroup group
    ) {
        //TODO: Clean this up with context resolver.
        Shares newShares;
        if (sharesString.contains("all") || sharesString.contains("everything") || sharesString.contains("*")) {
            newShares = Sharables.allOf();
        } else {
            newShares = Sharables.noneOf();
            String[] shareList = sharesString.split(",");
            for (String shareString : shareList) {
                Shares shares = Sharables.lookup(shareString);
                if (shares == null) {
                    continue;
                }
                newShares.setSharing(shares, true);
            }
        }
        if (newShares.isEmpty()) {
            this.messager.normal(Message.ERROR_NO_SHARES_SPECIFIED, issuer.getIssuer(), sharesString);
            return;
        }

        for (Sharable<?> sharable : newShares) {
            group.getShares().setSharing(sharable, false);
        }
        this.plugin.getGroupManager().updateGroup(group);
        this.plugin.getMVIConfig().save();
        this.messager.normal(Message.NOW_SHARING, issuer.getIssuer(), group.getName(), group.getShares().toString());
    }
}
