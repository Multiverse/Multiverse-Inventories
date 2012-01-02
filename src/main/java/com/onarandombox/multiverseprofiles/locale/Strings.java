package com.onarandombox.multiverseprofiles.locale;

import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * @author dumptruckman, SwearWord
 */
public interface Strings {

    public String getString(Language lang, Object... args);

    public String formatString(String string, Object... args);

    /**
     * Gets a list of the messages for a given path.  Color codes will be
     * converted and any lines too long will be split into an extra element in
     * the list.  %n notated variables n the message will be replaced with the
     * optional arguments passed in.
     *
     * @param lang Path of the message in the language yaml file.
     * @param args Optional arguments to replace %n variable notations
     * @return A List of formatted Strings
     */
    public List<String> getStrings(Language lang, Object... args);

    public void bad(Language lang, CommandSender sender, Object... args);

    public void normal(Language lang, CommandSender sender, Object... args);

    public void good(Language lang, CommandSender sender, Object... args);

    public void info(Language lang, CommandSender sender, Object... args);

    /**
     * Sends a custom string to a player.
     *
     * @param player
     * @param message
     * @param args
     */
    public void sendMessage(CommandSender player, String message, Object... args);

    public void sendMessages(CommandSender player, List<String> messages);
}
