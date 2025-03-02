package org.mvplugins.multiverse.inventories.commands;

import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.core.commandtools.MVCommandIssuer;
import org.mvplugins.multiverse.core.commandtools.MVCommandManager;
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
import org.mvplugins.multiverse.inventories.share.Shares;
import org.mvplugins.multiverse.inventories.util.MVInvi18n;

import static org.mvplugins.multiverse.core.locale.message.MessageReplacement.replace;

@Service
@CommandAlias("mvinv")
final class RemoveDisabledSharesCommand extends InventoriesCommand {

    private final WorldGroupManager worldGroupManager;

    @Inject
    RemoveDisabledSharesCommand(@NotNull MVCommandManager commandManager, @NotNull WorldGroupManager worldGroupManager) {
        super(commandManager);
        this.worldGroupManager = worldGroupManager;
    }

    @Subcommand("removedisabledshares")
    @CommandPermission("multiverse.inventories.removedisabledshares")
    @CommandCompletion("@worldGroups @shares")
    @Syntax("<group> <share[,extra]>")
    @Description("Remove one or more disabled shares from a group.")
    void onRemoveSharesCommand(
            MVCommandIssuer issuer,

            @Syntax("<group>")
            @Description("Group you want to remove the shares from.")
            WorldGroup group,

            @Syntax("<share[,extra]>")
            @Description("One or more sharables to remove.")
            Shares shares
    ) {
        group.getDisabledShares().setSharing(shares, false);
        worldGroupManager.updateGroup(group);
        issuer.sendInfo(MVInvi18n.DISABLEDSHARES_NOWSHARING,
                replace("{group}").with(group.getName()),
                replace("{shares}").with(group.getDisabledShares().toStringList()));
    }
}
