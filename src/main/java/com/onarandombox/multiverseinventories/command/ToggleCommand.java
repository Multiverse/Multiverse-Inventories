package com.onarandombox.multiverseinventories.command;

import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.api.share.Sharable;
import com.onarandombox.multiverseinventories.api.share.Sharables;
import com.onarandombox.multiverseinventories.api.share.Shares;
import com.onarandombox.multiverseinventories.locale.Message;
import com.onarandombox.multiverseinventories.util.Perm;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * The /mvi info Command.
 */
public class ToggleCommand extends InventoriesCommand {

    public ToggleCommand(MultiverseInventories plugin) {
        super(plugin);
        this.setName("Toggles the usage of optional sharables");
        this.setCommandUsage("/mvinv toggle {SHARE}");
        this.setArgRange(1, 1);
        this.addKey("mvinv toggle");
        this.addKey("mvinv t");
        this.setPermission(Perm.COMMAND_ADDSHARES.getPermission());
    }

    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        Shares shares = Sharables.lookup(args.get(0).toLowerCase());
        if (shares == null) {
            this.messager.normal(Message.ERROR_NO_SHARES_SPECIFIED, sender);
            return;
        }
        boolean foundOpt = false;
        for (Sharable sharable : shares) {
            if (sharable.isOptional()) {
                foundOpt = true;
                if (this.plugin.getMVIConfig().getOptionalShares().contains(sharable)) {
                    this.plugin.getMVIConfig().getOptionalShares().remove(sharable);
                    this.messager.normal(Message.NOW_NOT_USING_OPTIONAL, sender, sharable.getNames()[0]);
                } else {
                    this.plugin.getMVIConfig().getOptionalShares().add(sharable);
                    this.messager.normal(Message.NOW_USING_OPTIONAL, sender, sharable.getNames()[0]);
                }
            }
        }
        if (foundOpt) {
            this.plugin.getMVIConfig().save();
        } else {
            this.messager.normal(Message.NO_OPTIONAL_SHARES, sender, args.get(0));
        }
    }
}

