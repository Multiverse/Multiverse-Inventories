package org.mvplugins.multiverse.inventories.commands.prompts;

import org.mvplugins.multiverse.inventories.MultiverseInventories;
import org.mvplugins.multiverse.inventories.profile.group.WorldGroup;
import org.mvplugins.multiverse.inventories.locale.Message;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;

class GroupCreatePrompt extends InventoriesPrompt {

    public GroupCreatePrompt(final MultiverseInventories plugin, final CommandSender sender) {
        super(plugin, sender);
    }

    @Override
    public String getPromptText(final ConversationContext conversationContext) {
        return messager.getMessage(Message.GROUP_CREATE_PROMPT);
    }

    @Override
    public Prompt acceptInput(final ConversationContext conversationContext, final String s) {
        final WorldGroup group = worldGroupManager.getGroup(s);
        if (group == null) {
            if (s.isEmpty() || !s.matches("^[a-zA-Z0-9][a-zA-Z0-9_]*$")) {
                messager.normal(Message.GROUP_INVALID_NAME, sender);
                return this;
            }
            final WorldGroup newGroup = worldGroupManager.newEmptyGroup(s);
            return new GroupWorldsPrompt(plugin, sender, newGroup,
                    new GroupSharesPrompt(plugin, sender, newGroup, Prompt.END_OF_CONVERSATION, true), true);
        } else {
            messager.normal(Message.GROUP_EXISTS, sender, s);
        }
        return Prompt.END_OF_CONVERSATION;
    }
}
