package com.onarandombox.multiverseinventories.commands.prompts;

import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.locale.Messager;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;

abstract class InventoriesPrompt implements Prompt {

    protected final MultiverseInventories plugin;
    protected final Messager messager;
    protected final CommandSender sender;

    InventoriesPrompt(final MultiverseInventories plugin, final CommandSender sender) {
        this.plugin = plugin;
        this.messager = plugin.getMessager();
        this.sender = sender;
    }

    @Override
    public boolean blocksForInput(final ConversationContext conversationContext) {
        return true;
    }
}
