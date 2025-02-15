package org.mvplugins.multiverse.inventories.commands;

import org.mvplugins.multiverse.core.commandtools.MVCommandIssuer;
import org.mvplugins.multiverse.inventories.MultiverseInventories;
import org.mvplugins.multiverse.inventories.config.InventoriesConfig;
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
import org.mvplugins.multiverse.inventories.util.MVInvi18n;

import static org.mvplugins.multiverse.core.locale.message.MessageReplacement.replace;

@Service
@CommandAlias("mvinv")
final class ToggleCommand extends InventoriesCommand {

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
            @NotNull MVCommandIssuer issuer,

            @Single
            @Syntax("<share-name>")
            @Description("Share to toggle")
            @NotNull String shareName
    ) {
        Shares shares = Sharables.lookup(shareName.toLowerCase());
        if (shares == null) {
            issuer.sendError(MVInvi18n.ERROR_NOSHARESSPECIFIED);
            return;
        }
        boolean foundOpt = false;
        Shares optionalShares = inventoriesConfig.getOptionalShares();
        for (Sharable sharable : shares) {
            if (sharable.isOptional()) {
                foundOpt = true;
                if (optionalShares.contains(sharable)) {
                    optionalShares.remove(sharable);
                    issuer.sendInfo(MVInvi18n.TOGGLE_NOWNOTUSINGOPTIONAL, replace("{share}").with(sharable.getNames()[0]));
                } else {
                    optionalShares.add(sharable);
                    issuer.sendInfo(MVInvi18n.TOGGLE_NOWUSINGOPTIONAL, replace("{share}").with(sharable.getNames()[0]));
                }
            }
        }
        if (foundOpt) {
            inventoriesConfig.setOptionalShares(optionalShares);
            inventoriesConfig.save();
        } else {
            issuer.sendError(MVInvi18n.TOGGLE_NOOPTIONALSHARES, replace("{share}").with(shareName));
        }
    }
}
