package com.onarandombox.multiverseinventories.command;

import com.onarandombox.acf.BukkitCommandIssuer;
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
import org.jetbrains.annotations.NotNull;

@CommandAlias("mvinv")
public class ToggleCommand extends InventoriesCommand {
    public ToggleCommand(@NotNull MultiverseInventories plugin) {
        super(plugin);
        setPerm(Perm.COMMAND_ADDSHARES);
    }

    @Subcommand("toggle")
    @CommandCompletion("@sharables:scope=optional")
    @Syntax("<optional sharable>")
    @Description("Toggles a sharable on or off.")
    public void onToggleCommand(@NotNull BukkitCommandIssuer issuer,

                                @Conditions("optionalSharable")
                                @Syntax("<sharable>")
                                @Description("Optional sharable name.")
                                Sharable<?> targetSharable
    ) {
        if (this.plugin.getMVIConfig().getOptionalShares().contains(targetSharable)) {
            this.plugin.getMVIConfig().getOptionalShares().remove(targetSharable);
            this.messager.normal(Message.NOW_NOT_USING_OPTIONAL, issuer.getIssuer(), targetSharable.getNames()[0]);
        }
        else {
            this.plugin.getMVIConfig().getOptionalShares().add(targetSharable);
            this.messager.normal(Message.NOW_USING_OPTIONAL, issuer.getIssuer(), targetSharable.getNames()[0]);
        }
        this.plugin.getMVIConfig().save();
    }
}
