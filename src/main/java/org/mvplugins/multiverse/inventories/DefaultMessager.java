package org.mvplugins.multiverse.inventories;

import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.inventories.locale.MessageProvider;
import org.mvplugins.multiverse.inventories.locale.Messager;
import org.mvplugins.multiverse.inventories.locale.Message;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.mvplugins.multiverse.inventories.util.Font;

import java.util.List;

/**
 * Implementation of a Messager and MessageProvider using DefaultMessageProvider to implement the latter.
 */
@Service
final class DefaultMessager extends DefaultMessageProvider implements Messager, MessageProvider {

    @Inject
    public DefaultMessager(@NotNull MultiverseInventories plugin) {
        super(plugin);
    }

    private void send(Message message, String prefix, CommandSender sender, Object... args) {
        List<String> messages = this.getMessages(message, args);
        if (!messages.isEmpty()) {
            messages.set(0, prefix + " " + messages.get(0));
            sendMessages(sender, messages);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void bad(Message message, CommandSender sender, Object... args) {
        send(message, ChatColor.RED + this.getMessage(Message.GENERIC_ERROR), sender, args);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void normal(Message message, CommandSender sender, Object... args) {
        send(message, "", sender, args);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void good(Message message, CommandSender sender, Object... args) {
        send(message, ChatColor.GREEN + this.getMessage(Message.GENERIC_SUCCESS), sender, args);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void info(Message message, CommandSender sender, Object... args) {
        send(message, ChatColor.YELLOW + this.getMessage(Message.GENERIC_INFO), sender, args);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void help(Message message, CommandSender sender, Object... args) {
        send(message, ChatColor.GRAY + this.getMessage(Message.GENERIC_HELP), sender, args);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendMessage(CommandSender player, String message) {
        List<String> messages = Font.splitString(message);
        sendMessages(player, messages);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendMessages(CommandSender player, List<String> messages) {
        for (String s : messages) {
            player.sendMessage(s);
        }
    }
}

