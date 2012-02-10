package com.onarandombox.multiverseinventories.command;

import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.multiverseinventories.api.profile.WorldGroupProfile;
import com.onarandombox.multiverseinventories.util.Perm;
import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.locale.Message;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * The /mvi info Command.
 */
public class SpawnCommand extends InventoriesCommand {

    public SpawnCommand(MultiverseInventories plugin) {
        super(plugin);
        this.setName("Spawn");
        this.setCommandUsage("/mvinv spawn" + ChatColor.GOLD + " [PLAYER]");
        this.setArgRange(0, 1);
        this.addKey("mvinv spawn");
        this.addKey("mvinvspawn");
        this.addKey("mvinvs");
        this.addKey("gspawn");
        this.addKey("ispawn");
        this.setPermission(Perm.COMMAND_SPAWN.getPermission());
        this.addAdditonalPermission(Perm.COMMAND_SPAWN_OTHER.getPermission());

    }

    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        Player player = null;
        if (sender instanceof Player) {
            player = (Player) sender;
        }
        // If a persons name was passed in, you must be A. the console, or B have permissions
        if (args.size() == 1) {
            Perm perm = Perm.COMMAND_SPAWN_OTHER;
            if (player != null && !perm.has(player)) {
                this.messager.normal(Message.GENERIC_COMMAND_NO_PERMISSION, player,
                        perm.getPermission().getDescription(), perm.getPermission().getName());
                return;
            }
            Player target = Bukkit.getPlayer(args.get(0));
            if (target != null) {
                this.messager.normal(Message.TELEPORTING, target);
                spawnAccurately(target);

                if (player != null) {
                    this.messager.normal(Message.TELEPORTED_BY, target,
                            ChatColor.YELLOW + player.getName());
                } else {
                    this.messager.normal(Message.TELEPORTED_BY, target,
                            ChatColor.LIGHT_PURPLE + this.messager
                                    .getMessage(Message.GENERIC_THE_CONSOLE));
                }
            } else {
                this.messager.normal(Message.GENERIC_NOT_LOGGED_IN, sender, args.get(0));
            }
        } else {
            Perm perm = Perm.COMMAND_SPAWN;
            if (player != null && !perm.has(player)) {
                this.messager.normal(Message.GENERIC_COMMAND_NO_PERMISSION, player,
                        perm.getPermission().getDescription(), perm.getPermission().getName());
                return;
            }
            if (player != null) {
                this.messager.normal(Message.TELEPORTING, player);
                spawnAccurately(player);
            } else {
                this.messager.normal(Message.TELEPORT_CONSOLE_ERROR, sender);
            }
        }
    }

    private void spawnAccurately(Player player) {
        World world = null;
        for (WorldGroupProfile group : this.plugin.getGroupManager().getGroupsForWorld(player.getWorld().getName())) {
            if (group.getSpawnWorld() != null) {
                world = Bukkit.getWorld(group.getSpawnWorld());
                if (world != null) {
                    break;
                }
            }
        }
        if (world == null) {
            world = player.getWorld();
        }
        MultiverseWorld mvWorld = this.plugin.getCore()
                .getMVWorldManager().getMVWorld(world);
        Location spawnLocation;
        if (mvWorld != null) {
            spawnLocation = mvWorld.getSpawnLocation();
        } else {
            spawnLocation = world.getSpawnLocation();
        }
        this.plugin.getCore().getSafeTTeleporter().safelyTeleport(player, player, spawnLocation, false);
    }
}

