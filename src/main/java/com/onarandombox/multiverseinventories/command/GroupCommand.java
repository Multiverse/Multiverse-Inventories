package com.onarandombox.multiverseinventories.command;

import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.command.prompts.GroupControlPrompt;
import com.onarandombox.multiverseinventories.locale.Message;
import com.onarandombox.multiverseinventories.util.Perm;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.Conversable;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationFactory;

import java.util.List;

/**
 * The /mvi info Command.
 */
public class GroupCommand extends InventoriesCommand {

    public GroupCommand(MultiverseInventories plugin) {
        super(plugin);
        this.setName("Creates a world group.");
        this.setCommandUsage("/mvinv group");
        this.setArgRange(0, 0);
        this.addKey("mvinv group");
        this.addKey("mvinv g");
        this.addKey("mvinvgroup");
        this.addKey("mvinvg");
        this.setPermission(Perm.COMMAND_GROUP.getPermission());
    }

    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        if (!(sender instanceof Conversable)) {
            this.messager.normal(Message.NON_CONVERSABLE, sender);
            return;
        }

        Conversable conversable = (Conversable) sender;
        Conversation conversation = new ConversationFactory(this.plugin)
                .withFirstPrompt(new GroupControlPrompt(plugin, sender))
                .withEscapeSequence("##")
                .withModality(false).buildConversation(conversable);
        conversation.begin();
    }
}

