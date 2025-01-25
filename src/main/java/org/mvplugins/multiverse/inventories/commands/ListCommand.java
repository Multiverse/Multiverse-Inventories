package org.mvplugins.multiverse.inventories.commands;

import org.mvplugins.multiverse.inventories.MultiverseInventories;
import org.mvplugins.multiverse.inventories.WorldGroup;
import org.mvplugins.multiverse.inventories.locale.Message;
import org.bukkit.command.CommandSender;
import org.mvplugins.multiverse.core.commandtools.MVCommandManager;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandAlias;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandPermission;
import org.mvplugins.multiverse.external.acf.commands.annotation.Description;
import org.mvplugins.multiverse.external.acf.commands.annotation.Subcommand;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Service;

import java.util.Collection;

@Service
@CommandAlias("mvinv")
class ListCommand extends InventoriesCommand {

    private final MultiverseInventories plugin;

    @Inject
    ListCommand(@NotNull MVCommandManager commandManager, @NotNull MultiverseInventories plugin) {
        super(commandManager);
        this.plugin = plugin;
    }

    @CommandAlias("mvinvlist|mvinvl")
    @Subcommand("list")
    @CommandPermission("multiverse.inventories.list")
    @Description("World and Group Information")
    void onListCommand(@NotNull CommandSender sender) {
        Collection<WorldGroup> groups = this.plugin.getGroupManager().getGroups();
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
        this.plugin.getMessager().normal(Message.LIST_GROUPS, sender, groupsString);
    }
}
