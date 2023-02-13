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
import org.bukkit.Bukkit;
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

                                 @Flags("type= old name")
                                 @Syntax("<oldname>")
                                 @Description("The old name of the player to migrate data from.")
                                 String oldName,

                                 @NotNull
                                 @Flags("type= new name")
                                 @Syntax("<newname>")
                                 @Description("The new name of the player to migrate data to.")
                                 String newName,

                                 @Optional
                                 @Values("--save-old")
                                 String saveOld
    ) {
        boolean deleteOld = saveOld == null || saveOld.equalsIgnoreCase("--save-old");

        try {
            this.plugin.getData().migratePlayerData(oldName, newName, Bukkit.getOfflinePlayer(newName).getUniqueId(), deleteOld);
        }
        catch (IOException e) {
            messager.bad(Message.MIGRATE_FAILED, issuer.getIssuer(), oldName, newName);
            Logging.severe("Could not migrate data from name " + oldName + " to " + newName);
            e.printStackTrace();
            return;
        }

        messager.good(Message.MIGRATE_SUCCESSFUL, issuer.getIssuer(), oldName, newName);
    }
}
