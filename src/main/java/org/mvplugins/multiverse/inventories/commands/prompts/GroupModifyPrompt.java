package org.mvplugins.multiverse.inventories.commands.prompts;

import org.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.core.commandtools.MVCommandIssuer;
import org.mvplugins.multiverse.core.locale.message.Message;
import org.mvplugins.multiverse.inventories.MultiverseInventories;
import org.mvplugins.multiverse.inventories.profile.group.WorldGroup;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.mvplugins.multiverse.inventories.util.MVInvi18n;

import static org.mvplugins.multiverse.core.locale.message.MessageReplacement.replace;

class GroupModifyPrompt extends InventoriesPrompt {

    protected final WorldGroup group;

    public GroupModifyPrompt(final MultiverseInventories plugin, final MVCommandIssuer issuer,
                             final WorldGroup group) {
        super(plugin, issuer);
        this.group = group;
    }

    @NotNull
    @Override
    public Message getPromptMessage(@NotNull final ConversationContext conversationContext) {
        return Message.of(MVInvi18n.GROUP_MODIFYPROMPT, replace("{group}").with(group.getName()));
    }

    @Override
    public Prompt acceptInput(@NotNull final ConversationContext conversationContext, final String input) {
        if ("worlds".equalsIgnoreCase(input)) {
            return new GroupWorldsPrompt(plugin, issuer, group, this, false);
        } else if (input.equalsIgnoreCase("shares")) {
            return new GroupSharesPrompt(plugin, issuer, group, this, false);
        } else {
            issuer.sendError(MVInvi18n.GROUP_INVALIDOPTION);
            return this;
        }
    }
}
