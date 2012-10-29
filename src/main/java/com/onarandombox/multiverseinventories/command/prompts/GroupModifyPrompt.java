package com.onarandombox.multiverseinventories.command.prompts;

import com.onarandombox.multiverseinventories.api.Inventories;
import com.onarandombox.multiverseinventories.api.profile.WorldGroupProfile;
import com.onarandombox.multiverseinventories.locale.Message;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;

class GroupModifyPrompt extends InventoriesPrompt {

    protected final WorldGroupProfile group;

    public GroupModifyPrompt(final Inventories plugin, final CommandSender sender,
                             final WorldGroupProfile group) {
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
