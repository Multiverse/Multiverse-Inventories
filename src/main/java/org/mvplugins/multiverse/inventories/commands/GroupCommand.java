package org.mvplugins.multiverse.inventories.commands;

import org.mvplugins.multiverse.core.command.LegacyAliasCommand;
import org.mvplugins.multiverse.core.command.MVCommandIssuer;
import org.mvplugins.multiverse.inventories.MultiverseInventories;
import org.mvplugins.multiverse.inventories.commands.prompts.GroupControlPrompt;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.Conversable;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationFactory;
import org.mvplugins.multiverse.core.command.MVCommandManager;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandAlias;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandPermission;
import org.mvplugins.multiverse.external.acf.commands.annotation.Description;
import org.mvplugins.multiverse.external.acf.commands.annotation.Subcommand;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.inventories.util.MVInvi18n;

@Service
class GroupCommand extends InventoriesCommand {

    private final MultiverseInventories plugin;

    @Inject
    GroupCommand(@NotNull MultiverseInventories plugin) {
        this.plugin = plugin;
    }

    @Subcommand("group")
    @CommandPermission("multiverse.inventories.group")
    @Description("Manage a world group with prompts!")
    void onGroupCommand(@NotNull MVCommandIssuer issuer) {
        if (!(issuer.getIssuer() instanceof Conversable conversable)) {
            issuer.sendError(MVInvi18n.GROUP_NONCONVERSABLE);
            return;
        }
        Conversation conversation = new ConversationFactory(plugin)
                .withFirstPrompt(new GroupControlPrompt(plugin, issuer))
                .withEscapeSequence("##")
                .withModality(false).buildConversation(conversable);
        conversation.begin();
    }

    @Service
    private final static class LegacyAlias extends GroupCommand implements LegacyAliasCommand {
        @Inject
        LegacyAlias(MultiverseInventories plugin) {
            super(plugin);
        }

        @Override
        @CommandAlias("mvinvgroup|mvinvg")
        void onGroupCommand(MVCommandIssuer issuer) {
            super.onGroupCommand(issuer);
        }
    }
}
