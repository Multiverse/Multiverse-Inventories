package com.onarandombox.multiverseinventories.command.prompts;

import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.WorldGroup;
import com.onarandombox.multiverseinventories.locale.Message;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;

import java.util.HashSet;
import java.util.Set;

class GroupWorldsPrompt extends InventoriesPrompt {

    protected final WorldGroup group;
    protected final Prompt nextPrompt;
    protected final boolean isCreating;
    protected final Set<String> worlds;

    public GroupWorldsPrompt(final MultiverseInventories plugin, final CommandSender sender,
                             final WorldGroup group, final Prompt nextPrompt,
                             final boolean creatingGroup) {
        super(plugin, sender);
        this.group = group;
        this.nextPrompt = nextPrompt;
        this.isCreating = creatingGroup;
        this.worlds = new HashSet<String>(group.getWorlds());
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
        return messager.getMessage(Message.GROUP_WORLDS_PROMPT, group.getName(), builder.toString());
    }

    @Override
    public Prompt acceptInput(final ConversationContext conversationContext, final String s) {
        if (s.equals("@")) {
            if (worlds.isEmpty()) {
                messager.normal(Message.GROUP_WORLDS_EMPTY, sender);
                return this;
            }
            group.getWorlds().clear();
            group.getWorlds().addAll(worlds);
            if (!isCreating) {
                plugin.getGroupManager().updateGroup(group);
                messager.normal(Message.GROUP_UPDATED, sender);
                messager.normal(Message.INFO_GROUP, sender, group.getName());
                messager.normal(Message.INFO_GROUPS_INFO, sender, group.getWorlds(), group.getShares());
            }
            return nextPrompt;
        }

        boolean negative = false;
        World world = Bukkit.getWorld(s);
        if (world == null && s.startsWith("-") && s.length() > 1) {
            negative = true;
            world = Bukkit.getWorld(s.substring(1));
        }

        if (world == null) {
            messager.normal(Message.ERROR_NO_WORLD, sender, s);
            return this;
        }
        if (negative) {
            if (!worlds.contains(world.getName())) {
                messager.normal(Message.WORLD_NOT_IN_GROUP, sender, world.getName(), group.getName());
                return this;
            }
            worlds.remove(world.getName());
            return this;
        }
        worlds.add(world.getName());
        return this;
    }
}
