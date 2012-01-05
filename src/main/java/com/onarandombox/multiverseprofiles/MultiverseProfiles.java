package com.onarandombox.multiverseprofiles;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVPlugin;
import com.onarandombox.multiverseprofiles.config.ProfilesConfig;
import com.onarandombox.multiverseprofiles.config.SimpleProfilesConfig;
import com.onarandombox.multiverseprofiles.data.ProfilesData;
import com.onarandombox.multiverseprofiles.data.SimpleProfilesData;
import com.onarandombox.multiverseprofiles.listener.ProfilesPlayerListener;
import com.onarandombox.multiverseprofiles.locale.Messager;
import com.onarandombox.multiverseprofiles.locale.Messaging;
import com.onarandombox.multiverseprofiles.locale.MultiverseMessage;
import com.onarandombox.multiverseprofiles.locale.SimpleMessager;
import com.onarandombox.multiverseprofiles.player.SimplePlayerProfile;
import com.onarandombox.multiverseprofiles.util.ProfilesDebug;
import com.onarandombox.multiverseprofiles.util.ProfilesLog;
import com.onarandombox.multiverseprofiles.world.SimpleWorldGroup;
import com.onarandombox.multiverseprofiles.world.SimpleWorldProfile;
import com.onarandombox.multiverseprofiles.world.WorldGroup;
import com.onarandombox.multiverseprofiles.world.WorldProfile;
import com.pneumaticraft.commandhandler.CommandHandler;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

/**
 * @author dumptruckman
 */
public class MultiverseProfiles extends JavaPlugin implements MVPlugin, Messaging {

    protected CommandHandler commandHandler;
    private final int requiresProtocol = 10;
    private MultiverseCore core = null;

    private final ProfilesPlayerListener playerListener = new ProfilesPlayerListener(this);

    //private Strings language = null;
    private ProfilesConfig config = null;
    private ProfilesData data = null;
    private ProfilesLog log = null;
    private ProfilesDebug debug = null;

    private Messager messager = new SimpleMessager(this);

    private HashMap<World, WorldProfile> worldProfiles = new HashMap<World, WorldProfile>();
    private HashMap<World, List<WorldGroup>> worldGroups = new HashMap<World, List<WorldGroup>>();

    static {
        ConfigurationSerialization.registerClass(SimplePlayerProfile.class);
        ConfigurationSerialization.registerClass(SimpleWorldProfile.class);
        ConfigurationSerialization.registerClass(SimpleWorldGroup.class);
    }
    
    final public void onDisable() {
        // Save the plugin data
        this.getData().save(true);

        // Display disable message/version info
        ProfilesLog.info("disabled.", true);
    }

    final public void onEnable() {
        ProfilesLog.init(this);

        MultiverseCore core;
        core = (MultiverseCore) this.getServer().getPluginManager().getPlugin("Multiverse-Core");
        // Test if the Core was found, if not we'll disable this plugin.
        if (core == null) {
            ProfilesLog.info("Multiverse-Core not found, will keep looking.");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }
        this.setCore(core);

        if (this.getCore().getProtocolVersion() < this.getRequiredProtocol()) {
            ProfilesLog.severe("Your Multiverse-Core is OUT OF DATE");
            ProfilesLog.severe("This version of Profiles requires Protocol Level: " + this.getRequiredProtocol());
            ProfilesLog.severe("Your of Core Protocol Level is: " + this.getCore().getProtocolVersion());
            ProfilesLog.severe("Grab an updated copy at: ");
            ProfilesLog.severe("http://bukkit.onarandombox.com/?dir=multiverse-core");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        try {
            this.getMessager().setLocale(new Locale(this.getConf().getLocale()));
        } catch (IllegalArgumentException e) {
            ProfilesLog.severe(e.getMessage());
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        ProfilesDebug.init(this);

        this.getCore().incrementPluginCount();

        // Grab the PluginManager
        final PluginManager pm = this.getServer().getPluginManager();

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

    public void log(Level level, String msg) {
        ProfilesLog.log(level, msg);
        ProfilesDebug.log(level, msg);
    }

    public MultiverseCore getCore() {
        return this.core;
    }

    public void setCore(MultiverseCore core) {
        this.core = core;
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
    
    public ProfilesConfig getConf() {
        if (this.config == null) {
            // Loads the configuration
            try {
                this.config = new SimpleProfilesConfig(this);
            } catch (Exception e) {  // Catch errors loading the config file and exit out if found.
                ProfilesLog.severe(this.getMessager().getMessage(MultiverseMessage.ERROR_CONFIG_LOAD));
                ProfilesLog.severe(e.getMessage());
                Bukkit.getPluginManager().disablePlugin(this);
                return null;
            }
        }
        return this.config;
    }

    public ProfilesData getData() {
        if (this.data == null) {
            // Loads the data
            try {
                this.data = new SimpleProfilesData(this);
            } catch (IOException e) {  // Catch errors loading the language file and exit out if found.
                ProfilesLog.severe(this.getMessager().getMessage(MultiverseMessage.ERROR_DATA_LOAD));
                ProfilesLog.severe(e.getMessage());
                Bukkit.getPluginManager().disablePlugin(this);
                return null;
            }
        }
        return this.data;
    }

    /**
     * {@inheritDoc}
     */
    public Messager getMessager() {
        return messager;
    }

    /**
     * {@inheritDoc}
     */
    public void setMessager(Messager messager) {
        if (messager == null)
            throw new IllegalArgumentException("The new messager can't be null!");

        this.messager = messager;
    }
    
    public int getRequiredProtocol() {
        return this.requiresProtocol;
    }

    public void addWorldProfile(WorldProfile worldProfile) {
        this.worldProfiles.put(worldProfile.getWorld(), worldProfile);
    }

    public WorldProfile getWorldProfile(World world) {
        return this.worldProfiles.get(world);
    }

    public HashMap<World, List<WorldGroup>> getWorldGroups() {
        return this.worldGroups;
    }
}
