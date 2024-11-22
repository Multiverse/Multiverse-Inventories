package com.onarandombox.multiverseinventories.commands;

import org.mvplugins.multiverse.core.commandtools.MVCommandManager;
import org.mvplugins.multiverse.core.commandtools.MultiverseCommand;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.external.jvnet.hk2.annotations.Contract;

/**
 * Base class for all multiverse inventories commands.
 */
@Contract
public abstract class InventoriesCommand extends MultiverseCommand {
    protected InventoriesCommand(@NotNull MVCommandManager commandManager) {
        super(commandManager);
    }
}
