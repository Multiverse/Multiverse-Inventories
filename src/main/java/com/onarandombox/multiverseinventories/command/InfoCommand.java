package com.onarandombox.multiverseinventories.command;

import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.api.profile.WorldGroupProfile;
import com.onarandombox.multiverseinventories.api.profile.WorldProfile;
import com.onarandombox.multiverseinventories.locale.Message;
import com.onarandombox.multiverseinventories.util.Perm;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;

/**
 * The /mvi info Command.
 */
public class InfoCommand extends InventoriesCommand {

    public InfoCommand(MultiverseInventories plugin) {
        super(plugin);
        this.setName("World and Group Information");
        this.setCommandUsage("/mvinv info " + ChatColor.GREEN + "[WORLD|GROUP]");
        this.setArgRange(0, 1);
        this.addKey("mvinv info");
        this.addKey("mvinvi");
        this.addKey("mvinvinfo");
        this.setPermission(Perm.COMMAND_INFO.getPermission());
    }

    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        String name;
        if (args.isEmpty()) {
            if (!(sender instanceof Player)) {
                this.messager.normal(Message.INFO_ZERO_ARG, sender);
                return;
            }
            name = ((Player) sender).getWorld().getName();
        } else {
            name = args.get(0);
        }

        WorldProfile worldProfile = this.plugin.getWorldManager().getWorldProfile(name);
        this.messager.normal(Message.INFO_WORLD, sender, name);
        if (worldProfile != null && Bukkit.getWorld(worldProfile.getWorld()) != null) {
            this.worldInfo(sender, worldProfile);
        } else {
            this.messager.normal(Message.ERROR_NO_WORLD_PROFILE, sender, name);
        }
        WorldGroupProfile worldGroup = this.plugin.getGroupManager().getGroup(name);
        this.messager.normal(Message.INFO_GROUP, sender, name);
        if (worldGroup != null) {
            this.groupInfo(sender, worldGroup);
        } else {
            this.messager.normal(Message.ERROR_NO_GROUP, sender, name);
        }
    }

    private void groupInfo(CommandSender sender, WorldGroupProfile worldGroup) {
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
        this.messager.normal(Message.INFO_GROUP_INFO,
                sender, worldsString, worldGroup.getShares().toString(), worldGroup.getNegativeShares().toString());
    }

    private void worldInfo(CommandSender sender, WorldProfile worldProfile) {
        StringBuilder groupsString = new StringBuilder();
        List<WorldGroupProfile> worldGroups = this.plugin.getGroupManager().getGroupsForWorld(worldProfile.getWorld());

        if (worldGroups.isEmpty()) {
            groupsString.append("N/A");
        } else {
            for (WorldGroupProfile worldGroup : worldGroups) {
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

