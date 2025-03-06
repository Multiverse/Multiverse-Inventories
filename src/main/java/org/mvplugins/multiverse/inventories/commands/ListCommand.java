package org.mvplugins.multiverse.inventories.commands;

import org.mvplugins.multiverse.core.commandtools.MVCommandIssuer;
import org.mvplugins.multiverse.inventories.MultiverseInventories;
import org.mvplugins.multiverse.inventories.profile.group.WorldGroup;
import org.mvplugins.multiverse.core.commandtools.MVCommandManager;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandAlias;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandPermission;
import org.mvplugins.multiverse.external.acf.commands.annotation.Description;
import org.mvplugins.multiverse.external.acf.commands.annotation.Subcommand;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.inventories.profile.group.WorldGroupManager;
import org.mvplugins.multiverse.inventories.util.MVInvi18n;

import java.util.Collection;

import static org.mvplugins.multiverse.core.locale.message.MessageReplacement.replace;

@Service
@CommandAlias("mvinv")
final class ListCommand extends InventoriesCommand {

    private final WorldGroupManager worldGroupManager;

    @Inject
    ListCommand(
            @NotNull MVCommandManager commandManager,
            @NotNull WorldGroupManager worldGroupManager) {
        super(commandManager);
        this.worldGroupManager = worldGroupManager;
    }

    @CommandAlias("mvinvlist|mvinvl")
    @Subcommand("list")
    @CommandPermission("multiverse.inventories.list")
    @Description("World and Group Information")
    void onListCommand(@NotNull MVCommandIssuer issuer) {
        Collection<WorldGroup> groups = worldGroupManager.getGroups();
        String groupsString = "N/A";
        if (!groups.isEmpty()) {
            StringBuilder builder = new StringBuilder();
            for (WorldGroup group : groups) {
                if (!builder.toString().isEmpty()) {
                    builder.append(", ");
                }
                builder.append(group.getName());
            }
            groupsString = builder.toString();
        }
        issuer.sendInfo(MVInvi18n.LIST_GROUPS);
        issuer.sendInfo(MVInvi18n.LIST_GROUPS_INFO, replace("{groups}").with(groupsString));
    }
}
