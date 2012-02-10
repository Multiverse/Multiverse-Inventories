package com.onarandombox.multiverseinventories.command;

import com.onarandombox.multiverseinventories.api.profile.WorldGroupProfile;
import com.onarandombox.multiverseinventories.util.MVIPerms;
import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.locale.Message;
import com.onarandombox.multiverseinventories.api.share.Sharable;
import com.onarandombox.multiverseinventories.api.share.Shares;
import com.onarandombox.multiverseinventories.api.share.SimpleShares;
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
        this.setPermission(MVIPerms.COMMAND_ADDSHARES.getPerm());
    }

    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        Shares newShares;
        if (args.get(0).contains("all") || args.get(0).contains("everything") || args.get(0).contains("*")) {
            newShares = new SimpleShares(Sharable.all());
        } else {
            newShares = new SimpleShares();
            String[] sharesString = args.get(0).split(",");
            for (String shareString : sharesString) {
                Sharable sharable = Sharable.lookup(shareString);
                if (sharable == null) {
                    continue;
                }
                newShares.setSharing(sharable, true);
            }
        }
        if (newShares.getSharables().isEmpty()) {
            this.getPlugin().getMessager().normal(Message.ERROR_NO_SHARES_SPECIFIED, sender, args.get(0));
            return;
        }
        WorldGroupProfile worldGroup = this.getPlugin().getGroupManager().getGroup(args.get(1));
        if (worldGroup == null) {
            this.getPlugin().getMessager().normal(Message.ERROR_NO_GROUP, sender, args.get(1));
            return;
        }
        worldGroup.getShares().mergeShares(newShares);
        this.getPlugin().getMVIConfig().save();
        this.getPlugin().getMessager().normal(Message.NOW_SHARING, sender, worldGroup.getName(),
                worldGroup.getShares().toString());
    }
}

