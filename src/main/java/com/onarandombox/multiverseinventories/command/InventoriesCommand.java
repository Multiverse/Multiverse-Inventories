package com.onarandombox.multiverseinventories.command;

import com.onarandombox.MultiverseCore.api.MVPlugin;
import com.onarandombox.MultiverseCore.commandtools.MultiverseCommand;
import com.onarandombox.acf.CommandIssuer;
import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.locale.Messager;
import com.onarandombox.multiverseinventories.util.Perm;
import org.jetbrains.annotations.NotNull;

abstract class InventoriesCommand extends MultiverseCommand {
    protected final MultiverseInventories plugin;
    protected final Messager messager;

    private Perm perm = null;

    protected InventoriesCommand(@NotNull MultiverseInventories plugin) {
        super(plugin);
        this.plugin = plugin;
        this.messager = plugin.getMessager();
    }

    public void setPerm(Perm perm) {
        this.perm = perm;
    }

    @Override
    public boolean hasPermission(CommandIssuer issuer) {
        return perm == null || issuer.hasPermission(perm.getNode());
    }
}
