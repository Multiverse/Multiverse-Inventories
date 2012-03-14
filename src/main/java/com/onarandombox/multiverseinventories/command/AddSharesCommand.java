package com.onarandombox.multiverseinventories.command;

import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.api.profile.WorldGroupProfile;
import com.onarandombox.multiverseinventories.api.share.Sharables;
import com.onarandombox.multiverseinventories.api.share.Shares;
import com.onarandombox.multiverseinventories.locale.Message;
import com.onarandombox.multiverseinventories.util.Perm;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * The /mvi info Command.
 */
public class AddSharesCommand extends InventoriesCommand {

    public AddSharesCommand(MultiverseInventories plugin) {
        super(plugin);
        this.setName("Adds share(s) to a World Group.");
        this.setCommandUsage("/mvinv addshares {SHARE[,EXTRA]} {GROUP}");
        this.setArgRange(2, 2);
        this.addKey("mvinv addshares");
        this.addKey("mvinv addshare");
        this.addKey("mvinv adds");
        this.addKey("mvinvas");
        this.addKey("mvinvadds");
        this.addKey("mvinvaddshares");
        this.addKey("mvinvaddshare");
        this.setPermission(Perm.COMMAND_ADDSHARES.getPermission());
    }

    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        Shares newShares;
        Shares negativeShares;
        if (args.get(0).contains("all") || args.get(0).contains("everything") || args.get(0).contains("*")) {
            newShares = Sharables.allOf();
            negativeShares = Sharables.noneOf();
        } else if (args.get(0).contains("-all") || args.get(0).contains("-everything") || args.get(0).contains("-*")) {
            negativeShares = Sharables.allOf();
            newShares = Sharables.noneOf();
        } else {
            negativeShares = Sharables.noneOf();
            newShares = Sharables.noneOf();
            String[] sharesString = args.get(0).split(",");
            for (String shareString : sharesString) {
                if (shareString.startsWith("-") && shareString.length() > 1) {
                    Shares shares = Sharables.lookup(shareString.substring(1));
                    if (shares == null) {
                        continue;
                    }
                    negativeShares.setSharing(shares, true);
                } else {
                    Shares shares = Sharables.lookup(shareString);
                    if (shares == null) {
                        continue;
                    }
                    newShares.setSharing(shares, true);
                }
            }
        }
        if (newShares.isEmpty() && negativeShares.isEmpty()) {
            this.messager.normal(Message.ERROR_NO_SHARES_SPECIFIED, sender, args.get(0));
            return;
        }
        WorldGroupProfile worldGroup = this.plugin.getGroupManager().getGroup(args.get(1));
        if (worldGroup == null) {
            this.messager.normal(Message.ERROR_NO_GROUP, sender, args.get(1));
            return;
        }
        worldGroup.getShares().mergeShares(newShares);
        worldGroup.getNegativeShares().mergeShares(negativeShares);
        this.plugin.getMVIConfig().updateWorldGroup(worldGroup);
        this.plugin.getMVIConfig().save();
        this.messager.normal(Message.NOW_SHARING, sender, worldGroup.getName(),
                worldGroup.getShares().toString(), worldGroup.getNegativeShares().toString());
    }
}

