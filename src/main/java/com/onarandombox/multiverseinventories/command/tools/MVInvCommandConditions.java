package com.onarandombox.multiverseinventories.command.tools;

import com.onarandombox.acf.BukkitCommandExecutionContext;
import com.onarandombox.acf.BukkitCommandIssuer;
import com.onarandombox.acf.BukkitConditionContext;
import com.onarandombox.acf.CommandConditions;
import com.onarandombox.acf.ConditionContext;
import com.onarandombox.acf.ConditionFailedException;
import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.locale.Message;
import com.onarandombox.multiverseinventories.locale.Messager;
import com.onarandombox.multiverseinventories.share.Sharable;
import com.onarandombox.multiverseinventories.util.Perm;
import org.bukkit.entity.Player;

public class MVInvCommandConditions {
    public static void init(MultiverseInventories plugin) {
        new MVInvCommandConditions(plugin);
    }

    private final MultiverseInventories plugin;
    private final Messager messager;

    private MVInvCommandConditions(MultiverseInventories plugin) {
        this.plugin = plugin;
        this.messager = plugin.getMessager();
        CommandConditions<BukkitCommandIssuer, BukkitCommandExecutionContext, BukkitConditionContext> commandConditions = plugin.getCore().getMVCommandManager().getCommandConditions();
        commandConditions.addCondition(Sharable.class, "optionalSharable", this::checkOptionalSharable);
        commandConditions.addCondition(String.class, "newWorldGroupName", this::checkNewWorldGroupName);
        commandConditions.addCondition(Player.class, "spawnPermission", this::checkSpawnPermission);
    }

    private void checkOptionalSharable(ConditionContext<BukkitCommandIssuer> context,
                                       BukkitCommandExecutionContext executionContext,
                                       Sharable<?> sharable
    ) {
        if (sharable == null || !sharable.isOptional()) {
            throw new ConditionFailedException(messager.getMessage(Message.NO_OPTIONAL_SHARES, sharable));
        }
    }

    private void checkNewWorldGroupName(ConditionContext<BukkitCommandIssuer> context,
                                        BukkitCommandExecutionContext executionContext,
                                        String worldGroupName
    ) {
        if (this.plugin.getGroupManager().getGroup(worldGroupName) != null) {
            throw new ConditionFailedException(messager.getMessage(Message.GROUP_EXISTS, worldGroupName));
        }
    }

    private void checkSpawnPermission(ConditionContext<BukkitCommandIssuer> context,
                                      BukkitCommandExecutionContext executionContext,
                                      Player player
    ) {
        if (context.getIssuer().isPlayer() && player.equals(context.getIssuer().getPlayer())) {
            if (!context.getIssuer().hasPermission(Perm.COMMAND_SPAWN.getNode())) {
                throw new ConditionFailedException(messager.getMessage(Message.GENERIC_COMMAND_NO_PERMISSION,
                        Perm.COMMAND_SPAWN.getPermission().getDescription(), Perm.COMMAND_SPAWN.getNode()));
            }
            return;
        }
        if (!context.getIssuer().hasPermission(Perm.COMMAND_SPAWN_OTHER.getNode())) {
            throw new ConditionFailedException(messager.getMessage(Message.GENERIC_COMMAND_NO_PERMISSION,
                    Perm.COMMAND_SPAWN_OTHER.getPermission().getDescription(), Perm.COMMAND_SPAWN_OTHER.getNode()));
        }
    }
}
