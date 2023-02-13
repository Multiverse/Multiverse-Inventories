package com.onarandombox.multiverseinventories.command;

import java.util.stream.Collectors;

import com.onarandombox.MultiverseCore.display.ContentDisplay;
import com.onarandombox.MultiverseCore.display.filters.ContentFilter;
import com.onarandombox.MultiverseCore.display.handlers.InlineSendHandler;
import com.onarandombox.MultiverseCore.display.parsers.ContentProvider;
import com.onarandombox.acf.BukkitCommandIssuer;
import com.onarandombox.acf.annotation.CommandAlias;
import com.onarandombox.acf.annotation.Description;
import com.onarandombox.acf.annotation.Subcommand;
import com.onarandombox.acf.annotation.Syntax;
import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.WorldGroup;
import com.onarandombox.multiverseinventories.locale.Message;
import com.onarandombox.multiverseinventories.util.Perm;
import org.jetbrains.annotations.NotNull;

@CommandAlias("mvinv")
public class ListCommand extends InventoriesCommand {
    public ListCommand(@NotNull MultiverseInventories plugin) {
        super(plugin);
        setPerm(Perm.COMMAND_LIST);
    }

    @Subcommand("list")
    @Syntax("[filter]")
    @Description("World and Group Information.")
    public void onListCommand(BukkitCommandIssuer issuer,

                              @Syntax("[filter]")
                              @Description("Filter the list by world or group.")
                              ContentFilter filter
    ) {
        ContentDisplay.create()
                .addContent(groupContent())
                .withSendHandler(InlineSendHandler.create()
                        .withHeader(this.messager.getMessage(Message.LIST_GROUPS_HEADER))
                        .withFilter(filter)
                        .noContentMessage(this.messager.getMessage(Message.ERROR_NO_GROUP, filter)))
                .send(issuer);
    }

    private ContentProvider groupContent() {
        return (issuer) -> this.plugin.getGroupManager().getGroups().stream()
                .map(WorldGroup::getName)
                .collect(Collectors.toList());
    }
}
