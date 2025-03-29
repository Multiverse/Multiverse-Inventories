package org.mvplugins.multiverse.inventories.commands.prompts;

import org.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.core.command.MVCommandIssuer;
import org.mvplugins.multiverse.core.locale.message.Message;
import org.mvplugins.multiverse.inventories.MultiverseInventories;
import org.mvplugins.multiverse.inventories.profile.group.WorldGroup;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.mvplugins.multiverse.inventories.util.MVInvi18n;

import static org.mvplugins.multiverse.core.locale.message.MessageReplacement.replace;

final class GroupCreatePrompt extends InventoriesPrompt {

    public GroupCreatePrompt(final MultiverseInventories plugin, final MVCommandIssuer issuer) {
        super(plugin, issuer);
    }

    @NotNull
    @Override
    Message getPromptMessage(@NotNull final ConversationContext conversationContext) {
        return Message.of(MVInvi18n.GROUP_CREATEPROMPT);
    }

    @Override
    public Prompt acceptInput(@NotNull final ConversationContext conversationContext, final String s) {
        final WorldGroup group = worldGroupManager.getGroup(s);
        if (group == null) {
            if (s.isEmpty() || !s.matches("^[a-zA-Z0-9][a-zA-Z0-9_]*$")) {
                issuer.sendError(MVInvi18n.GROUP_INVALIDNAME);
                return this;
            }
            final WorldGroup newGroup = worldGroupManager.newEmptyGroup(s);
            return new GroupWorldsPrompt(plugin, issuer, newGroup,
                    new GroupSharesPrompt(plugin, issuer, newGroup, Prompt.END_OF_CONVERSATION, true), true);
        } else {
            issuer.sendError(MVInvi18n.GROUP_EXISTS, replace("{group}").with(s));
        }
        return Prompt.END_OF_CONVERSATION;
    }
}
