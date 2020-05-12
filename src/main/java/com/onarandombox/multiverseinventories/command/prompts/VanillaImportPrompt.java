package com.onarandombox.multiverseinventories.command.prompts;

import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.WorldGroup;
import com.onarandombox.multiverseinventories.locale.Message;
import com.onarandombox.multiverseinventories.migration.MigrationException;
import com.onarandombox.multiverseinventories.migration.vanilla.PlayerDataImporter;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class VanillaImportPrompt extends InventoriesPrompt {
    protected final WorldGroup group;
    protected final Prompt nextPrompt;
    protected final Set<String> worlds;

    public VanillaImportPrompt(final MultiverseInventories plugin, final CommandSender sender,
                               final WorldGroup group, final Prompt nextPrompt) {
        super(plugin, sender);
        this.group = group;
        this.nextPrompt = nextPrompt;
        this.worlds = new HashSet<String>();

        for (MultiverseWorld world: plugin.getCore().getMVWorldManager().getMVWorlds()) {
            worlds.add(world.getName());
        }

        // TODO: decide which for loop to use (this, or the above)
        //for (World world: plugin.getServer().getWorlds()) {
        //    worlds.add(world.getName());
        //} // based off GroupWorldsPrompt, we should be using this?
    }

    @Override
    public String getPromptText(final ConversationContext conversationContext) {
        final StringBuilder builder = new StringBuilder();
        for (final String world : worlds) {
            if (builder.length() == 0) {
                builder.append(ChatColor.WHITE);
            } else {
                builder.append(ChatColor.GOLD).append(", ").append(ChatColor.WHITE);
            }
            builder.append(world);
        }
        return messager.getMessage(Message.VANILLA_IMPORT_PROMPT, builder.toString());
    }

    @Override
    public @Nullable Prompt acceptInput(@NotNull ConversationContext conversationContext, @Nullable String s) {
        boolean found = false;

        for (String world: worlds) {
            if (s.equals(world)) {
                found = true;
                break;
            }
        }

        if (found) {
            try {
                new PlayerDataImporter(plugin, s, group).importData();
                messager.normal(Message.VANILLA_IMPORT_COMPLETE, sender);
                return nextPrompt;
            } catch (MigrationException e) {
                messager.normal(Message.GENERIC_SORRY, sender);
                messager.normal(Message.ERROR_PLAYERDATA_IMPORT, sender);
                plugin.getGroupManager().removeGroup(group);
                e.getCauseException().printStackTrace();
                return Prompt.END_OF_CONVERSATION;
            }
        } else {
            messager.normal(Message.ERROR_NO_WORLD, sender, s);
        }

        return this;
    }
}
