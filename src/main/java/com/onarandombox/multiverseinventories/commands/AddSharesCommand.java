package com.onarandombox.multiverseinventories.commands;

import com.onarandombox.acf.annotation.CommandAlias;
import com.onarandombox.acf.annotation.CommandCompletion;
import com.onarandombox.acf.annotation.Description;
import com.onarandombox.acf.annotation.Subcommand;
import com.onarandombox.acf.annotation.Syntax;
import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.WorldGroup;
import com.onarandombox.multiverseinventories.locale.Message;
import com.onarandombox.multiverseinventories.share.Sharables;
import com.onarandombox.multiverseinventories.share.Shares;
import com.onarandombox.multiverseinventories.util.Perm;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

@CommandAlias("mvinv")
public class AddSharesCommand extends InventoriesCommand {

    public AddSharesCommand(MultiverseInventories plugin) {
        super(plugin);
        this.addPermission(Perm.COMMAND_ADDSHARES);
    }

    @Subcommand("addshares")
    @Syntax("<share[,extra]> <group>")
    @CommandCompletion("@shares @worldGroups")
    public void onAddSharesCommand(@NotNull CommandSender sender,

                                   @Syntax("<share[,extra]>")
                                   @Description("One or more sharables to add.")
                                   @NotNull String sharesString,

                                   @Syntax("<group>")
                                   @Description("Group you want to add the shares to.")
                                   @NotNull WorldGroup group) {

        //TODO: Clean this up.
        Shares newShares;
        Shares negativeShares;
        if (sharesString.contains("all") || sharesString.contains("everything") || sharesString.contains("*")) {
            newShares = Sharables.allOf();
            negativeShares = Sharables.noneOf();
        }
        else if (sharesString.contains("-all") || sharesString.contains("-everything") || sharesString.contains("-*")) {
            negativeShares = Sharables.allOf();
            newShares = Sharables.noneOf();
        }
        else {
            negativeShares = Sharables.noneOf();
            newShares = Sharables.noneOf();
            String[] shareList = sharesString.split(",");
            for (String share : shareList) {
                if (share.startsWith("-") && share.length() > 1) {
                    Shares shares = Sharables.lookup(share.substring(1));
                    if (shares == null) {
                        continue;
                    }
                    negativeShares.setSharing(shares, true);
                } else {
                    Shares shares = Sharables.lookup(share);
                    if (shares == null) {
                        continue;
                    }
                    newShares.setSharing(shares, true);
                }
            }
        }

        if (newShares.isEmpty() && negativeShares.isEmpty()) {
            this.messager.normal(Message.ERROR_NO_SHARES_SPECIFIED, sender, sharesString);
            return;
        }

        group.getShares().mergeShares(newShares);
        group.getShares().removeAll(negativeShares);
        this.plugin.getGroupManager().updateGroup(group);
        this.plugin.getMVIConfig().save();
        this.messager.normal(Message.NOW_SHARING, sender, group.getName(), group.getShares().toString());
    }
}
