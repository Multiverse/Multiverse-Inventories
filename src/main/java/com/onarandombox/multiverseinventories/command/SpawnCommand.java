package com.onarandombox.multiverseinventories.command;

import com.onarandombox.MultiverseCore.api.MVWorld;
import com.onarandombox.acf.BukkitCommandIssuer;
import com.onarandombox.acf.CommandIssuer;
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
    public SpawnCommand(@NotNull MultiverseInventories plugin) {
        super(plugin);
        setPerm(Perm.COMMAND_SPAWN);
    }

    @Subcommand("spawn")
    @CommandCompletion("@players")
    @Syntax("[player]")
    @Description("Teleport a player to spawn.")
    public void onSpawnCommand(CommandSender sender,

                               @Flags("resolve=issuerAware")
                               @Conditions("spawnPermission")
                               @Syntax("[player]")
                               @Description("The player to teleport to spawn.")
                               Player player
    ) {
        this.messager.normal(Message.TELEPORTING, player);
        spawnAccurately(player);
        if (!sender.equals(player)) {
            this.messager.normal(Message.TELEPORTED_BY, player, (sender instanceof ConsoleCommandSender)
                    ? ChatColor.YELLOW + sender.getName()
                    : ChatColor.LIGHT_PURPLE + this.messager.getMessage(Message.GENERIC_THE_CONSOLE));
        }
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

        MVWorld mvWorld = this.plugin.getCore().getMVWorldManager().getMVWorld(world);

        Location spawnLocation = (mvWorld == null)
                ? world.getSpawnLocation()
                : mvWorld.getSpawnLocation();

        this.plugin.getCore().getSafeTTeleporter().safelyTeleport(player, player, spawnLocation, false);
    }

    @Override
    public boolean hasPermission(CommandIssuer issuer) {
        return issuer.hasPermission(Perm.COMMAND_SPAWN.getNode()) || issuer.hasPermission(Perm.COMMAND_SPAWN_OTHER.getNode());
    }
}
