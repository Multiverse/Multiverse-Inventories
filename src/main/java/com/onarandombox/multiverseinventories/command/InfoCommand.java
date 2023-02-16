package com.onarandombox.multiverseinventories.command;

import java.util.Collections;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.onarandombox.MultiverseCore.api.MVWorld;
import com.onarandombox.MultiverseCore.display.ContentDisplay;
import com.onarandombox.MultiverseCore.display.handlers.InlineSendHandler;
import com.onarandombox.MultiverseCore.display.handlers.PagedSendHandler;
import com.onarandombox.MultiverseCore.display.parsers.ContentProvider;
import com.onarandombox.acf.BukkitCommandIssuer;
import com.onarandombox.acf.annotation.CommandAlias;
import com.onarandombox.acf.annotation.CommandCompletion;
import com.onarandombox.acf.annotation.Description;
import com.onarandombox.acf.annotation.Flags;
import com.onarandombox.acf.annotation.Optional;
import com.onarandombox.acf.annotation.Subcommand;
import com.onarandombox.acf.annotation.Syntax;
import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.WorldGroup;
import com.onarandombox.multiverseinventories.locale.Message;
import com.onarandombox.multiverseinventories.profile.container.ProfileContainer;
import com.onarandombox.multiverseinventories.util.Perm;
import org.jetbrains.annotations.NotNull;

@CommandAlias("mvinv")
public class InfoCommand extends InventoriesCommand {
    public InfoCommand(@NotNull MultiverseInventories plugin) {
        super(plugin);
        setPerm(Perm.COMMAND_INFO);
    }

    @Subcommand("info")
    @CommandCompletion("@mvworlds|@worldGroups")
    @Syntax("[world|group]")
    @Description("World and Group Information.")
    public void onInfoCommand(BukkitCommandIssuer issuer,

                              @Optional
                              @Flags("resolve=issuerOnly")
                              MVWorld world,

                              @Optional
                              @Syntax("[world|group]")
                              @Description("The world or group to get information about.")
                              String name
    ) {
        if (name == null) {
            if (world == null) {
                this.messager.normal(Message.INFO_ZERO_ARG, issuer.getIssuer());
                return;
            }
            name = world.getName();
        }

        WorldGroup worldGroup = this.plugin.getGroupManager().getGroup(name);
        ProfileContainer worldProfileContainer = this.plugin.getWorldProfileContainerStore().getContainer(name);

        if (worldGroup == null && worldProfileContainer == null) {
            this.messager.normal(Message.INFO_ZERO_ARG, issuer.getIssuer());
            return;
        }

        ContentDisplay.create()
                .addContent(groupContent(worldGroup))
                .withSendHandler(PagedSendHandler.create()
                        .withHeader(this.messager.getMessage(Message.INFO_GROUP_HEADER, name))
                        .noContentMessage(this.messager.getMessage(Message.ERROR_NO_GROUP, name))
                        .doPagination(false))
                .send(issuer);

        issuer.sendMessage("");

        ContentDisplay.create()
                .addContent(worldContent(worldProfileContainer))
                .withSendHandler(InlineSendHandler.create()
                        .withHeader(this.messager.getMessage(Message.INFO_WORLD_HEADER, name))
                        .withPrefix(this.messager.getMessage(Message.INFO_WORLD_GROUPS))
                        .noContentMessage(this.messager.getMessage(Message.ERROR_NO_WORLD, name)))
                .send(issuer);
    }

    private ContentProvider groupContent(WorldGroup worldGroup) {
        return (issuer) -> {
            if (worldGroup == null) {
                return Collections.emptyList();
            }
            return Lists.newArrayList(
                    this.messager.getMessage(Message.INFO_GROUP_WORLDS, String.join(", ", worldGroup.getWorlds())),
                    this.messager.getMessage(Message.INFO_GROUP_SHARES, String.join(", ", worldGroup.getShares().toStringList()))
            );
        };
    }

    private ContentProvider worldContent(ProfileContainer worldProfileContainer) {
        return (issuer) -> {
            if (worldProfileContainer == null) {
                return Collections.emptyList();
            }
            return this.plugin.getGroupManager().getGroupsForWorld(worldProfileContainer.getContainerName()).stream()
                    .map(WorldGroup::getName)
                    .collect(Collectors.toList());
        };
    }
}
