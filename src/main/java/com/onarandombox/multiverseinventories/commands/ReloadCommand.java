package com.onarandombox.multiverseinventories.commands;

import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.locale.Message;
import org.bukkit.command.CommandSender;
import org.mvplugins.multiverse.core.commandtools.MVCommandManager;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandAlias;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandPermission;
import org.mvplugins.multiverse.external.acf.commands.annotation.Description;
import org.mvplugins.multiverse.external.acf.commands.annotation.Subcommand;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.external.jvnet.hk2.annotations.Service;

@Service
@CommandAlias("mvinv")
class ReloadCommand extends InventoriesCommand {

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
    void onReloadCommand(@NotNull CommandSender sender) {
        this.plugin.reloadConfig();
        this.plugin.getMessager().normal(Message.RELOAD_COMPLETE, sender);
    }
}
