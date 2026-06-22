package org.mvplugins.multiverse.inventories.commands;

import org.bukkit.World;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.core.command.MVCommandIssuer;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandCompletion;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandPermission;
import org.mvplugins.multiverse.external.acf.commands.annotation.Description;
import org.mvplugins.multiverse.external.acf.commands.annotation.Subcommand;
import org.mvplugins.multiverse.external.acf.commands.annotation.Syntax;
import org.mvplugins.multiverse.inventories.util.MVInvi18n;

import static org.mvplugins.multiverse.core.locale.message.MessageReplacement.replace;

@Service
final class PlayerDataImportCommand extends InventoriesCommand {

    private static final String IMPORTER_DOWNLOAD_URL = "https://modrinth.com/project/multiverse-inventoriesimporter/";
    private static final String IMPORTER_LEARN_MORE_URL = "https://mvplugins.org/inventories/how-to/import-playerdata/";

    @Subcommand("playerdata import")
    @Syntax("<world>")
    @CommandPermission("multiverse.inventories.importplayerdata")
    @CommandCompletion("@worldwithplayerdata")
    @Description("Import player data from the world's playerdata folder.")
    void onCommand(MVCommandIssuer issuer, World world) {
        issuer.sendError(MVInvi18n.PLAYERDATAIMPORT_IMPORTERNOTINSTALLED);
        issuer.sendInfo(MVInvi18n.PLAYERDATAIMPORT_DOWNLOADLINK, replace("{url}").with(IMPORTER_DOWNLOAD_URL));
        issuer.sendInfo(MVInvi18n.PLAYERDATAIMPORT_LEARNMORE, replace("{url}").with(IMPORTER_LEARN_MORE_URL));
    }
}
