package org.mvplugins.multiverse.inventories.command;

import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.core.command.MVCommandManager;
import org.mvplugins.multiverse.core.command.MVCommandPermissions;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.inventories.config.InventoriesConfig;

@Service
public class MVInvCommandPermissions {

    @Inject
    MVInvCommandPermissions(@NotNull MVCommandManager commandManager, @NotNull InventoriesConfig config) {

        MVCommandPermissions commandPermissions = commandManager.getCommandPermissions();
        commandPermissions.registerPermissionChecker("mvinv-gamemode-profile-true", commandIssuer -> config.getEnableGamemodeShareHandling());
        commandPermissions.registerPermissionChecker("mvinv-gamemode-profile-false", commandIssuer -> !config.getEnableGamemodeShareHandling());
    }
}
