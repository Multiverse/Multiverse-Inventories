package com.onarandombox.multiverseinventories.commands_acf;

import com.onarandombox.acf.annotation.CommandAlias;
import com.onarandombox.acf.annotation.CommandCompletion;
import com.onarandombox.acf.annotation.Description;
import com.onarandombox.acf.annotation.Subcommand;
import com.onarandombox.acf.annotation.Syntax;
import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.locale.Message;
import com.onarandombox.multiverseinventories.share.Sharable;
import com.onarandombox.multiverseinventories.share.Sharables;
import com.onarandombox.multiverseinventories.share.Shares;
import com.onarandombox.multiverseinventories.util.Perm;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

@CommandAlias("mvinv")
public class ToggleCommand extends InventoriesCommand {

    public ToggleCommand(MultiverseInventories plugin) {
        super(plugin);
        this.addPermission(Perm.COMMAND_ADDSHARES);
    }

    @Subcommand("toggle")
    @Syntax("<share>")
    @CommandCompletion("")
    @Description("Toggles the usage of optional sharable.")
    public void onToggleCommand(@NotNull CommandSender sender,
                                @NotNull String targetSharable) {

        //TODO: Move this to command conditions.
        Shares shares = Sharables.lookup(targetSharable.toLowerCase());
        if (shares == null) {
            this.messager.normal(Message.ERROR_NO_SHARES_SPECIFIED, sender);
            return;
        }

        boolean foundOpt = false;
        for (Sharable<?> sharable : shares) {
            if (sharable.isOptional()) {
                foundOpt = true;
                if (this.plugin.getMVIConfig().getOptionalShares().contains(sharable)) {
                    this.plugin.getMVIConfig().getOptionalShares().remove(sharable);
                    this.messager.normal(Message.NOW_NOT_USING_OPTIONAL, sender, sharable.getNames()[0]);
                } else {
                    this.plugin.getMVIConfig().getOptionalShares().add(sharable);
                    this.messager.normal(Message.NOW_USING_OPTIONAL, sender, sharable.getNames()[0]);
                }
                break;
            }
        }

        if (foundOpt) {
            this.plugin.getMVIConfig().save();
        } else {
            this.messager.normal(Message.NO_OPTIONAL_SHARES, sender, targetSharable);
        }
    }
}
