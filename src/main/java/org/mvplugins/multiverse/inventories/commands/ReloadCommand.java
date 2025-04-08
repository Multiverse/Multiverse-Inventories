package org.mvplugins.multiverse.inventories.commands;

import org.mvplugins.multiverse.core.command.MVCommandIssuer;
import org.mvplugins.multiverse.inventories.MultiverseInventories;
import org.mvplugins.multiverse.core.command.MVCommandManager;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandAlias;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandPermission;
import org.mvplugins.multiverse.external.acf.commands.annotation.Description;
import org.mvplugins.multiverse.external.acf.commands.annotation.Subcommand;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.inventories.util.MVInvi18n;

@Service
@CommandAlias("mvinv")
final class ReloadCommand extends InventoriesCommand {

    private final MultiverseInventories plugin;

    @Inject
    ReloadCommand(@NotNull MultiverseInventories plugin) {
        this.plugin = plugin;
    }

    @CommandAlias("mvinvreload")
    @Subcommand("reload")
    @CommandPermission("multiverse.inventories.reload")
    @Description("Reloads config file")
    void onReloadCommand(@NotNull MVCommandIssuer issuer) {
        this.plugin.reloadConfig();
        issuer.sendInfo(MVInvi18n.RELOAD_COMPLETE);
    }
}
