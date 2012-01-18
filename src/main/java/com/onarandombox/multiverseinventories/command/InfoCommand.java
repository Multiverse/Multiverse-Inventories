package com.onarandombox.multiverseinventories.command;

import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.group.WorldGroup;
import com.onarandombox.multiverseinventories.locale.MultiverseMessage;
import com.onarandombox.multiverseinventories.permission.MVIPerms;
import com.onarandombox.multiverseinventories.profile.WorldProfile;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Set;

/**
 * The /mvi info Command.
 */
public class InfoCommand extends InventoriesCommand {

    public InfoCommand(MultiverseInventories plugin) {
        super(plugin);
        this.setName("World and Group Information");
        this.setCommandUsage("/mvinv info " + ChatColor.GREEN + "{WORLD|GROUP}");
        this.setArgRange(1, 1);
        this.addKey("mvinv info");
        this.addKey("mvinvi");
        this.addKey("mvinvinfo");
        this.setPermission(MVIPerms.COMMAND_INFO.getPerm());
    }

    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        WorldProfile worldProfile = this.getPlugin().getProfileManager().getWorldProfile(args.get(0));
        this.getPlugin().getMessager().normal(MultiverseMessage.INFO_WORLD, sender, args.get(0));
        if (worldProfile != null && Bukkit.getWorld(worldProfile.getWorld()) != null) {
            this.worldInfo(sender, worldProfile);
        } else {
            this.getPlugin().getMessager().normal(MultiverseMessage.ERROR_NO_WORLD_PROFILE, sender, args.get(0));
        }
        WorldGroup worldGroup = this.getPlugin().getGroupManager().getGroup(args.get(0));
        this.getPlugin().getMessager().normal(MultiverseMessage.INFO_GROUP, sender, args.get(0));
        if (worldGroup != null) {
            this.groupInfo(sender, worldGroup);
        } else {
            this.getPlugin().getMessager().normal(MultiverseMessage.ERROR_NO_GROUP, sender, args.get(0));
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

        this.getPlugin().getMessager().normal(MultiverseMessage.INFO_GROUP_INFO,
                sender, worldsString, worldGroup.getShares().toString());
    }

    private void worldInfo(CommandSender sender, WorldProfile worldProfile) {
        StringBuilder groupsString = new StringBuilder();
        List<WorldGroup> worldGroups = this.getPlugin().getGroupManager().getWorldGroups(worldProfile.getWorld());

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

        this.getPlugin().getMessager().normal(MultiverseMessage.INFO_WORLD_INFO,
                sender, groupsString.toString());
    }
}
