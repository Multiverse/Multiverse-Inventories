package com.onarandombox.multiverseinventories.command;

import com.onarandombox.acf.BukkitCommandIssuer;
import com.onarandombox.acf.annotation.CommandAlias;
import com.onarandombox.acf.annotation.Description;
import com.onarandombox.acf.annotation.Subcommand;
import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.locale.Message;
import com.onarandombox.multiverseinventories.util.Perm;
import org.jetbrains.annotations.NotNull;

@CommandAlias("mvinv")
public class ReloadCommand extends InventoriesCommand {
    public ReloadCommand(@NotNull MultiverseInventories plugin) {
        super(plugin);
        setPerm(Perm.COMMAND_RELOAD);
    }

    @Subcommand("reload")
    @Description("Reloads the config file.")
    public void onReloadCommand(BukkitCommandIssuer issuer) {
        this.plugin.reloadConfig();
        messager.good(Message.RELOAD_COMPLETE, issuer.getIssuer());
    }
}
