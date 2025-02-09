package org.mvplugins.multiverse.inventories.commands.prompts;

import org.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.core.commandtools.MVCommandIssuer;
import org.mvplugins.multiverse.core.locale.message.Message;
import org.mvplugins.multiverse.inventories.MultiverseInventories;
import org.mvplugins.multiverse.inventories.profile.group.WorldGroup;
import org.mvplugins.multiverse.inventories.share.Sharable;
import org.mvplugins.multiverse.inventories.share.Sharables;
import org.mvplugins.multiverse.inventories.share.Shares;
import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.mvplugins.multiverse.inventories.util.MVInvi18n;

import static org.mvplugins.multiverse.core.locale.message.MessageReplacement.replace;

class GroupSharesPrompt extends InventoriesPrompt {

    protected final WorldGroup group;
    protected final Prompt nextPrompt;
    protected final boolean isCreating;
    protected final Shares shares;

    public GroupSharesPrompt(final MultiverseInventories plugin, final MVCommandIssuer issuer,
                             final WorldGroup group, final Prompt nextPrompt,
                             final boolean creatingGroup) {
        super(plugin, issuer);
        this.group = group;
        this.nextPrompt = nextPrompt;
        this.isCreating = creatingGroup;
        this.shares = Sharables.fromShares(group.getShares());
    }

    @NotNull
    @Override
    public Message getPromptMessage(@NotNull final ConversationContext conversationContext) {
        final StringBuilder builder = new StringBuilder();
        for (final Sharable sharable : shares) {
            if (builder.isEmpty()) {
                builder.append(ChatColor.WHITE);
            } else {
                builder.append(ChatColor.GOLD).append(", ").append(ChatColor.WHITE);
            }
            builder.append(sharable.toString());
        }
        return Message.of(MVInvi18n.GROUP_SHARESPROMPT,
                replace("{group}").with(group.getName()),
                replace("{shares}").with(builder.toString()));
    }

    @Override
    public Prompt acceptInput(@NotNull final ConversationContext conversationContext, final String input) {
        if ("@".equals(input)) {
            group.getShares().clear();
            group.getShares().addAll(this.shares);
            worldGroupManager.updateGroup(group);
            if (isCreating) {
                issuer.sendInfo(MVInvi18n.GROUP_CREATIONCOMPLETE);
            } else {
                issuer.sendInfo(MVInvi18n.GROUP_UPDATED);
            }
            issuer.sendInfo(MVInvi18n.INFO_GROUP, replace("{group}").with(group.getName()));
            issuer.sendInfo(MVInvi18n.INFO_GROUP_INFO, replace("{worlds}").with(group.getWorlds()));
            issuer.sendInfo(MVInvi18n.INFO_GROUP_INFOSHARES, replace("{shares}").with(group.getShares()));
            worldGroupManager.checkForConflicts(issuer);
            return nextPrompt;
        }

        boolean negative = false;
        Shares shares = Sharables.lookup(input.toLowerCase());
        if (shares == null && input.startsWith("-") && input.length() > 1) {
            negative = true;
            shares = Sharables.lookup(input.toLowerCase().substring(1));
        }

        if (shares == null) {
            issuer.sendError(MVInvi18n.ERROR_NOSHARESSPECIFIED);
            return this;
        }
        if (negative) {
            this.shares.removeAll(shares);
            return this;
        }
        this.shares.addAll(shares);
        return this;
    }
}
