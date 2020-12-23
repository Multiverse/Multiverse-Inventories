package com.onarandombox.multiverseinventories.commands_acf;

import com.onarandombox.acf.annotation.CommandAlias;
import com.onarandombox.acf.annotation.CommandCompletion;
import com.onarandombox.acf.annotation.Conditions;
import com.onarandombox.acf.annotation.Description;
import com.onarandombox.acf.annotation.Subcommand;
import com.onarandombox.acf.annotation.Syntax;
import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.locale.Message;
import com.onarandombox.multiverseinventories.share.Sharable;
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
    @CommandCompletion("@optionalSharables")
    @Description("Toggles the usage of optional sharable.")
    public void onToggleCommand(@NotNull CommandSender sender,
                                @NotNull @Conditions("optional") Sharable<?> targetSharable) {

        //TODO: on|off toggle instead?
        if (this.plugin.getMVIConfig().getOptionalShares().contains(targetSharable)) {
            this.plugin.getMVIConfig().getOptionalShares().remove(targetSharable);
            this.messager.normal(Message.NOW_NOT_USING_OPTIONAL, sender, targetSharable.getNames()[0]);
        }
        else {
            this.plugin.getMVIConfig().getOptionalShares().add(targetSharable);
            this.messager.normal(Message.NOW_USING_OPTIONAL, sender, targetSharable.getNames()[0]);
        }

        this.plugin.getMVIConfig().save();
    }
}
