package com.onarandombox.multiverseinventories.command;

import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.api.profile.WorldGroupProfile;
import com.onarandombox.multiverseinventories.api.share.Sharables;
import com.onarandombox.multiverseinventories.api.share.Shares;
import com.onarandombox.multiverseinventories.locale.Message;
import com.onarandombox.multiverseinventories.util.Perm;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.Conversable;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.Prompt;

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
                .withFirstPrompt(new GroupControlPrompt(sender))
                .withEscapeSequence("##")
                .withModality(false).buildConversation(conversable);
        conversation.begin();
    }

    private class GroupControlPrompt implements Prompt {
        private CommandSender sender;

        public GroupControlPrompt(CommandSender sender) {
            this.sender = sender;
        }

        @Override
        public String getPromptText(ConversationContext conversationContext) {
            return messager.getMessage(Message.GROUP_CONTROL_PROMPT);
        }

        @Override
        public boolean blocksForInput(ConversationContext conversationContext) {
            return true;
        }

        @Override
        public Prompt acceptInput(ConversationContext conversationContext, String s) {
            if (s.equalsIgnoreCase("delete")) {
                return new GroupDeletePrompt(sender);
            } else if (s.equalsIgnoreCase("create")) {
                return new GroupCreatePrompt(sender);
            } else {
                messager.normal(Message.INVALID_OPTION, sender);
                return Prompt.END_OF_CONVERSATION;
            }
        }
    }

    private class GroupCreatePrompt implements Prompt {
        private CommandSender sender;

        public GroupCreatePrompt(CommandSender sender) {
            this.sender = sender;
        }

        @Override
        public String getPromptText(ConversationContext conversationContext) {
            return messager.getMessage(Message.GROUP_CREATE_PROMPT);
        }

        @Override
        public boolean blocksForInput(ConversationContext conversationContext) {
            return true;
        }

        @Override
        public Prompt acceptInput(ConversationContext conversationContext, String s) {
            WorldGroupProfile group = plugin.getGroupManager().getGroup(s);
            if (group == null) {
                if (s.isEmpty() || !s.matches("^[a-zA-Z0-9][a-zA-Z0-9_]*$")) {
                    messager.normal(Message.GROUP_INVALID_NAME, sender);
                    return this;
                }
                return new GroupSpecifyWorldsPrompt(sender, plugin.getGroupManager().newEmptyGroup(s));
            } else {
                messager.normal(Message.GROUP_EXISTS, sender, s);
            }
            return Prompt.END_OF_CONVERSATION;
        }
    }

    private class GroupSpecifyWorldsPrompt implements Prompt {
        private CommandSender sender;
        private WorldGroupProfile worldGroup;

        public GroupSpecifyWorldsPrompt(CommandSender sender, WorldGroupProfile worldGroup) {
            this.sender = sender;
            this.worldGroup = worldGroup;
        }

        @Override
        public String getPromptText(ConversationContext conversationContext) {
            return messager.getMessage(Message.GROUP_SPECIFY_WORLDS_PROMPT, worldGroup.getName());
        }

        @Override
        public boolean blocksForInput(ConversationContext conversationContext) {
            return true;
        }

        @Override
        public Prompt acceptInput(ConversationContext conversationContext, String s) {
            if (s.equals("@")) {
                return new GroupSpecifySharesPrompt(sender, worldGroup);
            }
            World world = Bukkit.getWorld(s);
            if (world == null) {
                messager.normal(Message.ERROR_NO_WORLD, sender, s);
            } else {
                worldGroup.addWorld(world);
                messager.normal(Message.WORLD_ADDED, sender, s, worldGroup.getName());
            }
            return this;
        }
    }

    private class GroupSpecifySharesPrompt implements Prompt {
        private CommandSender sender;
        private WorldGroupProfile worldGroup;

        public GroupSpecifySharesPrompt(CommandSender sender, WorldGroupProfile worldGroup) {
            this.sender = sender;
            this.worldGroup = worldGroup;
        }

        @Override
        public String getPromptText(ConversationContext conversationContext) {
            return messager.getMessage(Message.GROUP_SPECIFY_SHARES_PROMPT, worldGroup.getName());
        }

        @Override
        public boolean blocksForInput(ConversationContext conversationContext) {
            return true;
        }

        @Override
        public Prompt acceptInput(ConversationContext conversationContext, String s) {
            if (s.equals("@")) {
                if (worldGroup.getWorlds().isEmpty()) {
                    messager.normal(Message.GROUP_NO_WORLDS, sender);
                } else {
                    plugin.getGroupManager().addGroup(worldGroup, true);
                    messager.normal(Message.GROUP_CREATED, sender);
                    messager.normal(Message.INFO_GROUP, sender, worldGroup.getName());
                    messager.normal(Message.INFO_GROUP_INFO, sender, worldGroup.getWorlds(), worldGroup.getShares(), worldGroup.getNegativeShares());
                    plugin.getGroupManager().checkForConflicts(sender);
                }
                return Prompt.END_OF_CONVERSATION;
            }
            boolean negative = false;
            Shares shares;
            if (s.startsWith("-") && s.length() > 1) {
                negative = true;
                shares = Sharables.lookup(s.toLowerCase().substring(1));
            } else {
                shares = Sharables.lookup(s.toLowerCase());
            }

            if (shares == null) {
                messager.normal(Message.ERROR_NO_SHARES_SPECIFIED, sender);
            } else {
                if (!negative) {
                    worldGroup.getShares().addAll(shares);
                } else {
                    worldGroup.getNegativeShares().addAll(shares);
                }
                messager.normal(Message.NOW_SHARING, sender, worldGroup.getName(), worldGroup.getShares(), worldGroup.getNegativeShares());
            }
            return this;
        }
    }

    private class GroupDeletePrompt implements Prompt {
        private CommandSender sender;

        public GroupDeletePrompt(CommandSender sender) {
            this.sender = sender;
        }

        @Override
        public String getPromptText(ConversationContext conversationContext) {
            return messager.getMessage(Message.GROUP_DELETE_PROMPT);
        }

        @Override
        public boolean blocksForInput(ConversationContext conversationContext) {
            return true;
        }

        @Override
        public Prompt acceptInput(ConversationContext conversationContext, String s) {
            WorldGroupProfile group = plugin.getGroupManager().getGroup(s);
            if (group == null) {
                messager.normal(Message.ERROR_NO_GROUP, sender, s);
            } else {
                plugin.getGroupManager().removeGroup(group);
                messager.normal(Message.GROUP_REMOVED, sender, s);
            }
            return Prompt.END_OF_CONVERSATION;
        }
    }
}

