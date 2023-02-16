package com.onarandombox.multiverseinventories.command;

import java.util.Arrays;
import java.util.Set;
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
public class RemoveWorldsCommand extends InventoriesCommand {
    public RemoveWorldsCommand(@NotNull MultiverseInventories plugin) {
        super(plugin);
        setPerm(Perm.COMMAND_ADDWORLD);
    }

    @Subcommand("removeworlds")
    @CommandCompletion("@mvworlds @worldGroups")
    @Syntax("<world[,extra]> <group>")
    @Description("Adds a World to a World Group.")
    public void onRemoveWorldCommand(BukkitCommandIssuer issuer,

                                     @Syntax("<world>")
                                     @Description("World name to remove.")
                                     MVWorld[] world,

                                     @Syntax("<group>")
                                     @Description("Group you want to remove the world from.")
                                     @NotNull WorldGroup group
    ) {
        Set<String> worldNames = Arrays.stream(world).map(MVWorld::getName).collect(Collectors.toSet());
        String worldNamesString = String.join(", ", worldNames);
        if (!group.getWorlds().removeAll(worldNames)) {
            this.messager.normal(Message.WORLD_NOT_IN_GROUP, issuer.getIssuer(), worldNamesString, group.getName());
            return;
        }
        this.plugin.getGroupManager().updateGroup(group);
        this.plugin.getMVIConfig().save();
        this.messager.normal(Message.WORLD_REMOVED, issuer.getIssuer(), worldNamesString, group.getName());
    }
}
