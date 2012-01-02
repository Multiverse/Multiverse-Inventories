package com.onarandombox.multiverseprofiles.locale;

import org.bukkit.command.CommandSender;

import java.util.List;

public interface Messager extends MessageProvider {

    /**
     * Sends a message to the specified player with the generic ERROR prefix.
     *
     * @param message The message to send.
     * @param sender The entity to send the messages to.
     * @param args arguments for String.format().
     */
    public void bad(MultiverseMessage message, CommandSender sender, Object... args);

    /**
     * Sends a message to the specified player with NO special prefix.
     *
     * @param message The message to send.
     * @param sender The entity to send the messages to.
     * @param args arguments for String.format().
     */
    public void normal(MultiverseMessage message, CommandSender sender, Object... args);

    /**
     * Sends a message to the specified player with the generic SUCCESS prefix.
     *
     * @param message The message to send.
     * @param sender The entity to send the messages to.
     * @param args arguments for String.format().
     */
    public void good(MultiverseMessage message, CommandSender sender, Object... args);

    /**
     * Sends a message to the specified player with the generic INFO prefix.
     *
     * @param message The message to send.
     * @param sender The entity to send the messages to.
     * @param args arguments for String.format().
     */
    public void info(MultiverseMessage message, CommandSender sender, Object... args);

    /**
     * Sends a message to the specified player with the generic HELP prefix.
     *
     * @param message The message to send.
     * @param sender The entity to send the messages to.
     * @param args arguments for String.format().
     */
    public void help(MultiverseMessage message, CommandSender sender, Object... args);

    /**
     * Sends a message to a player that automatically takes words that are too long and puts them on a new line.
     *
     * @param player Player to send message to.
     * @param message Message to send.
     */
    public void sendMessage(CommandSender player, String message);

    /**
     * Sends a message to a player that automatically takes words that are too long and puts them on a new line.
     *
     * @param player Player to send message to.
     * @param messages Messages to send.
     */
    public void sendMessages(CommandSender player, List<String> messages);
}
