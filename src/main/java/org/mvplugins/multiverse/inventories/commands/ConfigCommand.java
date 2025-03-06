package org.mvplugins.multiverse.inventories.commands;

import org.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.core.commandtools.MVCommandIssuer;
import org.mvplugins.multiverse.core.commandtools.MVCommandManager;
import org.mvplugins.multiverse.core.exceptions.MultiverseException;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandAlias;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandCompletion;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandPermission;
import org.mvplugins.multiverse.external.acf.commands.annotation.Description;
import org.mvplugins.multiverse.external.acf.commands.annotation.Optional;
import org.mvplugins.multiverse.external.acf.commands.annotation.Subcommand;
import org.mvplugins.multiverse.external.acf.commands.annotation.Syntax;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.vavr.control.Option;
import org.mvplugins.multiverse.inventories.config.InventoriesConfig;

@Service
@CommandAlias("mvinv")
final class ConfigCommand extends InventoriesCommand {

    private final InventoriesConfig config;

    @Inject
    ConfigCommand(@NotNull MVCommandManager commandManager, @NotNull InventoriesConfig config) {
        super(commandManager);
        this.config = config;
    }

    @Subcommand("config")
    @CommandPermission("multiverse.inventories.config")
    @CommandCompletion("@mvinvconfigs @mvinvconfigvalues")
    @Syntax("<name> [value]")
    @Description("Show or set a config value.")
    void onConfigCommand(
            MVCommandIssuer issuer,

            @Syntax("<name>")
            @Description("The name of the config to set or show.")
            String name,

            @Optional
            @Syntax("[value]")
            @Description("The value to set the config to. If not specified, the current value will be shown.")
            String value) {
        if (value == null) {
            showConfigValue(issuer, name);
            return;
        }
        updateConfigValue(issuer, name, value);
    }

    private void showConfigValue(MVCommandIssuer issuer, String name) {
        config.getStringPropertyHandle().getProperty(name)
                .onSuccess(value -> issuer.sendMessage(name + "is currently set to " + value))
                .onFailure(e -> issuer.sendMessage(e.getMessage()));
    }

    private void updateConfigValue(MVCommandIssuer issuer, String name, String value) {
        // TODO: Update with localization
        config.getStringPropertyHandle().setPropertyString(name, value)
                .onSuccess(ignore -> {
                    config.save();
                    issuer.sendMessage("Successfully set " + name + " to " + value);
                })
                .onFailure(ignore -> issuer.sendMessage("Unable to set " + name + " to " + value + "."))
                .onFailure(MultiverseException.class, e -> Option.of(e.getMVMessage()).peek(issuer::sendError));
    }
}
