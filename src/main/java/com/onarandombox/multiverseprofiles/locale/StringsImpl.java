package com.onarandombox.multiverseprofiles.locale;

import com.onarandombox.multiverseprofiles.util.ProfilesLog;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author dumptruckman, SwearWord
 */
public class StringsImpl implements Strings {

    private FileConfiguration language = null;
    
    public StringsImpl(JavaPlugin plugin, String languageFileName) throws IOException {
        // Make the data folders
        plugin.getDataFolder().mkdirs();

        // Check if the language file exists.  If not, create it.
        File languageFile = new File(plugin.getDataFolder(), languageFileName);
        this.initialize(languageFile);
    }

    public StringsImpl(File languageFile) throws IOException {
        this.initialize(languageFile);
    }
    
    private void initialize(File languageFile) throws IOException {
        if (!languageFile.exists()) {
            throw new IOException("Language file: " + languageFile.toString() + " is non-existent!");
        }

        // Load the language file into memory
        this.language = YamlConfiguration.loadConfiguration(languageFile);
    }

    private void send(Language lang, String prefix, CommandSender sender, Object... args) {
        List<String> messages = getStrings(lang, args);
        for (int i = 0; i < messages.size(); i++) {
            if (i == 0) {
                sender.sendMessage(prefix + " " + messages.get(i));
            } else {
                sender.sendMessage(messages.get(i));
            }
        }
    }

    public String formatString(String string, Object... args) {
        // Replaces & with the Section character
        string = string.replaceAll("&", Character.toString((char) 167));
        // If there are arguments, %n notations in the message will be
        // replaced
        if (args != null) {
            for (int j = 0; j < args.length; j++) {
                string = string.replace("%" + (j + 1), args[j].toString());
            }
        }
        return string;
    }

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
    public List<String> getStrings(Language lang, Object... args) {
        // Gets the messages for the path submitted
        List list = this.language.getList(lang.getPath());

        List<String> message = new ArrayList<String>();
        if (list == null) {
            ProfilesLog.warning("Missing language for: " + lang.getPath());
            return message;
        }
        // Parse each item in list
        for (int i = 0; i < list.size(); i++) {
            String temp = formatString(list.get(i).toString(), args);

            // Pass the line into the line breaker
            List<String> lines = Font.splitString(temp);
            // Add the broken up lines into the final message list to return
            for (int j = 0; j < lines.size(); j++) {
                message.add(lines.get(j));
            }
        }
        return message;
    }

    public String getString(Language lang, Object... args) {
        List list = this.language.getList(lang.getPath());
        if (list == null) {
            ProfilesLog.warning("Missing language for: " + lang.getPath());
            return "";
        }
        if (list.isEmpty()) return "";
        return (formatString(list.get(0).toString(), args));
    }

    public void bad(Language lang, CommandSender sender, Object... args) {
        send(lang, ChatColor.RED.toString() + getString(Language.ERROR, args), sender, args);
    }

    public void normal(Language lang, CommandSender sender, Object... args) {
        send(lang, "", sender, args);
    }

    public void good(Language lang, CommandSender sender, Object... args) {
        send(lang, ChatColor.GREEN.toString() + getString(Language.SUCCESS, args), sender, args);
    }

    public void info(Language lang, CommandSender sender, Object... args) {
        send(lang, ChatColor.YELLOW.toString() + getString(Language.INFO, args), sender, args);
    }

    /**
     * Sends a custom string to a player.
     *
     * @param player
     * @param message
     * @param args
     */
    public void sendMessage(CommandSender player, String message, Object... args) {
        List<String> messages = Font.splitString(formatString(message, args));
        sendMessages(player, messages);
    }

    public void sendMessages(CommandSender player, List<String> messages) {
        for (String s : messages) {
            player.sendMessage(s);
        }
    }
}