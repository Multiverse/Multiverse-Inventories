package com.onarandombox.multiverseinventories;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVPlugin;
import com.onarandombox.multiverseinventories.config.MVIConfigImpl;
import com.onarandombox.multiverseinventories.data.MVIDataImpl;
import com.onarandombox.multiverseinventories.listener.MVIPlayerListener;
import com.onarandombox.multiverseinventories.locale.Language;
import com.onarandombox.multiverseinventories.util.MVIDebug;
import com.onarandombox.multiverseinventories.util.MVILog;
import com.pneumaticraft.commandhandler.CommandHandler;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.logging.Level;

/**
 * @author dumptruckman
 */
public class MultiverseInventories extends JavaPlugin implements MVPlugin {

    protected CommandHandler commandHandler;

    private final MVIPlayerListener playerListener = new MVIPlayerListener();

    final public void onDisable() {
        // Save the plugin data
        MVIManager.getData().save(true);

        // Clear static references
        MVIManager.wipeStaticInstances();

        // Display disable message/version info
        MVILog.info("disabled.", true);
    }

    final public void onEnable() {
        // Store the instance of this plugin
        MVIManager.setPluginInstance(this);

        // Initialize logger
        MVILog.load();

        MultiverseCore core;
        core = (MultiverseCore) getServer().getPluginManager().getPlugin("Multiverse-Core");
        // Test if the Core was found, if not we'll disable this plugin.
        if (core == null) {
            MVILog.info("Multiverse-Core not found, will keep looking.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        this.setCore(core);

        if (MVIManager.getCore().getProtocolVersion() < MVIManager.getRequiredProtocol()) {
            MVILog.severe("Your Multiverse-Core is OUT OF DATE");
            MVILog.severe("This version of NetherPortals requires Protocol Level: " + MVIManager.getRequiredProtocol());
            MVILog.severe("Your of Core Protocol Level is: " + MVIManager.getCore().getProtocolVersion());
            MVILog.severe("Grab an updated copy at: ");
            MVILog.severe("http://bukkit.onarandombox.com/?dir=multiverse-core");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Initialize debug logger
        MVIDebug.load();

        MVIManager.getCore().incrementPluginCount();

        // Grab the PluginManager
        final PluginManager pm = getServer().getPluginManager();

        // Loads the configuration
        try {
            MVIManager.loadConfig();
        } catch (Exception e) {  // Catch errors loading the config file and exit out if found.
            MVILog.severe("Encountered an error while loading the configuration file.  Disabling...");
            MVILog.severe("Error Message: " + e.getMessage());
            pm.disablePlugin(this);
            return;
        }

        // Loads the language
        try {
            Language.load();
        } catch (IOException e) {  // Catch errors loading the language file and exit out if found.
            MVILog.severe("Encountered an error while loading the language file.  Disabling...");
            pm.disablePlugin(this);
            return;
        }

        // Loads the data
        try {
            MVIManager.loadData();
        } catch (IOException e) {  // Catch errors loading the language file and exit out if found.
            MVILog.severe("Encountered an error while loading the data file.  Disabling...");
            pm.disablePlugin(this);
            return;
        }

        // Register Events
        registerEvents();

        // Display enable message/version info
        MVILog.info("enabled.", true);
    }

    public void registerEvents() {
        final PluginManager pm = getServer().getPluginManager();
        // Event registering goes here
        pm.registerEvent(Event.Type.PLAYER_CHANGED_WORLD, playerListener, Event.Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_LOGIN, playerListener, Event.Priority.Normal, this);
    }

    public void log(Level level, String msg) {
        MVILog.log(level, msg);
        MVIDebug.log(level, msg);
    }

    public MultiverseCore getCore() {
        return MVIManager.getCore();
    }

    public void setCore(MultiverseCore core) {
        MVIManager.setCore(core);
    }

    public int getProtocolVersion() {
        return 1;
    }

    public String dumpVersionInfo(String buffer) {
        buffer += logAndAddToPasteBinBuffer("Multiverse-Inventories Version: " + this.getDescription().getVersion());
        buffer += logAndAddToPasteBinBuffer("Bukkit Version: " + this.getServer().getVersion());
        buffer += logAndAddToPasteBinBuffer("Special Code: FRN001");
        return buffer;
    }

    private String logAndAddToPasteBinBuffer(String string) {
        MVILog.info(string);
        return MVILog.getString(string + "\n", false);
    }
}
