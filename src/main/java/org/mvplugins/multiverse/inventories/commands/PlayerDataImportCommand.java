package org.mvplugins.multiverse.inventories.commands;

import org.bukkit.World;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.core.command.MVCommandIssuer;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandCompletion;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandPermission;
import org.mvplugins.multiverse.external.acf.commands.annotation.Description;
import org.mvplugins.multiverse.external.acf.commands.annotation.Subcommand;
import org.mvplugins.multiverse.external.acf.commands.annotation.Syntax;

@Service
final class PlayerDataImportCommand extends InventoriesCommand {

    @Subcommand("playerdata import")
    @Syntax("<world>")
    @CommandPermission("multiverse.inventories.importplayerdata")
    @CommandCompletion("@worldwithplayerdata")
    @Description("Import player data from the world's playerdata folder.")
    void onCommand(MVCommandIssuer issuer, World world) {
        issuer.sendError("Please install Multiverse-InventoriesImporter plugin to use this command.");
        issuer.sendInfo("Download Link: https://modrinth.com/project/multiverse-inventoriesimporter/");
        issuer.sendInfo("Learn More: https://mvplugins.org/inventories/how-to/import-playerdata/");
    }
}
