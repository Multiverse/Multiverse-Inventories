package com.onarandombox.multiverseinventories.command;

import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.group.WorldGroup;
import com.onarandombox.multiverseinventories.locale.MultiverseMessage;
import com.onarandombox.multiverseinventories.permission.MIPerms;
import com.onarandombox.multiverseinventories.profile.WorldProfile;
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
        this.setName("World or Group Information");
        this.setCommandUsage("/mvi info " + ChatColor.GREEN + "{WORLD|WORLDGROUP}");
        this.setArgRange(1, 1);
        this.addKey("mvi info");
        this.addKey("mvii");
        this.addKey("mviinfo");
        this.setPermission(MIPerms.COMMAND_INFO.getPerm());
    }

    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        WorldProfile worldProfile = this.getPlugin().getProfileManager().getWorldProfile(args.get(0));
        if (worldProfile != null) {
            this.worldInfo(sender, worldProfile);
        } else {
            this.getPlugin().getMessager().bad(MultiverseMessage.ERROR_NO_GROUP, sender, args.get(0));
        }
        WorldGroup worldGroup = this.getPlugin().getGroupManager().getGroup(args.get(0));
        if (worldGroup != null) {
            this.groupInfo(sender, worldGroup);
        } else {
            this.getPlugin().getMessager().bad(MultiverseMessage.ERROR_NO_GROUP, sender, args.get(0));
        }
    }

    private void groupInfo(CommandSender sender, WorldGroup worldGroup) {
        StringBuilder worldsString = new StringBuilder();
        Set<String> worlds = worldGroup.getWorlds();
        boolean first = true;
        for (String world : worlds) {
            if (first) {
                first = false;
            } else {
                worldsString.append(",");
            }
            worldsString.append(world);
        }

        this.getPlugin().getMessager().info(
                MultiverseMessage.INFO_GROUP,
                sender, worldGroup.getName(),
                worldsString, worldGroup.getShares().toString());
    }

    private void worldInfo(CommandSender sender, WorldProfile worldProfile) {
        StringBuilder groupsString = new StringBuilder();
        List<WorldGroup> worldGroups = this.getPlugin().getGroupManager().getWorldGroups(worldProfile.getWorld());

        boolean first = true;
        for (WorldGroup worldGroup : worldGroups) {
            if (first) {
                first = false;
            } else {
                groupsString.append(",");
            }
            groupsString.append(worldGroup.getName());
        }

        this.getPlugin().getMessager().info(
                MultiverseMessage.INFO_WORLD,
                sender, worldProfile.getWorld(),
                groupsString);
    }
}
