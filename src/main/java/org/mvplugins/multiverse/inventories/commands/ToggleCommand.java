package org.mvplugins.multiverse.inventories.commands;

import org.mvplugins.multiverse.core.command.LegacyAliasCommand;
import org.mvplugins.multiverse.core.command.MVCommandIssuer;
import org.mvplugins.multiverse.inventories.config.InventoriesConfig;
import org.mvplugins.multiverse.inventories.share.Sharable;
import org.mvplugins.multiverse.inventories.share.Shares;
import org.mvplugins.multiverse.core.command.MVCommandManager;
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
class ToggleCommand extends InventoriesCommand {

    private final InventoriesConfig inventoriesConfig;

    @Inject
    ToggleCommand(@NotNull InventoriesConfig inventoriesConfig) {
        this.inventoriesConfig = inventoriesConfig;
    }

    @Subcommand("toggle")
    @CommandPermission("multiverse.inventories.addshares")
    @CommandCompletion("@sharables:scope=optional")
    @Syntax("<share-name>")
    @Description("Toggles the usage of optional sharables")
    void onToggleCommand(
            @NotNull MVCommandIssuer issuer,

            @Single
            @Syntax("<share-name>")
            @Description("Share to toggle")
            @NotNull Sharable<?> sharable
    ) {
        Shares optionalShares = inventoriesConfig.getActiveOptionalShares();
        if (!sharable.isOptional()) {
            issuer.sendError(MVInvi18n.TOGGLE_NOOPTIONALSHARES, replace("{share}").with(sharable.toString()));
            return;
        }
        if (optionalShares.contains(sharable)) {
            optionalShares.remove(sharable);
            issuer.sendInfo(MVInvi18n.TOGGLE_NOWNOTUSINGOPTIONAL, replace("{share}").with(sharable.getNames()[0]));
        } else {
            optionalShares.add(sharable);
            issuer.sendInfo(MVInvi18n.TOGGLE_NOWUSINGOPTIONAL, replace("{share}").with(sharable.getNames()[0]));
        }
        inventoriesConfig.setActiveOptionalShares(optionalShares);
        inventoriesConfig.save();
    }

    @Service
    private final static class LegacyAlias extends ToggleCommand implements LegacyAliasCommand {
        @Inject
        LegacyAlias(InventoriesConfig inventoriesConfig) {
            super(inventoriesConfig);
        }

        @Override
        @CommandAlias("mvinvtoggle")
        void onToggleCommand(MVCommandIssuer issuer, Sharable<?> sharable) {
            super.onToggleCommand(issuer, sharable);
        }
    }
}
