package com.onarandombox.multiverseinventories.commands_acf;

import com.onarandombox.acf.annotation.CommandAlias;
import com.onarandombox.acf.annotation.CommandCompletion;
import com.onarandombox.acf.annotation.Subcommand;
import com.onarandombox.acf.annotation.Syntax;
import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.WorldGroup;
import com.onarandombox.multiverseinventories.locale.Message;
import com.onarandombox.multiverseinventories.share.Sharable;
import com.onarandombox.multiverseinventories.share.Sharables;
import com.onarandombox.multiverseinventories.share.Shares;
import com.onarandombox.multiverseinventories.util.Perm;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

@CommandAlias("mvinv")
public class RemoveSharesCommand extends InventoriesCommand {

    public RemoveSharesCommand(MultiverseInventories plugin) {
        super(plugin);
        this.addPermission(Perm.COMMAND_ADDSHARES);
    }

    @Subcommand("removeshares")
    @Syntax("<share[,extra]> <group>")
    @CommandCompletion("@shares @worldGroups")
    public void onRemoveSharesCommand(@NotNull CommandSender sender, 
                                      @NotNull String sharesString, 
                                      @NotNull WorldGroup group) {

        //TODO: Clean this up.
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
            this.messager.normal(Message.ERROR_NO_SHARES_SPECIFIED, sender, sharesString);
            return;
        }

        for (Sharable<?> sharable : newShares) {
            group.getShares().setSharing(sharable, false);
        }
        this.plugin.getGroupManager().updateGroup(group);
        this.plugin.getMVIConfig().save();
        this.messager.normal(Message.NOW_SHARING, sender, group.getName(), group.getShares().toString());
    }
}
