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
import org.mvplugins.multiverse.inventories.util.GroupWorldNameValidator;
import org.mvplugins.multiverse.inventories.util.MVInvi18n;

import java.util.HashSet;
import java.util.Set;

import static org.mvplugins.multiverse.core.locale.message.MessageReplacement.replace;

final class GroupWorldsPrompt extends InventoriesPrompt {

    protected final WorldGroup group;
    protected final Prompt nextPrompt;
    protected final boolean isCreating;
    protected final Set<String> worlds;
    private final GroupWorldNameValidator groupWorldNameValidator;

    public GroupWorldsPrompt(final MultiverseInventories plugin, final MVCommandIssuer issuer,
                             final WorldGroup group, final Prompt nextPrompt,
                             final boolean creatingGroup) {
        super(plugin, issuer);
        this.group = group;
        this.nextPrompt = nextPrompt;
        this.isCreating = creatingGroup;
        this.worlds = new HashSet<>(group.getConfigWorlds());
        this.groupWorldNameValidator = plugin.getServiceLocator().getService(GroupWorldNameValidator.class);
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
                issuer.sendInfo(MVInvi18n.INFO_GROUP_INFO, replace("{worlds}").with(group.getConfigWorlds()));
                issuer.sendInfo(MVInvi18n.INFO_GROUP_INFOSHARES, replace("{shares}").with(group.getShares()));
            }
            return nextPrompt;
        }

        boolean negative = input.startsWith("-");
        String worldName = negative ? input.substring(1) : input;
        if (!groupWorldNameValidator.validateWorldName(worldName)) {
            issuer.sendError(MVInvi18n.ERROR_NOWORLD, replace("{world}").with(input));
            return this;
        }

        if (negative) {
            if (!worlds.contains(worldName)) {
                issuer.sendError(MVInvi18n.REMOVEWORLD_WORLDNOTINGROUP,
                        replace("{world}").with(input),
                        replace("{group}").with(group.getName()));
                return this;
            }
            worlds.remove(worldName);
            return this;
        }

        worlds.add(worldName);
        return this;
    }
}
