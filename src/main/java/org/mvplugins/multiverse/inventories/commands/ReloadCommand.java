package org.mvplugins.multiverse.inventories.commands;

import org.mvplugins.multiverse.core.commandtools.MVCommandIssuer;
import org.mvplugins.multiverse.inventories.MultiverseInventories;
import org.mvplugins.multiverse.core.commandtools.MVCommandManager;
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
    ReloadCommand(@NotNull MVCommandManager commandManager, @NotNull MultiverseInventories plugin) {
        super(commandManager);
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
