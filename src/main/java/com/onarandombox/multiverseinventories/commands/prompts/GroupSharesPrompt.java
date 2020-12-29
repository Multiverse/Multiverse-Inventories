package com.onarandombox.multiverseinventories.commands.prompts;

import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.WorldGroup;
import com.onarandombox.multiverseinventories.share.Sharable;
import com.onarandombox.multiverseinventories.share.Sharables;
import com.onarandombox.multiverseinventories.share.Shares;
import com.onarandombox.multiverseinventories.locale.Message;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;

class GroupSharesPrompt extends InventoriesPrompt {

    protected final WorldGroup group;
    protected final Prompt nextPrompt;
    protected final boolean isCreating;
    protected final Shares shares;

    public GroupSharesPrompt(final MultiverseInventories plugin, final CommandSender sender,
                             final WorldGroup group, final Prompt nextPrompt,
                             final boolean creatingGroup) {
        super(plugin, sender);
        this.group = group;
        this.nextPrompt = nextPrompt;
        this.isCreating = creatingGroup;
        this.shares = Sharables.fromShares(group.getShares());
    }

    @Override
    public String getPromptText(final ConversationContext conversationContext) {
        final StringBuilder builder = new StringBuilder();
        for (final Sharable sharable : shares) {
            if (builder.length() == 0) {
                builder.append(ChatColor.WHITE);
            } else {
                builder.append(ChatColor.GOLD).append(", ").append(ChatColor.WHITE);
            }
            builder.append(sharable.toString());
        }
        return messager.getMessage(Message.GROUP_SHARES_PROMPT, group.getName(), builder.toString());
    }

    @Override
    public Prompt acceptInput(final ConversationContext conversationContext, final String s) {
        if (s.equals("@")) {
            group.getShares().clear();
            group.getShares().addAll(this.shares);
            plugin.getGroupManager().updateGroup(group);
            if (isCreating) {
                messager.normal(Message.GROUP_CREATION_COMPLETE, sender);
            } else {
                messager.normal(Message.GROUP_UPDATED, sender);
            }
            messager.normal(Message.INFO_GROUP, sender, group.getName());
            messager.normal(Message.INFO_GROUPS_INFO, sender, group.getWorlds(), group.getShares());
            plugin.getGroupManager().checkForConflicts(sender);
            return nextPrompt;
        }
        boolean negative = false;
        final Shares shares;
        if (s.startsWith("-") && s.length() > 1) {
            negative = true;
            shares = Sharables.lookup(s.toLowerCase().substring(1));
        } else {
            shares = Sharables.lookup(s.toLowerCase());
        }

        if (shares == null) {
            messager.normal(Message.ERROR_NO_SHARES_SPECIFIED, sender);
        } else {
            if (!negative) {
                this.shares.addAll(shares);
            } else {
                this.shares.removeAll(shares);
            }
        }
        return this;
    }
}
