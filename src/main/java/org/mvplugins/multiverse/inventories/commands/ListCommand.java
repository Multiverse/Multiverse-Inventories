package org.mvplugins.multiverse.inventories.commands;

import org.mvplugins.multiverse.core.command.LegacyAliasCommand;
import org.mvplugins.multiverse.core.command.MVCommandIssuer;
import org.mvplugins.multiverse.inventories.MultiverseInventories;
import org.mvplugins.multiverse.inventories.profile.group.WorldGroup;
import org.mvplugins.multiverse.core.command.MVCommandManager;
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
import java.util.List;

import static org.mvplugins.multiverse.core.locale.message.MessageReplacement.replace;

@Service
class ListCommand extends InventoriesCommand {

    private final WorldGroupManager worldGroupManager;

    @Inject
    ListCommand(@NotNull WorldGroupManager worldGroupManager) {
        this.worldGroupManager = worldGroupManager;
    }

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

    @Service
    private final static class LegacyAlias extends ListCommand implements LegacyAliasCommand {
        @Inject
        LegacyAlias(WorldGroupManager worldGroupManager) {
            super(worldGroupManager);
        }

        @Override
        @CommandAlias("mvinvlist|mvinvl")
        void onListCommand(MVCommandIssuer issuer) {
            super.onListCommand(issuer);
        }
    }
}
