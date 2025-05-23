package org.mvplugins.multiverse.inventories.commands;

import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.core.command.MVCommandIssuer;
import org.mvplugins.multiverse.core.command.MVCommandManager;
import org.mvplugins.multiverse.external.acf.commands.BukkitCommandIssuer;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandAlias;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandCompletion;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandPermission;
import org.mvplugins.multiverse.external.acf.commands.annotation.Description;
import org.mvplugins.multiverse.external.acf.commands.annotation.Subcommand;
import org.mvplugins.multiverse.external.acf.commands.annotation.Syntax;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.inventories.profile.group.WorldGroup;
import org.mvplugins.multiverse.inventories.profile.group.WorldGroupManager;
import org.mvplugins.multiverse.inventories.share.Sharables;
import org.mvplugins.multiverse.inventories.share.Shares;
import org.mvplugins.multiverse.inventories.util.MVInvi18n;

import static org.mvplugins.multiverse.core.locale.message.MessageReplacement.replace;

@Service
final class AddSharesCommand extends InventoriesCommand {

    private final WorldGroupManager worldGroupManager;

    @Inject
    AddSharesCommand(@NotNull WorldGroupManager worldGroupManager) {
        this.worldGroupManager = worldGroupManager;
    }

    @Subcommand("add-shares")
    @CommandPermission("multiverse.inventories.addshares")
    @CommandCompletion("@worldGroups @shares")
    @Syntax("<group> <share[,extra]>")
    @Description("Add one or more shares to a group.")
    void onAddSharesCommand(
            MVCommandIssuer issuer,

            @Syntax("<group>")
            @Description("Group you want to add the shares to.")
            WorldGroup group,

            @Syntax("<share[,extra]>")
            @Description("One or more sharables to add.")
            Shares shares
    ) {
        group.getShares().mergeShares(shares);
        worldGroupManager.updateGroup(group);
        var negativeshares = Sharables.allOf();
        negativeshares.setSharing(group.getShares(), false);
        issuer.sendInfo(MVInvi18n.SHARES_NOWSHARING,
                replace("{group}").with(group.getName()),
                replace("{shares}").with(group.getShares().toStringList()),
                replace("{negativeshares}").with(negativeshares.toStringList()));
    }
}
