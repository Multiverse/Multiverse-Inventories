package org.mvplugins.multiverse.inventories.commands;

import org.jvnet.hk2.annotations.Contract;
import org.mvplugins.multiverse.core.command.MVCommandManager;
import org.mvplugins.multiverse.core.command.MultiverseCommand;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandAlias;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;

/**
 * Base class for all multiverse inventories commands.
 */
@Contract
@CommandAlias("mvinv")
public abstract class InventoriesCommand extends MultiverseCommand {
}
