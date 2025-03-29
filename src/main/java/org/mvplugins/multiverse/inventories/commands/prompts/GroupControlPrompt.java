package org.mvplugins.multiverse.inventories.commands.prompts;

import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.core.commandtools.MVCommandIssuer;
import org.mvplugins.multiverse.core.locale.message.Message;
import org.mvplugins.multiverse.inventories.MultiverseInventories;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.mvplugins.multiverse.inventories.util.MVInvi18n;

public final class GroupControlPrompt extends InventoriesPrompt {

    public GroupControlPrompt(final MultiverseInventories plugin, final MVCommandIssuer issuer) {
        super(plugin, issuer);
    }

    @NotNull
    @Override
    Message getPromptMessage(@NotNull final ConversationContext conversationContext) {
        return Message.of(MVInvi18n.GROUP_COMMANDPROMPT);
    }

    @Override
    public Prompt acceptInput(@NotNull final ConversationContext conversationContext, final String input) {
        if (input.equalsIgnoreCase("delete")) {
            return new GroupDeletePrompt(plugin, issuer);
        } else if (input.equalsIgnoreCase("create")) {
            return new GroupCreatePrompt(plugin, issuer);
        } else if (input.equalsIgnoreCase("edit")) {
            return new GroupEditPrompt(plugin, issuer);
        } else {
            issuer.sendError(MVInvi18n.GROUP_INVALIDOPTION);
            return this;
        }
    }
}
