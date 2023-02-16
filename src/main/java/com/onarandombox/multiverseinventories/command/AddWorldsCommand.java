package com.onarandombox.multiverseinventories.command;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
public class AddWorldsCommand extends InventoriesCommand {
    public AddWorldsCommand(@NotNull MultiverseInventories plugin) {
        super(plugin);
        setPerm(Perm.COMMAND_ADDWORLD);
    }

    @Subcommand("addworlds")
    @CommandCompletion("@mvworlds:multiple @worldGroups")
    @Syntax("<world[,extra]> <group>")
    @Description("Adds a World to a World Group.")
    public void onAddWorldCommand(BukkitCommandIssuer issuer,

                                  @Syntax("<world>")
                                  @Description("World name to add.")
                                  MVWorld[] world,

                                  @Syntax("<group>")
                                  @Description("Group you want to add the world to.")
                                  WorldGroup group
    ) {
        List<String> worldNames = Arrays.stream(world).map(MVWorld::getName).collect(Collectors.toList());
        String worldNamesString = String.join(", ", worldNames);
        if (!group.getWorlds().addAll(worldNames)) {
            this.messager.normal(Message.WORLD_ALREADY_EXISTS, issuer.getIssuer(), worldNamesString, group.getName());
            return;
        }
        this.plugin.getGroupManager().updateGroup(group);
        this.plugin.getMVIConfig().save();
        this.messager.normal(Message.WORLD_ADDED, issuer.getIssuer(), worldNamesString, group.getName());
    }
}
