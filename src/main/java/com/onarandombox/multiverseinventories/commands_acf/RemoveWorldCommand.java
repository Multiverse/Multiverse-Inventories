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
public class RemoveWorldCommand extends InventoriesCommand {

    public RemoveWorldCommand(MultiverseInventories plugin) {
        super(plugin);
        this.addPermission(Perm.COMMAND_RMWORLD);
    }

    @Subcommand("removeworld")
    @Syntax("<world> <group>")
    @CommandCompletion("@MVWorlds @worldGroups")
    @Description("Adds a World to a World Group.")
    public void onRemoveWorldCommand(@NotNull CommandSender sender,

                                     @Syntax("<world>")
                                     @Description("World name to remove.")
                                     @NotNull @Flags("other") MultiverseWorld world,

                                     @Syntax("<group>")
                                     @Description("Group you want to remove the world from.")
                                     @NotNull WorldGroup group) {

        if (!group.containsWorld(world.getName())) {
            this.messager.normal(Message.WORLD_NOT_IN_GROUP, sender, world.getName(), group.getName());
            return;
        }

        group.removeWorld(world.getCBWorld());
        this.plugin.getGroupManager().updateGroup(group);
        this.plugin.getMVIConfig().save();
        this.messager.normal(Message.WORLD_REMOVED, sender, world.getName(), group.getName());
    }
}
