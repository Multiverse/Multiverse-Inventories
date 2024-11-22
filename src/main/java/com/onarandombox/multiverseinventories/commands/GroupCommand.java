package com.onarandombox.multiverseinventories.commands;

import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.commands.prompts.GroupControlPrompt;
import com.onarandombox.multiverseinventories.locale.Message;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.Conversable;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationFactory;
import org.mvplugins.multiverse.core.commandtools.MVCommandManager;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandAlias;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandPermission;
import org.mvplugins.multiverse.external.acf.commands.annotation.Description;
import org.mvplugins.multiverse.external.acf.commands.annotation.Subcommand;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.external.jvnet.hk2.annotations.Service;

@Service
@CommandAlias("mvinv")
class GroupCommand extends InventoriesCommand {

    private final MultiverseInventories plugin;

    @Inject
    GroupCommand(@NotNull MVCommandManager commandManager, @NotNull MultiverseInventories plugin) {
        super(commandManager);
        this.plugin = plugin;
    }

    @CommandAlias("mvinvgroup|mvinvg")
    @Subcommand("group")
    @CommandPermission("multiverse.inventories.group")
    @Description("Manage a world group wiht prompts!")
    void onGroupCommand(@NotNull CommandSender sender) {
        if (!(sender instanceof Conversable conversable)) {
            this.plugin.getMessager().normal(Message.NON_CONVERSABLE, sender);
            return;
        }
        Conversation conversation = new ConversationFactory(plugin)
                .withFirstPrompt(new GroupControlPrompt(plugin, sender))
                .withEscapeSequence("##")
                .withModality(false).buildConversation(conversable);
        conversation.begin();
    }
}
