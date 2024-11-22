package com.onarandombox.multiverseinventories.commands;

import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.WorldGroup;
import com.onarandombox.multiverseinventories.locale.Message;
import com.onarandombox.multiverseinventories.profile.container.ProfileContainer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.mvplugins.multiverse.core.commandtools.MVCommandManager;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandAlias;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandCompletion;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandPermission;
import org.mvplugins.multiverse.external.acf.commands.annotation.Description;
import org.mvplugins.multiverse.external.acf.commands.annotation.Optional;
import org.mvplugins.multiverse.external.acf.commands.annotation.Single;
import org.mvplugins.multiverse.external.acf.commands.annotation.Subcommand;
import org.mvplugins.multiverse.external.acf.commands.annotation.Syntax;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.external.jvnet.hk2.annotations.Service;

import java.util.List;
import java.util.Set;

@Service
@CommandAlias("mvinv")
class InfoCommand extends InventoriesCommand {

    private final MultiverseInventories plugin;

    @Inject
    InfoCommand(@NotNull MVCommandManager commandManager, @NotNull MultiverseInventories plugin) {
        super(commandManager);
        this.plugin = plugin;
    }

    @CommandAlias("mvinvinfo|mvinvi")
    @Subcommand("info")
    @CommandPermission("multiverse.inventories.info")
    @CommandCompletion("@mvworlds")
    @Syntax("<world|group>")
    @Description("World and Group Information")
    void onInfoCommand(
            @NotNull CommandSender sender,

            @Optional
            @Single
            @Syntax("<world|group>")
            @Description("World or Group")
            @NotNull String name
    ) {
        if (name == null) {
            if (!(sender instanceof Player)) {
                this.plugin.getMessager().normal(Message.INFO_ZERO_ARG, sender);
                return;
            }
            name = ((Player) sender).getWorld().getName();
        }

        ProfileContainer worldProfileContainer = this.plugin.getWorldProfileContainerStore().getContainer(name);
        plugin.getMessager().normal(Message.INFO_WORLD, sender, name);
        if (worldProfileContainer != null && Bukkit.getWorld(worldProfileContainer.getContainerName()) != null) {
            worldInfo(sender, worldProfileContainer);
        } else {
            plugin.getMessager().normal(Message.ERROR_NO_WORLD_PROFILE, sender, name);
        }
        WorldGroup worldGroup = this.plugin.getGroupManager().getGroup(name);
        this.plugin.getMessager().normal(Message.INFO_GROUP, sender, name);
        if (worldGroup != null) {
            this.groupInfo(sender, worldGroup);
        } else {
            this.plugin.getMessager().normal(Message.ERROR_NO_GROUP, sender, name);
        }
    }

    private void groupInfo(CommandSender sender, WorldGroup worldGroup) {
        StringBuilder worldsString = new StringBuilder();
        Set<String> worlds = worldGroup.getWorlds();
        if (worlds.isEmpty()) {
            worldsString.append("N/A");
        } else {
            for (String world : worlds) {
                if (!worldsString.toString().isEmpty()) {
                    worldsString.append(", ");
                }
                worldsString.append(world);
            }
        }
        this.plugin.getMessager().normal(Message.INFO_GROUPS_INFO,
                sender, worldsString, worldGroup.getShares().toString());
    }

    private void worldInfo(CommandSender sender, ProfileContainer worldProfileContainer) {
        StringBuilder groupsString = new StringBuilder();
        List<WorldGroup> worldGroups = this.plugin.getGroupManager()
                .getGroupsForWorld(worldProfileContainer.getContainerName());

        if (worldGroups.isEmpty()) {
            groupsString.append("N/A");
        } else {
            for (WorldGroup worldGroup : worldGroups) {
                if (!groupsString.toString().isEmpty()) {
                    groupsString.append(", ");
                }
                groupsString.append(worldGroup.getName());
            }
        }

        this.plugin.getMessager().normal(Message.INFO_WORLD_INFO,
                sender, groupsString.toString());
    }
}
