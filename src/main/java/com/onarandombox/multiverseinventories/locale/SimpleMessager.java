package com.onarandombox.multiverseinventories.locale;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

/**
 * Implementation of a Messager and MessageProvider using SimpleMessageProvider to implement the latter.
 */
public class SimpleMessager extends SimpleMessageProvider implements Messager, MessageProvider {

    public SimpleMessager(JavaPlugin plugin) {
        super(plugin);
    }

    private void send(MultiverseMessage message, String prefix, CommandSender sender, Object... args) {
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
    public void bad(MultiverseMessage message, CommandSender sender, Object... args) {
        send(message, ChatColor.RED.toString() + this.getMessage(MultiverseMessage.GENERIC_ERROR), sender, args);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void normal(MultiverseMessage message, CommandSender sender, Object... args) {
        send(message, "", sender, args);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void good(MultiverseMessage message, CommandSender sender, Object... args) {
        send(message, ChatColor.GREEN.toString() + this.getMessage(MultiverseMessage.GENERIC_SUCCESS), sender, args);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void info(MultiverseMessage message, CommandSender sender, Object... args) {
        send(message, ChatColor.YELLOW.toString() + this.getMessage(MultiverseMessage.GENERIC_INFO), sender, args);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void help(MultiverseMessage message, CommandSender sender, Object... args) {
        send(message, ChatColor.GRAY.toString() + this.getMessage(MultiverseMessage.GENERIC_HELP), sender, args);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendMessage(CommandSender player, String message) {
        List<String> messages = com.onarandombox.multiverseinventories.util.Font.splitString(message);
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

