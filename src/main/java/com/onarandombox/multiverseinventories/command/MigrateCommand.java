package com.onarandombox.multiverseinventories.command;

import java.io.IOException;

import com.dumptruckman.minecraft.util.Logging;
import com.onarandombox.acf.BukkitCommandIssuer;
import com.onarandombox.acf.annotation.CommandAlias;
import com.onarandombox.acf.annotation.CommandCompletion;
import com.onarandombox.acf.annotation.Description;
import com.onarandombox.acf.annotation.Flags;
import com.onarandombox.acf.annotation.Optional;
import com.onarandombox.acf.annotation.Subcommand;
import com.onarandombox.acf.annotation.Syntax;
import com.onarandombox.acf.annotation.Values;
import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.locale.Message;
import com.onarandombox.multiverseinventories.util.Perm;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

@CommandAlias("mvinv")
public class MigrateCommand extends InventoriesCommand {
    public MigrateCommand(@NotNull MultiverseInventories plugin) {
        super(plugin);
        setPerm(Perm.COMMAND_MIGRATE);
    }

    @Subcommand("migrate")
    @CommandCompletion("@players @players --save-old")
    @Syntax("<oldname> <newname> [saveold]")
    @Description("Migrate player data from one name to another.")
    public void onMigrateCommand(BukkitCommandIssuer issuer,

                                 @Syntax("<oldname>")
                                 @Description("The old name of the player to migrate data from.")
                                 OfflinePlayer oldPlayer,

                                 @Syntax("<newname>")
                                 @Description("The new name of the player to migrate data to.")
                                 OfflinePlayer newPlayer,

                                 @Optional
                                 @Values("--save-old")
                                 String saveOld
    ) {
        if (oldPlayer.equals(newPlayer)) {
            messager.bad(Message.MIGRATE_SAME_PLAYER, issuer.getIssuer());
            return;
        }

        boolean deleteOld = saveOld == null || saveOld.equalsIgnoreCase("--save-old");

        try {
            this.plugin.getData().migratePlayerData(oldPlayer.getName(), newPlayer.getName(), newPlayer.getUniqueId(), deleteOld);
        }
        catch (IOException e) {
            messager.bad(Message.MIGRATE_FAILED, issuer.getIssuer(), oldPlayer.getName(), newPlayer.getName());
            Logging.severe("Could not migrate data from name " + oldPlayer.getName() + " to " + newPlayer.getName());
            e.printStackTrace();
            return;
        }

        messager.good(Message.MIGRATE_SUCCESSFUL, issuer.getIssuer(), oldPlayer.getName(), newPlayer.getName());
    }
}
