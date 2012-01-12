package com.onarandombox.multiverseinventories.permission;

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
    /**
     * Permission for /mvi info.
     */
    COMMAND_INFO(new Permission("multiverse.inventories.info", "Displays information about a world or group.", PermissionDefault.OP)),
    /**
     * Permission prefix for bypassing groups.
     */
    BYPASS_GROUP("mvinv.bypass.group."),
    /**
     * Permission prefix for bypassing worlds.
     */
    BYPASS_WORLD("mvinv.bypass.world.");

    private Permission perm = null;
    private String permNode = "";

    MIPerms(Permission perm) {
        this.perm = perm;
    }

    MIPerms(String permNode) {
        this.permNode = permNode;
    }

    /**
     * @return the Permission.
     */
    public Permission getPerm() {
        return this.perm;
    }

    /**
     * @return the Permission node string.
     */
    public String getPermNode() {
        return this.permNode;
    }

    /**
     * @param finalNode String to add to the bypass prefix.
     * @return The full permission node for bypass.
     */
    public String getBypassNode(String finalNode) {
        return this.getPermNode() + finalNode;
    }

    /**
     * Checks if a player has permission to bypass something which requires a name of an object to be bypassed.
     * A World name for example.
     *
     * @param player Player to check permission for.
     * @param name Name of object to bypass.
     * @return True if player is allowed to bypass.
     */
    public boolean hasBypass(Player player, String name) {
        return player.hasPermission(this.getBypassNode(name))
                || player.hasPermission(this.getBypassNode("*"));
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

    /**
     * Registers all Permission to the plugin.
     *
     * @param plugin Plugin to register permissions to.
     */
    public static void register(JavaPlugin plugin) {
        PluginManager pm = plugin.getServer().getPluginManager();
        for (MIPerms perm : MIPerms.values()) {
            if (perm.getPerm() != null) {
                pm.addPermission(perm.getPerm());
            }
        }
    }
}

