package com.onarandombox.multiverseinventories.permission;

import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author dumptruckman
 */
public enum MVIPerms {
    EXAMPLE (new Permission("MultiverseInventories.example", PermissionDefault.OP)),
    ;

    private Permission perm;

    MVIPerms(Permission perm) {
        this.perm = perm;
    }

    private Permission getPerm() {
        return perm;
    }

    public boolean has(CommandSender sender) {
        return sender.hasPermission(perm);
    }

    public static void load(JavaPlugin plugin) {
        PluginManager pm = plugin.getServer().getPluginManager();
        for (MVIPerms perm : MVIPerms.values()) {
            pm.addPermission(perm.getPerm());
        }
    }
}

