package com.onarandombox.multiverseprofiles;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVPlugin;
import com.onarandombox.multiverseprofiles.listener.ProfilesPlayerListener;
import com.onarandombox.multiverseprofiles.locale.LanguageImpl;
import com.onarandombox.multiverseprofiles.util.ProfilesDebug;
import com.onarandombox.multiverseprofiles.util.ProfilesLog;
import com.pneumaticraft.commandhandler.CommandHandler;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.logging.Level;

/**
 * @author dumptruckman
 */
public class MultiverseProfilesPlugin extends JavaPlugin implements MVPlugin {

    protected CommandHandler commandHandler;

    private final ProfilesPlayerListener playerListener = new ProfilesPlayerListener();

    private MultiverseProfiles manager = null;

    final public void onDisable() {
        // Save the plugin data
        this.getManager().getData().save(true);

        // Display disable message/version info
        ProfilesLog.info("disabled.", true);
    }

    final public void onEnable() {
        // Store the instance of this plugin
        this.setManager(new MultiverseProfiles(this));

        // Initialize logger
        ProfilesLog.load();

        MultiverseCore core;
        core = (MultiverseCore) this.getServer().getPluginManager().getPlugin("Multiverse-Core");
        // Test if the Core was found, if not we'll disable this plugin.
        if (core == null) {
            ProfilesLog.info("Multiverse-Core not found, will keep looking.");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }
        this.setCore(core);

        if (this.getManager().getCore().getProtocolVersion() < MultiverseProfiles.getRequiredProtocol()) {
            ProfilesLog.severe("Your Multiverse-Core is OUT OF DATE");
            ProfilesLog.severe("This version of NetherPortals requires Protocol Level: " + MultiverseProfiles.getRequiredProtocol());
            ProfilesLog.severe("Your of Core Protocol Level is: " + MultiverseProfiles.getCore().getProtocolVersion());
            ProfilesLog.severe("Grab an updated copy at: ");
            ProfilesLog.severe("http://bukkit.onarandombox.com/?dir=multiverse-core");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Initialize debug logger
        ProfilesDebug.load();

        this.getManager().getCore().incrementPluginCount();

        // Grab the PluginManager
        final PluginManager pm = this.getServer().getPluginManager();

        // Loads the configuration
        try {
            this.getManager().loadConfig();
        } catch (Exception e) {  // Catch errors loading the config file and exit out if found.
            ProfilesLog.severe("Encountered an error while loading the configuration file.  Disabling...");
            ProfilesLog.severe("Error Message: " + e.getMessage());
            pm.disablePlugin(this);
            return;
        }

        // Loads the language
        try {
            LanguageImpl.load();
        } catch (IOException e) {  // Catch errors loading the language file and exit out if found.
            ProfilesLog.severe("Encountered an error while loading the language file.  Disabling...");
            pm.disablePlugin(this);
            return;
        }

        // Loads the data
        try {
            this.getManager().loadData();
        } catch (IOException e) {  // Catch errors loading the language file and exit out if found.
            ProfilesLog.severe("Encountered an error while loading the data file.  Disabling...");
            pm.disablePlugin(this);
            return;
        }

        // Register Events
        this.registerEvents();

        // Display enable message/version info
        ProfilesLog.info("enabled.", true);
    }

    private void registerEvents() {
        final PluginManager pm = getServer().getPluginManager();
        // Event registering goes here
        pm.registerEvent(Event.Type.PLAYER_CHANGED_WORLD, playerListener, Event.Priority.Normal, this);
        //pm.registerEvent(Event.Type.PLAYER_LOGIN, playerListener, Event.Priority.Normal, this);
    }

    private void setManager(MultiverseProfiles manager) {
        this.manager = manager;
    }

    public MultiverseProfiles getManager() {
        return this.manager;
    }

    public void log(Level level, String msg) {
        ProfilesLog.log(level, msg);
        ProfilesDebug.log(level, msg);
    }

    public MultiverseCore getCore() {
        return this.getManager().getCore();
    }

    public void setCore(MultiverseCore core) {
        this.getManager().setCore(core);
    }

    public int getProtocolVersion() {
        return 1;
    }

    public String dumpVersionInfo(String buffer) {
        buffer += this.logAndAddToPasteBinBuffer("Multiverse-Inventories Version: " + this.getDescription().getVersion());
        buffer += this.logAndAddToPasteBinBuffer("Bukkit Version: " + this.getServer().getVersion());
        buffer += this.logAndAddToPasteBinBuffer("Special Code: FRN001");
        return buffer;
    }

    private String logAndAddToPasteBinBuffer(String string) {
        ProfilesLog.info(string);
        return ProfilesLog.getString(string + "\n", false);
    }
}
