package com.onarandombox.multiverseinventories.commands_acf;

import com.dumptruckman.minecraft.util.Logging;
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
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

@CommandAlias("mvinv")
public class MigrateCommand extends InventoriesCommand {

    public MigrateCommand(MultiverseInventories plugin) {
        super(plugin);
        this.addPermission(Perm.COMMAND_INFO);
    }

    @Subcommand("migrate")
    @Syntax("<oldname> <newname> [saveold]")
    @CommandCompletion("@players @players saveold")
    @Description("Migrate player data from one name to another.")
    public void onMigrateCommand(@NotNull CommandSender sender,
                                 @NotNull @Flags("type= old name") String oldName,
                                 @NotNull @Flags("type= new name") String newName,
                                 @Nullable @Optional @Values("saveold") String saveOld) {

        boolean deleteOld = saveOld == null || saveOld.equalsIgnoreCase("saveold");

        try {
            this.plugin.getData().migratePlayerData(oldName, newName, Bukkit.getOfflinePlayer(newName).getUniqueId(), deleteOld);
        }
        catch (IOException e) {
            messager.bad(Message.MIGRATE_FAILED, sender, oldName, newName);
            Logging.severe("Could not migrate data from name " + oldName + " to " + newName);
            e.printStackTrace();
            return;
        }

        messager.good(Message.MIGRATE_SUCCESSFUL, sender, oldName, newName);
    }
}