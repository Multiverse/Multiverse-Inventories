package com.onarandombox.multiverseinventories.commands_acf;

import com.onarandombox.acf.annotation.CommandAlias;
import com.onarandombox.acf.annotation.Description;
import com.onarandombox.acf.annotation.Subcommand;
import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.command.prompts.GroupControlPrompt;
import com.onarandombox.multiverseinventories.locale.Message;
import com.onarandombox.multiverseinventories.util.Perm;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.Conversable;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationFactory;
import org.jetbrains.annotations.NotNull;

@CommandAlias("mvinv")
public class GroupCommand extends InventoriesCommand {

    public GroupCommand(MultiverseInventories plugin) {
        super(plugin);
        this.addPermission(Perm.COMMAND_GROUP);
    }

    @Subcommand("group")
    @Description("Create, edit, or delete a world group.")
    public void onGroupCommand(@NotNull CommandSender sender) {
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
