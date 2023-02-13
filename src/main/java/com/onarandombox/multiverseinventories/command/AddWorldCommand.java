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
public class AddWorldCommand extends InventoriesCommand {
    public AddWorldCommand(@NotNull MultiverseInventories plugin) {
        super(plugin);
        setPerm(Perm.COMMAND_ADDWORLD);
    }

    @Subcommand("addworld")
    @CommandCompletion("@mvworlds @worldGroups")
    @Syntax("<world> <group>")
    @Description("Adds a World to a World Group.")
    public void onAddWorldCommand(BukkitCommandIssuer issuer,

                                  @Syntax("<world>")
                                  @Description("World name to add.")
                                  MVWorld world,

                                  @Syntax("<group>")
                                  @Description("Group you want to add the world to.")
                                  WorldGroup group) {

        if (group.containsWorld(world.getName())) {
            this.messager.normal(Message.WORLD_ALREADY_EXISTS, issuer.getIssuer(), world.getName(), group.getName());
            return;
        }
        group.addWorld(world.getCBWorld());
        this.plugin.getGroupManager().updateGroup(group);
        this.plugin.getMVIConfig().save();
        this.messager.normal(Message.WORLD_ADDED, issuer.getIssuer(), world.getName(), group.getName());
    }
}
