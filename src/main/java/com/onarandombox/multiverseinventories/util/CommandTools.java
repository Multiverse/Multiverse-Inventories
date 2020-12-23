package com.onarandombox.multiverseinventories.util;

import com.onarandombox.MultiverseCore.commandTools.MVCommandManager;
import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.commands_acf.GroupCommand;

public class CommandTools {

    private final MultiverseInventories plugin;
    private final MVCommandManager manager;

    public CommandTools(MultiverseInventories plugin) {
        this.plugin = plugin;
        this.manager = plugin.getCore().getMVCommandManager();

        // Completions

        // Contexts

        // Conditions

        //Commands
        this.manager.registerCommand(new GroupCommand(this.plugin));
    }
}
