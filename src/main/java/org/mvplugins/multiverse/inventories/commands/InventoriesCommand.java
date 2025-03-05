package org.mvplugins.multiverse.inventories.commands;

import org.jvnet.hk2.annotations.Contract;
import org.mvplugins.multiverse.core.commandtools.MVCommandManager;
import org.mvplugins.multiverse.core.commandtools.MultiverseCommand;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;

/**
 * Base class for all multiverse inventories commands.
 */
@Contract
public abstract class InventoriesCommand extends MultiverseCommand {
    protected InventoriesCommand(@NotNull MVCommandManager commandManager) {
        super(commandManager, "mvinv");
    }
}
