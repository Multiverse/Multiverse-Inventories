package com.onarandombox.multiverseinventories.commands;

import com.onarandombox.MultiverseCore.commandTools.display.ContentCreator;
import com.onarandombox.MultiverseCore.commandTools.display.ContentFilter;
import com.onarandombox.MultiverseCore.commandTools.display.inline.ListDisplay;
import com.onarandombox.acf.annotation.CommandAlias;
import com.onarandombox.acf.annotation.Description;
import com.onarandombox.acf.annotation.Subcommand;
import com.onarandombox.acf.annotation.Syntax;
import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.WorldGroup;
import com.onarandombox.multiverseinventories.locale.Message;
import com.onarandombox.multiverseinventories.util.Perm;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@CommandAlias("mvinv")
public class ListCommand extends InventoriesCommand {

    public ListCommand(MultiverseInventories plugin) {
        super(plugin);
        this.addPermission(Perm.COMMAND_LIST);
    }

    @Subcommand("list")
    @Syntax("[filter]")
    @Description("World and Group Information.")
    public void onListCommand(@NotNull CommandSender sender,
                              @NotNull ContentFilter filter) {

        String prefix = this.messager.getMessage(Message.LIST_GROUPS_PREFIX);

        new ListDisplay().withSender(sender)
                .withHeader(this.messager.getMessage(Message.LIST_GROUPS_HEADER))
                .withCreator(generateGroupContent())
                .withPrefix(prefix)
                .withFilter(filter)
                .withEmptyMessage(String.format("%sN/A", prefix))
                .build()
                .runTaskAsynchronously(this.plugin);
    }

    private ContentCreator<List<String>> generateGroupContent() {
        return () -> this.plugin.getGroupManager().getGroups().stream()
                .map(WorldGroup::getName)
                .collect(Collectors.toList());
    }
}
