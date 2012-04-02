package com.onarandombox.multiverseinventories.util;

import com.onarandombox.multiverseinventories.api.Inventories;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;

/**
 * @author dumptruckman
 */
public enum Perm {
    /**
     * Permission for /mvinv info.
     */
    COMMAND_INFO(new Permission("multiverse.inventories.info", "Displays information about a world or group.", PermissionDefault.OP)),
    /**
     * Permission for /mvinv list.
     */
    COMMAND_LIST(new Permission("multiverse.inventories.list", "Displays a list of groups.", PermissionDefault.OP)),
    /**
     * Permission for /mvinv reload.
     */
    COMMAND_RELOAD(new Permission("multiverse.inventories.reload", "Reloads config file.", PermissionDefault.OP)),
    /**
     * Permission for /mvinv import.
     */
    COMMAND_IMPORT(new Permission("multiverse.inventories.import", "Imports data from MultiInv/WorldInventories", PermissionDefault.OP)),
    /**
     * Permission for /mvinv group.
     */
    COMMAND_GROUP(new Permission("multiverse.inventories.group", "Begins a conversation about groups.",
            PermissionDefault.OP)),
    /**
     * Permission for /mvinv addworld.
     */
    COMMAND_ADDWORLD(new Permission("multiverse.inventories.addworld", "Adds a world to a world group",
            PermissionDefault.OP)),
    /**
     * Permission for /mvinv remvoveworld.
     */
    COMMAND_RMWORLD(new Permission("multiverse.inventories.removeworld", "Removes a world from a world group",
            PermissionDefault.OP)),
    /**
     * Permission for /mvinv addshare.
     */
    COMMAND_ADDSHARES(new Permission("multiverse.inventories.addshares", "Adds share(s) to a world group",
            PermissionDefault.OP)),
    /**
     * Permission for /mvinv rmshare.
     */
    COMMAND_RMSHARES(new Permission("multiverse.inventories.removeshares", "Removes share(s) from a world group",
            PermissionDefault.OP)),
    /**
     * Permissions for /mvinv spawn.
     */
    COMMAND_SPAWN(new Permission("multiverse.inventories.spawn.self", "teleport yourself to group spawn",
            PermissionDefault.OP)),
    /**
     * Permissions for /mvinv spawn.
     */
    COMMAND_SPAWN_OTHER(new Permission("multiverse.inventories.spawn.other", "teleport other to group spawn",
            PermissionDefault.OP)),
    /**
     * Permission for debug command.
     */
    COMMAND_DEBUG(new Permission("multiverse.inventories.debug", "Spams the console a bunch.", PermissionDefault.OP)),
    /**
     * Permission for bypassing all groups.
     */
    BYPASS_GROUP_ALL(new Permission("mvinv.bypass.group.*", "", PermissionDefault.FALSE)),
    /**
     * Permission prefix for bypassing groups.
     */
    BYPASS_GROUP("mvinv.bypass.group.") {
        private String getBypassMessage(Player player, String name) {
            return "Player: " + player.getName() + " has bypass perms for group: " + name;
        }
    },
    /**
     * Permission for bypassing all worlds.
     */
    BYPASS_WORLD_ALL(new Permission("mvinv.bypass.world.*", "", PermissionDefault.FALSE)),
    /**
     * Permission prefix for bypassing worlds.
     */
    BYPASS_WORLD("mvinv.bypass.world.") {
        private String getBypassMessage(Player player, String name) {
            return "Player: " + player.getName() + " has bypass perms for world: " + name;
        }
    },
    /**
     * Permissions for bypassing all world/groups inventory handling.
     */
    BYPASS_ALL(new Permission("mvinv.bypass.*", "Allows bypassing all of your groups/worlds and constantly use "
            + "the same inventory", PermissionDefault.FALSE));

    private Permission perm = null;
    private String permNode = "";

    Perm(Permission perm) {
        this.perm = perm;
    }

    Perm(String permNode) {
        this.permNode = permNode;
    }

    /**
     * @return the Permission.
     */
    public Permission getPermission() {
        return this.perm;
    }

    /**
     * @return the Permission node string.
     */
    public String getNode() {
        return this.permNode;
    }

    /**
     * @param finalNode String to add to the bypass prefix.
     * @return The full permission node for bypass.
     */
    public Permission getBypassPermission(String finalNode) {
        String bypassNode = this.getNode() + finalNode;
        Logging.finer("Checking node " + bypassNode + "...");

        Permission permission = Bukkit.getPluginManager().getPermission(bypassNode);
        if (permission == null) {
            permission = new Permission(bypassNode, PermissionDefault.FALSE);
            switch (this) {
                case BYPASS_GROUP:
                    permission.addParent(BYPASS_GROUP_ALL.getPermission(), true);
                    break;
                case BYPASS_WORLD:
                    permission.addParent(BYPASS_WORLD_ALL.getPermission(), true);
                    break;
                default:
            }
            Bukkit.getPluginManager().addPermission(permission);
        }
        return permission;
    }

    /**
     * Checks if a player has permission to bypass something which requires a name of an object to be bypassed.
     * A World name for example.
     *
     * @param player Player to check permission for.
     * @param name   Name of object to bypass.
     * @return True if player is allowed to bypass.
     */
    public boolean hasBypass(Player player, String name) {
        if (inventories != null && !inventories.getMVIConfig().isUsingBypass()) {
            return false;
        }
        Permission bypassPerm = this.getBypassPermission(name);
        boolean hasBypass = player.hasPermission(bypassPerm);
        if (hasBypass) {
            Logging.fine("Player: " + player.getName() + " in World: " + player.getWorld().getName()
                    + " has permission: " + bypassPerm.getName() + "(Default: "
                    + bypassPerm.getDefault().toString() + ")!");
        }
        return hasBypass;
    }

    /**
     * Checks if the sender has the node in question.
     *
     * @param sender CommandSender to check permission for.
     * @return True if sender has the permission.
     */
    public boolean has(CommandSender sender) {
        return sender.hasPermission(perm);
    }

    private static Inventories inventories = null;

    /**
     * Registers all Permission to the plugin.
     *
     * @param plugin Plugin to register permissions to.
     */
    public static void register(Inventories plugin) {
        inventories = plugin;
        BYPASS_WORLD_ALL.getPermission().addParent(BYPASS_ALL.getPermission(), true);
        BYPASS_GROUP_ALL.getPermission().addParent(BYPASS_ALL.getPermission(), true);
        PluginManager pm = plugin.getServer().getPluginManager();
        for (Perm perm : Perm.values()) {
            if (perm.getPermission() != null) {
                pm.addPermission(perm.getPermission());
            }
        }
    }
}
