package org.mvplugins.multiverse.inventories.commands.prompts;

import org.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.core.command.MVCommandIssuer;
import org.mvplugins.multiverse.core.locale.message.Message;
import org.mvplugins.multiverse.inventories.MultiverseInventories;
import org.mvplugins.multiverse.inventories.profile.group.WorldGroup;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.mvplugins.multiverse.inventories.util.MVInvi18n;

import java.util.HashSet;
import java.util.Set;

import static org.mvplugins.multiverse.core.locale.message.MessageReplacement.replace;

final class GroupWorldsPrompt extends InventoriesPrompt {

    protected final WorldGroup group;
    protected final Prompt nextPrompt;
    protected final boolean isCreating;
    protected final Set<String> worlds;

    public GroupWorldsPrompt(final MultiverseInventories plugin, final MVCommandIssuer issuer,
                             final WorldGroup group, final Prompt nextPrompt,
                             final boolean creatingGroup) {
        super(plugin, issuer);
        this.group = group;
        this.nextPrompt = nextPrompt;
        this.isCreating = creatingGroup;
        this.worlds = new HashSet<String>(group.getWorlds());
    }

    @NotNull
    @Override
    public Message getPromptMessage(@NotNull final ConversationContext conversationContext) {
        final StringBuilder builder = new StringBuilder();
        for (final String world : worlds) {
            if (builder.length() == 0) {
                builder.append(ChatColor.WHITE);
            } else {
                builder.append(ChatColor.GOLD).append(", ").append(ChatColor.WHITE);
            }
            builder.append(world);
        }
        return Message.of(MVInvi18n.GROUP_WORLDSPROMPT,
                        replace("{group}").with(group.getName()),
                        replace("{worlds}").with(builder.toString()));
    }

    @Override
    public Prompt acceptInput(@NotNull final ConversationContext conversationContext, final String input) {
        if (input.equals("@")) {
            if (worlds.isEmpty()) {
                issuer.sendInfo(MVInvi18n.GROUP_WORLDSEMPTY);
                return this;
            }
            group.removeAllWorlds(false);
            group.addWorlds(worlds, false);
            if (!isCreating) {
                worldGroupManager.updateGroup(group);
                issuer.sendInfo(MVInvi18n.GROUP_UPDATED);
                issuer.sendInfo(MVInvi18n.INFO_GROUP, replace("{group}").with(group.getName()));
                issuer.sendInfo(MVInvi18n.INFO_GROUP_INFO, replace("{worlds}").with(group.getWorlds()));
                issuer.sendInfo(MVInvi18n.INFO_GROUP_INFOSHARES, replace("{shares}").with(group.getShares()));
            }
            return nextPrompt;
        }

        boolean negative = false;
        World world = Bukkit.getWorld(input);
        if (world == null && input.startsWith("-") && input.length() > 1) {
            negative = true;
            world = Bukkit.getWorld(input.substring(1));
        }

        if (world == null) {
            issuer.sendError(MVInvi18n.ERROR_NOWORLD, replace("{world}").with(input));
            return this;
        }
        if (negative) {
            if (!worlds.contains(world.getName())) {
                issuer.sendError(MVInvi18n.REMOVEWORLD_WORLDNOTINGROUP,
                        replace("{world}").with(input),
                        replace("{group}").with(group.getName()));
                return this;
            }
            worlds.remove(world.getName());
            return this;
        }
        worlds.add(world.getName());
        return this;
    }
}
