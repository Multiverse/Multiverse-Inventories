package org.mvplugins.multiverse.inventories.commands.prompts;

import org.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.core.command.MVCommandIssuer;
import org.mvplugins.multiverse.core.locale.message.Message;
import org.mvplugins.multiverse.inventories.MultiverseInventories;
import org.mvplugins.multiverse.inventories.profile.group.WorldGroup;
import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.mvplugins.multiverse.inventories.util.MVInvi18n;

import static org.mvplugins.multiverse.core.locale.message.MessageReplacement.replace;

final class GroupEditPrompt extends InventoriesPrompt {

    public GroupEditPrompt(final MultiverseInventories plugin, final MVCommandIssuer issuer) {
        super(plugin, issuer);
    }

    @NotNull
    @Override
    public Message getPromptMessage(@NotNull final ConversationContext conversationContext) {
        final StringBuilder builder = new StringBuilder();
        for (WorldGroup group : worldGroupManager.getGroups()) {
            if (builder.isEmpty()) {
                builder.append(ChatColor.WHITE);
            } else {
                builder.append(ChatColor.GOLD).append(", ").append(ChatColor.WHITE);
            }
            builder.append(group.getName());
        }
        return Message.of(MVInvi18n.GROUP_EDITPROMPT, replace("{groups}").with(builder.toString()));
    }

    @Override
    public Prompt acceptInput(@NotNull final ConversationContext conversationContext, final String input) {
        final WorldGroup group = worldGroupManager.getGroup(input);
        if (group == null) {
            issuer.sendError(MVInvi18n.ERROR_NOGROUP, replace("{group}").with(input));
        } else {
            return new GroupModifyPrompt(plugin, issuer, group);
        }
        return Prompt.END_OF_CONVERSATION;
    }
}
