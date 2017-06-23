package com.onarandombox.multiverseinventories.command;

import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.WorldGroup;
import com.onarandombox.multiverseinventories.share.Sharable;
import com.onarandombox.multiverseinventories.share.Sharables;
import com.onarandombox.multiverseinventories.share.Shares;
import com.onarandombox.multiverseinventories.locale.Message;
import com.onarandombox.multiverseinventories.util.Perm;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * The /mvinv rmshares Command.
 * @deprecated Deprecated in favor of /mvinv group.
 */
@Deprecated
public class RemoveSharesCommand extends InventoriesCommand {

    public RemoveSharesCommand(MultiverseInventories plugin) {
        super(plugin);
        this.setName("Removes share(s) from a World Group.");
        this.setCommandUsage("/mvinv removeshares {SHARE[,EXTRA]} {GROUP}");
        this.setArgRange(2, 2);
        this.addKey("mvinv removeshares");
        this.addKey("mvinv rmshares");
        this.addKey("mvinv removeshare");
        this.addKey("mvinv rmshare");
        this.addKey("mvinv removes");
        this.addKey("mvinv rms");
        this.addKey("mvinvrs");
        this.addKey("mvinvrms");
        this.addKey("mvinvremoves");
        this.addKey("mvinvremoveshares");
        this.addKey("mvinvrmshares");
        this.addKey("mvinvremoveshare");
        this.addKey("mvinvrmshare");
        this.setPermission(Perm.COMMAND_RMSHARES.getPermission());
    }

    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        Shares newShares;
        if (args.get(0).contains("all") || args.get(0).contains("everything") || args.get(0).contains("*")) {
            newShares = Sharables.allOf();
        } else {
            newShares = Sharables.noneOf();
            String[] sharesString = args.get(0).split(",");
            for (String shareString : sharesString) {
                Shares shares = Sharables.lookup(shareString);
                if (shares == null) {
                    continue;
                }
                newShares.setSharing(shares, true);
            }
        }
        if (newShares.isEmpty()) {
            this.messager.normal(Message.ERROR_NO_SHARES_SPECIFIED, sender, args.get(0));
            return;
        }
        WorldGroup worldGroup = this.plugin.getGroupManager().getGroup(args.get(1));
        if (worldGroup == null) {
            this.messager.normal(Message.ERROR_NO_GROUP, sender, args.get(1));
            return;
        }
        for (Sharable sharable : newShares) {
            worldGroup.getShares().setSharing(sharable, false);
        }
        this.plugin.getGroupManager().updateGroup(worldGroup);
        this.plugin.getMVIConfig().save();
        this.messager.normal(Message.NOW_SHARING, sender, worldGroup.getName(),
                worldGroup.getShares().toString());
    }
}

