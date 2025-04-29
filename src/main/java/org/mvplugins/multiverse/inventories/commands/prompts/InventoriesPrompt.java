package org.mvplugins.multiverse.inventories.commands.prompts;

import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.core.command.MVCommandIssuer;
import org.mvplugins.multiverse.core.command.MVCommandManager;
import org.mvplugins.multiverse.core.locale.PluginLocales;
import org.mvplugins.multiverse.core.locale.message.Message;
import org.mvplugins.multiverse.inventories.MultiverseInventories;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.mvplugins.multiverse.inventories.profile.group.WorldGroupManager;

abstract class InventoriesPrompt implements Prompt {

    protected final MultiverseInventories plugin;
    protected final PluginLocales locales;
    protected final WorldGroupManager worldGroupManager;
    protected final MVCommandIssuer issuer;

    InventoriesPrompt(final MultiverseInventories plugin, final MVCommandIssuer issuer) {
        this.plugin = plugin;
        this.locales = plugin.getServiceLocator().getService(MVCommandManager.class).getLocales();
        this.issuer = issuer;
        this.worldGroupManager = this.plugin.getServiceLocator().getService(WorldGroupManager.class);
    }

    abstract Message getPromptMessage(@NotNull ConversationContext conversationContext);

    @NotNull
    @Override
    public String getPromptText(@NotNull ConversationContext context) {
        return ChatColor.translateAlternateColorCodes('&', getPromptMessage(context).formatted(locales, issuer));
    }

    @Override
    public boolean blocksForInput(@NotNull final ConversationContext conversationContext) {
        return true;
    }
}
