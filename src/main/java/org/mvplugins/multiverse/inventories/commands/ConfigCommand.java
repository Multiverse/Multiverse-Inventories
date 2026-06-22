package org.mvplugins.multiverse.inventories.commands;

import org.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.core.command.MVCommandIssuer;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandCompletion;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandPermission;
import org.mvplugins.multiverse.external.acf.commands.annotation.Description;
import org.mvplugins.multiverse.external.acf.commands.annotation.Optional;
import org.mvplugins.multiverse.external.acf.commands.annotation.Subcommand;
import org.mvplugins.multiverse.external.acf.commands.annotation.Syntax;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.inventories.config.InventoriesConfig;
import org.mvplugins.multiverse.inventories.util.MVInvi18n;

import static org.mvplugins.multiverse.core.locale.message.MessageReplacement.replace;

@Service
final class ConfigCommand extends InventoriesCommand {

    private final InventoriesConfig config;

    @Inject
    ConfigCommand(@NotNull InventoriesConfig config) {
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
                .onSuccess(value -> issuer.sendMessage(MVInvi18n.CONFIG_CURRENTVALUE,
                        replace("{name}").with(name),
                        replace("{value}").with(value)))
                .onFailure(e -> issuer.sendError(MVInvi18n.CONFIG_SHOWFAILED, replace("{error}").with(e)));
    }

    private void updateConfigValue(MVCommandIssuer issuer, String name, String value) {
        config.getStringPropertyHandle().setPropertyString(name, value)
                .onSuccess(ignore -> {
                    config.save();
                    issuer.sendMessage(MVInvi18n.CONFIG_SET,
                            replace("{name}").with(name),
                            replace("{value}").with(value));
                })
                .onFailure(e -> issuer.sendError(MVInvi18n.CONFIG_SETFAILED,
                        replace("{name}").with(name),
                        replace("{value}").with(value),
                        replace("{error}").with(e)));
    }
}
