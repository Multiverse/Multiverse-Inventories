package com.onarandombox.multiverseinventories.permission;

import com.onarandombox.multiverseinventories.group.WorldGroup;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author dumptruckman
 */
public enum MIPerms {
    COMMAND_INFO(new Permission("multiverse.inventories.info", "Displays information about a world or group.", PermissionDefault.OP)),
    BYPASS_GROUP("mvi.bypass.group."),
    BYPASS_WORLD("mvi.bypass.world.");

    private Permission perm = null;
    private String permNode = "";

    MIPerms(Permission perm) {
        this.perm = perm;
    }

    MIPerms(String permNode) {
        this.permNode = permNode;
    }

    public Permission getPerm() {
        return this.perm;
    }

    public String getPermNode() {
        return this.permNode;
    }
    
    public String getBypassNode(String finalNode) {
        return this.getPermNode() + finalNode;
    }
    
    public boolean hasBypass(Player player, String name) {
        return player.hasPermission(this.getBypassNode(name))
                || player.hasPermission(this.getBypassNode("*"));
    }

    public boolean has(CommandSender sender) {
        return sender.hasPermission(perm);
    }

    public static void register(JavaPlugin plugin) {
        PluginManager pm = plugin.getServer().getPluginManager();
        for (MIPerms perm : MIPerms.values()) {
            if (perm.getPerm() != null) {
                pm.addPermission(perm.getPerm());
            }
        }
    }
}

