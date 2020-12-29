package com.onarandombox.multiverseinventories.commands;

import com.onarandombox.MultiverseCore.api.MultiverseWorld;
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
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

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
                              @Nullable @Optional @Flags("other,defaultself,fallbackself")MultiverseWorld world,
                              @Nullable @Optional WorldGroup group) {

        if (world == null && group == null) {
            this.messager.normal(Message.INFO_ZERO_ARG, sender);
            return;
        }

        String name = (group == null) ? world.getName() : group.getName();

        ProfileContainer worldProfileContainer = this.plugin.getWorldProfileContainerStore().getContainer(name);

        messager.normal(Message.INFO_WORLD, sender, name);
        if (worldProfileContainer != null && Bukkit.getWorld(worldProfileContainer.getContainerName()) != null) {
            worldInfo(sender, worldProfileContainer);
        }
        else {
            messager.normal(Message.ERROR_NO_WORLD_PROFILE, sender, name);
        }

        messager.normal(Message.GENERIC_EMPTY, sender);

        WorldGroup worldGroup = this.plugin.getGroupManager().getGroup(name);
        this.messager.normal(Message.INFO_GROUP, sender, name);
        if (worldGroup != null) {
            groupInfo(sender, worldGroup);
        }
        else {
            this.messager.normal(Message.ERROR_NO_GROUP, sender, name);
        }
    }

    private void groupInfo(CommandSender sender, WorldGroup worldGroup) {
        StringBuilder worldsString = new StringBuilder();
        Set<String> worlds = worldGroup.getWorlds();
        if (worlds.isEmpty()) {
            worldsString.append("N/A");
        }
        else {
            for (String world : worlds) {
                if (!worldsString.toString().isEmpty()) {
                    worldsString.append(", ");
                }
                worldsString.append(world);
            }
        }
        this.messager.normal(Message.INFO_GROUPS_INFO,
                sender, worldsString, worldGroup.getShares().toString());
    }

    private void worldInfo(CommandSender sender, ProfileContainer worldProfileContainer) {
        StringBuilder groupsString = new StringBuilder();
        List<WorldGroup> worldGroups = this.plugin.getGroupManager()
                .getGroupsForWorld(worldProfileContainer.getContainerName());

        if (worldGroups.isEmpty()) {
            groupsString.append("N/A");
        }
        else {
            for (WorldGroup worldGroup : worldGroups) {
                if (!groupsString.toString().isEmpty()) {
                    groupsString.append(", ");
                }
                groupsString.append(worldGroup.getName());
            }
        }

        this.messager.normal(Message.INFO_WORLD_INFO,
                sender, groupsString.toString());
    }
}
