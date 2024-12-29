package org.mvplugins.multiverse.inventories.commands.prompts;

import org.mvplugins.multiverse.inventories.MultiverseInventories;
import org.mvplugins.multiverse.inventories.locale.Message;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;

public class GroupControlPrompt extends InventoriesPrompt {

    public GroupControlPrompt(final MultiverseInventories plugin, final CommandSender sender) {
        super(plugin, sender);
    }

    @Override
    public String getPromptText(final ConversationContext conversationContext) {
        return messager.getMessage(Message.GROUP_COMMAND_PROMPT);
    }

    @Override
    public Prompt acceptInput(final ConversationContext conversationContext, final String s) {
        if (s.equalsIgnoreCase("delete")) {
            return new GroupDeletePrompt(plugin, sender);
        } else if (s.equalsIgnoreCase("create")) {
            return new GroupCreatePrompt(plugin, sender);
        } else if (s.equalsIgnoreCase("edit")) {
            return new GroupEditPrompt(plugin, sender);
        } else {
            messager.normal(Message.INVALID_PROMPT_OPTION, sender);
            return this;
        }
    }
}
