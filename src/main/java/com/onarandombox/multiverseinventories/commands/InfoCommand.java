package com.onarandombox.multiverseinventories.commands;

import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.MultiverseCore.commandTools.display.ContentCreator;
import com.onarandombox.MultiverseCore.commandTools.display.inline.ListDisplay;
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
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@CommandAlias("mvinv")
public class InfoCommand extends InventoriesCommand {

    public InfoCommand(MultiverseInventories plugin) {
        super(plugin);
        this.addPermission(Perm.COMMAND_INFO);
    }

    @Subcommand("info")
    @Syntax("[world|group]")
    @CommandCompletion("@MVWorlds|@worldGroups @empty")
    @Description("World and Group Information.")
    public void onInfoCommand(@NotNull CommandSender sender,
                              @Nullable @Optional @Flags("other,defaultself,fallbackself") MultiverseWorld world,
                              @Nullable @Optional WorldGroup group) {

        if (world == null && group == null) {
            this.messager.normal(Message.INFO_ZERO_ARG, sender);
            return;
        }

        String name = (group == null) ? world.getName() : group.getName();

        ProfileContainer worldProfileContainer = this.plugin.getWorldProfileContainerStore().getContainer(name);
        new ListDisplay().withSender(sender)
                .withHeader(this.messager.getMessage(Message.INFO_WORLD_HEADER, name))
                .withCreator(createWorldContent(worldProfileContainer))
                .withPrefix(this.messager.getMessage(Message.INFO_WORLD_GROUPS, name))
                .withEmptyMessage(this.messager.getMessage(Message.ERROR_NO_WORLD_PROFILE, name))
                .build()
                .run();

        messager.normal(Message.GENERIC_EMPTY, sender);

        WorldGroup worldGroup = this.plugin.getGroupManager().getGroup(name);
        new ListDisplay().withSender(sender)
                .withHeader(this.messager.getMessage(Message.INFO_GROUP_HEADER, name))
                .withCreator(createGroupContent(worldGroup))
                .withPrefix(this.messager.getMessage(Message.INFO_GROUP_WORLDS, name))
                .withEmptyMessage(this.messager.getMessage(Message.ERROR_NO_GROUP, name))
                .build()
                .run();

        if (worldGroup != null) {
            this.messager.normal(Message.INFO_GROUP_SHARES, sender, worldGroup.getShares().toString());
        }
    }

    private ContentCreator<List<String>> createWorldContent(@Nullable ProfileContainer worldProfileContainer) {
        return () -> {
            if (worldProfileContainer == null) {
                return Collections.emptyList();
            }
            return this.plugin.getGroupManager().getGroupsForWorld(worldProfileContainer.getContainerName()).stream()
                    .map(WorldGroup::getName)
                    .collect(Collectors.toList());
        };
    }

    private ContentCreator<List<String>> createGroupContent(@Nullable WorldGroup worldGroup) {
        return () -> (worldGroup == null)
                ? Collections.emptyList()
                : new ArrayList<>(worldGroup.getWorlds());
    }
}
