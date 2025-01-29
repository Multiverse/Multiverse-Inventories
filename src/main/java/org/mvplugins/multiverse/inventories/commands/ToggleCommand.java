package org.mvplugins.multiverse.inventories.commands;

import org.mvplugins.multiverse.inventories.MultiverseInventories;
import org.mvplugins.multiverse.inventories.config.InventoriesConfig;
import org.mvplugins.multiverse.inventories.locale.Message;
import org.mvplugins.multiverse.inventories.share.Sharable;
import org.mvplugins.multiverse.inventories.share.Sharables;
import org.mvplugins.multiverse.inventories.share.Shares;
import org.bukkit.command.CommandSender;
import org.mvplugins.multiverse.core.commandtools.MVCommandManager;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandAlias;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandCompletion;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandPermission;
import org.mvplugins.multiverse.external.acf.commands.annotation.Description;
import org.mvplugins.multiverse.external.acf.commands.annotation.Single;
import org.mvplugins.multiverse.external.acf.commands.annotation.Subcommand;
import org.mvplugins.multiverse.external.acf.commands.annotation.Syntax;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Service;

@Service
@CommandAlias("mvinv")
class ToggleCommand extends InventoriesCommand {

    private final MultiverseInventories plugin;
    private final InventoriesConfig inventoriesConfig;

    @Inject
    ToggleCommand(
            @NotNull MVCommandManager commandManager,
            @NotNull MultiverseInventories plugin,
            @NotNull InventoriesConfig inventoriesConfig) {
        super(commandManager);
        this.plugin = plugin;
        this.inventoriesConfig = inventoriesConfig;
    }

    @CommandAlias("mvinvtoggle")
    @Subcommand("toggle")
    @CommandPermission("multiverse.inventories.addshares")
    @CommandCompletion("economy|last_location")
    @Syntax("<share-name>")
    @Description("Toggles the usage of optional sharables")
    void onToggleCommand(
            @NotNull CommandSender sender,

            @Single
            @Syntax("<share-name>")
            @Description("Share to toggle")
            @NotNull String shareName
    ) {
        Shares shares = Sharables.lookup(shareName.toLowerCase());
        if (shares == null) {
            this.plugin.getMessager().normal(Message.ERROR_NO_SHARES_SPECIFIED, sender);
            return;
        }
        boolean foundOpt = false;
        for (Sharable sharable : shares) {
            if (sharable.isOptional()) {
                foundOpt = true;
                if (inventoriesConfig.getOptionalShares().contains(sharable)) {
                    inventoriesConfig.getOptionalShares().remove(sharable);
                    this.plugin.getMessager().normal(Message.NOW_NOT_USING_OPTIONAL, sender, sharable.getNames()[0]);
                } else {
                    inventoriesConfig.getOptionalShares().add(sharable);
                    this.plugin.getMessager().normal(Message.NOW_USING_OPTIONAL, sender, sharable.getNames()[0]);
                }
            }
        }
        if (foundOpt) {
            inventoriesConfig.save();
        } else {
            this.plugin.getMessager().normal(Message.NO_OPTIONAL_SHARES, sender, shareName);
        }
    }
}
