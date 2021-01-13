package com.onarandombox.multiverseinventories.commands;

import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.acf.annotation.CommandAlias;
import com.onarandombox.acf.annotation.CommandCompletion;
import com.onarandombox.acf.annotation.Conditions;
import com.onarandombox.acf.annotation.Description;
import com.onarandombox.acf.annotation.Flags;
import com.onarandombox.acf.annotation.Subcommand;
import com.onarandombox.acf.annotation.Syntax;
import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.WorldGroup;
import com.onarandombox.multiverseinventories.locale.Message;
import com.onarandombox.multiverseinventories.util.Perm;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@CommandAlias("mvinv")
public class SpawnCommand extends InventoriesCommand {

    public SpawnCommand(MultiverseInventories plugin) {
        super(plugin);
        this.addPermission(Perm.COMMAND_SPAWN);
        this.addPermission(Perm.COMMAND_SPAWN_OTHER);
    }

    @Subcommand("spawn")
    @Syntax("[player]")
    @CommandCompletion("@players")
    @Description("Teleport a player to spawn.")
    public void onSpawnCommand(@NotNull CommandSender sender,
                               @NotNull
                               @Flags("other,defaultself")
                               @Conditions("selfOtherPerm:multiverse.inventories.spawn") Player player) {

        this.messager.normal(Message.TELEPORTING, player);
        spawnAccurately(player);

        if (sender.equals(player)) {
            return;
        }

        this.messager.normal(Message.TELEPORTED_BY, player, (sender instanceof ConsoleCommandSender)
                ? ChatColor.YELLOW + sender.getName()
                : ChatColor.LIGHT_PURPLE + this.messager.getMessage(Message.GENERIC_THE_CONSOLE));
    }

    private void spawnAccurately(Player player) {
        World world = null;
        for (WorldGroup group : this.plugin.getGroupManager().getGroupsForWorld(player.getWorld().getName())) {
            String spawnWorld = group.getSpawnWorld();
            if (spawnWorld == null) {
                continue;
            }
            world = Bukkit.getWorld(spawnWorld);
            if (world != null) {
                break;
            }
        }

        if (world == null) {
            world = player.getWorld();
        }

        MultiverseWorld mvWorld = this.plugin.getCore().getMVWorldManager().getMVWorld(world);

        Location spawnLocation = (mvWorld == null)
                ? world.getSpawnLocation()
                : mvWorld.getSpawnLocation();

        this.plugin.getCore().getSafeTTeleporter().safelyTeleport(player, player, spawnLocation, false);
    }
}
