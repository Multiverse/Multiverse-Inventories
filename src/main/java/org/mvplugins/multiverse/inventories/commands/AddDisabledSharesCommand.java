package org.mvplugins.multiverse.inventories.commands;

import org.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.core.command.MVCommandIssuer;
import org.mvplugins.multiverse.core.command.MVCommandManager;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandAlias;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandCompletion;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandPermission;
import org.mvplugins.multiverse.external.acf.commands.annotation.Description;
import org.mvplugins.multiverse.external.acf.commands.annotation.Subcommand;
import org.mvplugins.multiverse.external.acf.commands.annotation.Syntax;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.inventories.profile.group.WorldGroup;
import org.mvplugins.multiverse.inventories.profile.group.WorldGroupManager;
import org.mvplugins.multiverse.inventories.share.Shares;
import org.mvplugins.multiverse.inventories.util.MVInvi18n;

import static org.mvplugins.multiverse.core.locale.message.MessageReplacement.replace;

@Service
@CommandAlias("mvinv")
final class AddDisabledSharesCommand extends InventoriesCommand {

    private final WorldGroupManager worldGroupManager;

    @Inject
    AddDisabledSharesCommand(@NotNull MVCommandManager commandManager, @NotNull WorldGroupManager worldGroupManager) {
        super(commandManager);
        this.worldGroupManager = worldGroupManager;
    }

    @Subcommand("adddisbledshares")
    @CommandPermission("multiverse.inventories.adddisabledshares")
    @CommandCompletion("@worldGroups @shares")
    @Syntax("<group> <share[,extra]>")
    @Description("Add one or more disabled shares to a group.")
    void onAddDisabledSharesCommand(
            MVCommandIssuer issuer,

            @Syntax("<group>")
            @Description("Group you want to add the disabled shares to.")
            WorldGroup group,

            @Syntax("<share[,extra]>")
            @Description("One or more sharables to disable for the given group.")
            Shares shares
    ) {
        group.getDisabledShares().mergeShares(shares);
        worldGroupManager.updateGroup(group);
        issuer.sendInfo(MVInvi18n.DISABLEDSHARES_NOWSHARING,
                replace("{group}").with(group.getName()),
                replace("{shares}").with(group.getDisabledShares().toStringList()));
    }
}
