package org.mvplugins.multiverse.inventories.commands.prompts;

import org.mvplugins.multiverse.inventories.MultiverseInventories;
import org.mvplugins.multiverse.inventories.WorldGroup;
import org.mvplugins.multiverse.inventories.locale.Message;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;

class GroupEditPrompt extends InventoriesPrompt {

    public GroupEditPrompt(final MultiverseInventories plugin, final CommandSender sender) {
        super(plugin, sender);
    }

    @Override
    public String getPromptText(final ConversationContext conversationContext) {
        final StringBuilder builder = new StringBuilder();
        for (WorldGroup group : plugin.getGroupManager().getGroups()) {
            if (builder.length() == 0) {
                builder.append(ChatColor.WHITE);
            } else {
                builder.append(ChatColor.GOLD).append(", ").append(ChatColor.WHITE);
            }
            builder.append(group.getName());
        }
        return messager.getMessage(Message.GROUP_EDIT_PROMPT, builder.toString());
    }

    @Override
    public Prompt acceptInput(final ConversationContext conversationContext, final String s) {
        final WorldGroup group = plugin.getGroupManager().getGroup(s);
        if (group == null) {
            messager.normal(Message.ERROR_NO_GROUP, sender, s);
        } else {
            return new GroupModifyPrompt(plugin, sender, group);
        }
        return Prompt.END_OF_CONVERSATION;
    }
}
