package com.onarandombox.multiverseinventories.commands_acf;

import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.acf.annotation.CommandAlias;
import com.onarandombox.acf.annotation.CommandCompletion;
import com.onarandombox.acf.annotation.Description;
import com.onarandombox.acf.annotation.Flags;
import com.onarandombox.acf.annotation.Subcommand;
import com.onarandombox.acf.annotation.Syntax;
import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.WorldGroup;
import com.onarandombox.multiverseinventories.locale.Message;
import com.onarandombox.multiverseinventories.util.Perm;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

@CommandAlias("mvinv")
public class AddWorldCommand extends InventoriesCommand {

    public AddWorldCommand(MultiverseInventories plugin) {
        super(plugin);
        this.addPermission(Perm.COMMAND_ADDWORLD);
    }

    @Subcommand("addworld")
    @Syntax("<world> <group>")
    @CommandCompletion("@MVWorlds @worldGroups")
    @Description("Adds a World to a World Group.")
    public void onAddWorldCommand(@NotNull CommandSender sender,
                                  @NotNull @Flags("other") MultiverseWorld world,
                                  @NotNull WorldGroup group) {

        if (group.containsWorld(world.getName())) {
            this.messager.normal(Message.WORLD_ALREADY_EXISTS, sender, world.getName(), group.getName());
            return;
        }

        group.addWorld(world.getCBWorld());
        this.plugin.getGroupManager().updateGroup(group);
        this.plugin.getMVIConfig().save();
        this.messager.normal(Message.WORLD_ADDED, sender, world.getName(), group.getName());
    }
}
