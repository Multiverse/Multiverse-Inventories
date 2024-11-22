package org.mvplugins.multiverse.inventories.commands.prompts;

import org.mvplugins.multiverse.inventories.MultiverseInventories;
import org.mvplugins.multiverse.inventories.WorldGroup;
import org.mvplugins.multiverse.inventories.locale.Message;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;

class GroupModifyPrompt extends InventoriesPrompt {

    protected final WorldGroup group;

    public GroupModifyPrompt(final MultiverseInventories plugin, final CommandSender sender,
                             final WorldGroup group) {
        super(plugin, sender);
        this.group = group;
    }

    @Override
    public String getPromptText(final ConversationContext conversationContext) {
        return messager.getMessage(Message.GROUP_MODIFY_PROMPT, group.getName());
    }

    @Override
    public Prompt acceptInput(final ConversationContext conversationContext, final String s) {
        if (s.equalsIgnoreCase("worlds")) {
            return new GroupWorldsPrompt(plugin, sender, group, this, false);
        } else if (s.equalsIgnoreCase("shares")) {
            return new GroupSharesPrompt(plugin, sender, group, this, false);
        } else {
            messager.normal(Message.INVALID_PROMPT_OPTION, sender);
            return this;
        }
    }
}
