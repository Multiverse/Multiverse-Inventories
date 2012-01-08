package com.onarandombox.multiverseinventories.locale;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class SimpleMessager extends SimpleMessageProvider implements Messager {

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
    public void bad(MultiverseMessage message, CommandSender sender, Object... args) {
        send(message, ChatColor.RED.toString() + this.getMessages(MultiverseMessage.GENERIC_ERROR), sender, args);
    }

    /**
     * {@inheritDoc}
     */
    public void normal(MultiverseMessage message, CommandSender sender, Object... args) {
        send(message, "", sender, args);
    }

    /**
     * {@inheritDoc}
     */
    public void good(MultiverseMessage message, CommandSender sender, Object... args) {
        send(message, ChatColor.GREEN.toString() + this.getMessages(MultiverseMessage.GENERIC_SUCCESS), sender, args);
    }

    /**
     * {@inheritDoc}
     */
    public void info(MultiverseMessage message, CommandSender sender, Object... args) {
        send(message, ChatColor.YELLOW.toString() + this.getMessages(MultiverseMessage.GENERIC_INFO), sender, args);
    }

    /**
     * {@inheritDoc}
     */
    public void help(MultiverseMessage message, CommandSender sender, Object... args) {
        send(message, ChatColor.GRAY.toString() + this.getMessages(MultiverseMessage.GENERIC_HELP), sender, args);
    }

    /**
     * {@inheritDoc}
     */
    public void sendMessage(CommandSender player, String message) {
        List<String> messages = Font.splitString(message);
        sendMessages(player, messages);
    }

    /**
     * {@inheritDoc}
     */
    public void sendMessages(CommandSender player, List<String> messages) {
        for (String s : messages) {
            player.sendMessage(s);
        }
    }
}
