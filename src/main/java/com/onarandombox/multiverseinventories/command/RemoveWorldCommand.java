package com.onarandombox.multiverseinventories.command;

import com.onarandombox.MultiverseCore.api.MVWorld;
import com.onarandombox.acf.BukkitCommandIssuer;
import com.onarandombox.acf.annotation.CommandAlias;
import com.onarandombox.acf.annotation.CommandCompletion;
import com.onarandombox.acf.annotation.Description;
import com.onarandombox.acf.annotation.Subcommand;
import com.onarandombox.acf.annotation.Syntax;
import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.WorldGroup;
import com.onarandombox.multiverseinventories.locale.Message;
import com.onarandombox.multiverseinventories.util.Perm;
import org.jetbrains.annotations.NotNull;

@CommandAlias("mvinv")
public class RemoveWorldCommand extends InventoriesCommand {
    public RemoveWorldCommand(@NotNull MultiverseInventories plugin) {
        super(plugin);
        setPerm(Perm.COMMAND_ADDWORLD);
    }

    @Subcommand("removeworld")
    @CommandCompletion("@mvworlds @worldGroups")
    @Syntax("<world> <group>")
    @Description("Adds a World to a World Group.")
    public void onRemoveWorldCommand(BukkitCommandIssuer issuer,

                                     @Syntax("<world>")
                                     @Description("World name to remove.")
                                     MVWorld world,

                                     @Syntax("<group>")
                                     @Description("Group you want to remove the world from.")
                                     @NotNull WorldGroup group
    ) {
        if (!group.containsWorld(world.getName())) {
            this.messager.normal(Message.WORLD_NOT_IN_GROUP, issuer.getIssuer(), world.getName(), group.getName());
            return;
        }
        group.removeWorld(world.getCBWorld());
        this.plugin.getGroupManager().updateGroup(group);
        this.plugin.getMVIConfig().save();
        this.messager.normal(Message.WORLD_REMOVED, issuer.getIssuer(), world.getName(), group.getName());
    }
}
