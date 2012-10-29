package com.onarandombox.multiverseinventories.command.prompts;

import com.onarandombox.multiverseinventories.api.Inventories;
import com.onarandombox.multiverseinventories.locale.Messager;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;

abstract class InventoriesPrompt implements Prompt {

    protected final Inventories plugin;
    protected final Messager messager;
    protected final CommandSender sender;

    InventoriesPrompt(final Inventories plugin, final CommandSender sender) {
        this.plugin = plugin;
        this.messager = plugin.getMessager();
        this.sender = sender;
    }

    @Override
    public boolean blocksForInput(final ConversationContext conversationContext) {
        return true;
    }
}
