package com.onarandombox.multiverseinventories.locale;

import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * This interface describes a Messager which sends messages to CommandSenders.
 */
public interface Messager extends MessageProvider {

    /**
     * Sends a message to the specified player with the generic ERROR prefix.
     *
     * @param message The message to send.
     * @param sender  The entity to send the messages to.
     * @param args    arguments for String.format().
     */
    void bad(MultiverseMessage message, CommandSender sender, Object... args);

    /**
     * Sends a message to the specified player with NO special prefix.
     *
     * @param message The message to send.
     * @param sender  The entity to send the messages to.
     * @param args    arguments for String.format().
     */
    void normal(MultiverseMessage message, CommandSender sender, Object... args);

    /**
     * Sends a message to the specified player with the generic SUCCESS prefix.
     *
     * @param message The message to send.
     * @param sender  The entity to send the messages to.
     * @param args    arguments for String.format().
     */
    void good(MultiverseMessage message, CommandSender sender, Object... args);

    /**
     * Sends a message to the specified player with the generic INFO prefix.
     *
     * @param message The message to send.
     * @param sender  The entity to send the messages to.
     * @param args    arguments for String.format().
     */
    void info(MultiverseMessage message, CommandSender sender, Object... args);

    /**
     * Sends a message to the specified player with the generic HELP prefix.
     *
     * @param message The message to send.
     * @param sender  The entity to send the messages to.
     * @param args    arguments for String.format().
     */
    void help(MultiverseMessage message, CommandSender sender, Object... args);

    /**
     * Sends a message to a player that automatically takes words that are too long and puts them on a new line.
     *
     * @param player  Player to send message to.
     * @param message Message to send.
     */
    void sendMessage(CommandSender player, String message);

    /**
     * Sends a message to a player that automatically takes words that are too long and puts them on a new line.
     *
     * @param player   Player to send message to.
     * @param messages Messages to send.
     */
    void sendMessages(CommandSender player, List<String> messages);
}
