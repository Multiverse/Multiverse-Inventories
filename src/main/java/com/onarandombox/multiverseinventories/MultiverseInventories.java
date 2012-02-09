package com.onarandombox.multiverseinventories;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVPlugin;
import com.onarandombox.MultiverseCore.commands.HelpCommand;
import com.onarandombox.multiverseinventories.command.AddSharesCommand;
import com.onarandombox.multiverseinventories.command.AddWorldCommand;
import com.onarandombox.multiverseinventories.command.ImportCommand;
import com.onarandombox.multiverseinventories.command.InfoCommand;
import com.onarandombox.multiverseinventories.command.ListCommand;
import com.onarandombox.multiverseinventories.command.ReloadCommand;
import com.onarandombox.multiverseinventories.command.RemoveSharesCommand;
import com.onarandombox.multiverseinventories.command.RemoveWorldCommand;
import com.onarandombox.multiverseinventories.config.CommentedMVIConfig;
import com.onarandombox.multiverseinventories.config.MVIConfig;
import com.onarandombox.multiverseinventories.data.FlatfileMVIData;
import com.onarandombox.multiverseinventories.data.MVIData;
import com.onarandombox.multiverseinventories.group.GroupingConflict;
import com.onarandombox.multiverseinventories.group.SimpleWorldGroupManager;
import com.onarandombox.multiverseinventories.group.WorldGroupManager;
import com.onarandombox.multiverseinventories.listener.MVICoreListener;
import com.onarandombox.multiverseinventories.listener.RespawnListener;
import com.onarandombox.multiverseinventories.listener.WorldChangeListener;
import com.onarandombox.multiverseinventories.listener.MVIServerListener;
import com.onarandombox.multiverseinventories.locale.Messager;
import com.onarandombox.multiverseinventories.locale.Messaging;
import com.onarandombox.multiverseinventories.locale.MultiverseMessage;
import com.onarandombox.multiverseinventories.locale.SimpleMessager;
import com.onarandombox.multiverseinventories.migration.ImportManager;
import com.onarandombox.multiverseinventories.permission.MVIPerms;
import com.onarandombox.multiverseinventories.profile.ProfileManager;
import com.onarandombox.multiverseinventories.profile.WeakProfileManager;
import com.onarandombox.multiverseinventories.util.MVIDebug;
import com.onarandombox.multiverseinventories.util.MVILog;
import com.pneumaticraft.commandhandler.multiverse.CommandHandler;
import me.drayshak.WorldInventories.WorldInventories;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import uk.co.tggl.pluckerpluck.multiinv.MultiInv;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

/**
 * Multiverse-Inventories plugin main class.
 */
public class MultiverseInventories extends JavaPlugin implements MVPlugin, Messaging {

    private final int requiresProtocol = 12;
    private final WorldChangeListener worldChangeListener = new WorldChangeListener(this);
    private final MVIServerListener serverListener = new MVIServerListener(this);
    private final MVICoreListener coreListener = new MVICoreListener(this);
    private final RespawnListener respawnListener = new RespawnListener(this);

    private Messager messager = new SimpleMessager(this);
    private WorldGroupManager worldGroupManager = null;
    private ProfileManager profileManager = null;
    private ImportManager importManager = new ImportManager(this);

    private CommandHandler commandHandler = null;
    private MultiverseCore core = null;
    private MVIConfig config = null;
    private MVIData data = null;

    private File serverFolder = new File(System.getProperty("user.dir"));

    /**
     * {@inheritDoc}
     */
    @Override
    public final void onDisable() {
        // Display disable message/version info
        MVILog.info("disabled.", true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void onEnable() {
        MVILog.init(this);
        MVIPerms.register(this);

        MultiverseCore mvCore;
        mvCore = (MultiverseCore) this.getServer().getPluginManager().getPlugin("Multiverse-Core");
        // Test if the Core was found, if not we'll disable this plugin.
        if (mvCore == null) {
            MVILog.severe("Multiverse-Core not found, disabling...");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }
        this.setCore(mvCore);

        if (this.getCore().getProtocolVersion() < this.getRequiredProtocol()) {
            MVILog.severe("Your Multiverse-Core is OUT OF DATE");
            MVILog.severe("This version of Profiles requires Protocol Level: " + this.getRequiredProtocol());
            MVILog.severe("Your of Core Protocol Level is: " + this.getCore().getProtocolVersion());
            MVILog.severe("Grab an updated copy at: ");
            MVILog.severe("http://bukkit.onarandombox.com/?dir=multiverse-core");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        MVIDebug.init(this);

        this.reloadConfig();

        try {
            this.getMessager().setLocale(new Locale(this.getSettings().getLocale()));
        } catch (IllegalArgumentException e) {
            MVILog.severe(e.getMessage());
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Initialize data class
        //this.getProfileManager().setWorldProfiles(this.getData().getWorldProfiles());

        this.getCore().incrementPluginCount();

        // Register Events
        this.registerEvents();

        // Register Commands
        this.registerCommands();

        // Hook plugins that can be imported from
        this.hookImportables();

        // Display enable message/version info
        MVILog.info("enabled.", true);
    }

    private void registerEvents() {
        final PluginManager pm = Bukkit.getPluginManager();
        // Event registering goes here
        pm.registerEvents(worldChangeListener, this);
        pm.registerEvents(serverListener, this);
        pm.registerEvents(coreListener, this);
        pm.registerEvents(respawnListener, this);
    }

    private void registerCommands() {
        this.commandHandler = this.getCore().getCommandHandler();
        this.getCommandHandler().registerCommand(new InfoCommand(this));
        this.getCommandHandler().registerCommand(new ImportCommand(this));
        this.getCommandHandler().registerCommand(new ListCommand(this));
        this.getCommandHandler().registerCommand(new ReloadCommand(this));
        this.getCommandHandler().registerCommand(new AddWorldCommand(this));
        this.getCommandHandler().registerCommand(new RemoveWorldCommand(this));
        this.getCommandHandler().registerCommand(new AddSharesCommand(this));
        this.getCommandHandler().registerCommand(new RemoveSharesCommand(this));
        for (com.pneumaticraft.commandhandler.multiverse.Command c : this.commandHandler.getAllCommands()) {
            if (c instanceof HelpCommand) {
                c.addKey("mvinv");
            }
        }
    }

    private void hookImportables() {
        final PluginManager pm = Bukkit.getPluginManager();
        Plugin plugin = pm.getPlugin("MultiInv");
        if (plugin != null) {
            this.getImportManager().hookMultiInv((MultiInv) plugin);
        }
        plugin = pm.getPlugin("WorldInventories");
        if (plugin != null) {
            this.getImportManager().hookWorldInventories((WorldInventories) plugin);
        }
    }

    /**
     * @return A class used for managing importing data from other similar plugins.
     */
    public ImportManager getImportManager() {
        return this.importManager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
        if (!this.isEnabled()) {
            sender.sendMessage("This plugin is Disabled!");
            return true;
        }
        ArrayList<String> allArgs = new ArrayList<String>(Arrays.asList(args));
        allArgs.add(0, command.getName());
        return this.getCommandHandler().locateAndRunCommand(sender, allArgs);
    }

    private CommandHandler getCommandHandler() {
        return this.commandHandler;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void log(Level level, String msg) {
        MVILog.log(level, msg, false);
        MVIDebug.log(level, msg);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MultiverseCore getCore() {
        return this.core;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCore(MultiverseCore core) {
        this.core = core;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getProtocolVersion() {
        return 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String dumpVersionInfo(String buffer) {
        buffer += this.getVersionInfo();
        return buffer;
    }

    /**
     * @return The pastebin version string.
     */
    public String getVersionInfo() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.logAndAddToPasteBinBuffer("Multiverse-Inventories Version: "
                + this.getDescription().getVersion()));
        builder.append(this.logAndAddToPasteBinBuffer("Debug Mode: " + this.getSettings().isDebugging()));
        builder.append(this.logAndAddToPasteBinBuffer("First Run: " + this.getSettings().isFirstRun()));
        builder.append(this.logAndAddToPasteBinBuffer("Groups: " + this.getGroupManager().getGroups().toString()));
        return builder.toString();
    }

    private String logAndAddToPasteBinBuffer(String string) {
        MVILog.info(string);
        return MVILog.getString(string + "\n", false);
    }

    /**
     * @return the MVIConfig object which contains settings for this plugin.
     */
    public MVIConfig getSettings() {
        if (this.config == null) {
            // Loads the configuration
            try {
                this.config = new CommentedMVIConfig(this);
                MVILog.debug("Loaded config file!");
            } catch (Exception e) {  // Catch errors loading the config file and exit out if found.
                MVILog.severe(this.getMessager().getMessage(MultiverseMessage.ERROR_CONFIG_LOAD));
                MVILog.severe(e.getMessage());
                Bukkit.getPluginManager().disablePlugin(this);
                return null;
            }
        }
        return this.config;
    }

    /**
     * Nulls the config object and reloads a new one, also resetting the world groups in memory.
     */
    public void reloadConfig() {
        this.config = null;
        this.worldGroupManager = null;
        this.profileManager = null;
        // Set debug mode from config
        MVILog.setDebugMode(this.getSettings().isDebugging());
        // Get world groups from config

        this.getGroupManager().setGroups(this.getSettings().getWorldGroups());
        // Create initial World Group for first run IF NO GROUPS EXIST
        if (this.getSettings().isFirstRun()) {
            MVILog.info("First run!");
            if (this.getGroupManager().getGroups().isEmpty()) {
                this.getGroupManager().createDefaultGroup();
            }
        }
        this.checkForGroupConflicts(null);
    }

    /**
     * @return the MVIData object which contains data for this plugin.
     */
    public MVIData getData() {
        if (this.data == null) {
            // Loads the data
            try {
                this.data = new FlatfileMVIData(this);
            } catch (IOException e) {  // Catch errors loading the language file and exit out if found.
                MVILog.severe(this.getMessager().getMessage(MultiverseMessage.ERROR_DATA_LOAD));
                MVILog.severe(e.getMessage());
                Bukkit.getPluginManager().disablePlugin(this);
                return null;
            }
        }
        return this.data;
    }

    /**
     * Runs a check for conflicts between groups and displays them to console and sender if not null.
     *
     * @param sender The sender to relay information to.  If null, info only displayed in console.
     */
    public void checkForGroupConflicts(CommandSender sender) {
        String message = this.getMessager().getMessage(MultiverseMessage.CONFLICT_CHECKING);
        if (sender != null) {
            this.getMessager().sendMessage(sender, message);
        }
        MVILog.info(message);
        List<GroupingConflict> conflicts = this.getGroupManager().checkGroups();
        for (GroupingConflict conflict : conflicts) {
            message = this.getMessager().getMessage(MultiverseMessage.CONFLICT_RESULTS,
                    conflict.getFirstGroup().getName(), conflict.getSecondGroup().getName(),
                    conflict.getConflictingShares().toString(), conflict.getWorldsString());
            if (sender != null) {
                this.getMessager().sendMessage(sender, message);
            }
            MVILog.info(message);
        }
        if (!conflicts.isEmpty()) {
            message = this.getMessager().getMessage(MultiverseMessage.CONFLICT_FOUND);
            if (sender != null) {
                this.getMessager().sendMessage(sender, message);
            }
            MVILog.info(message);
        } else {
            message = this.getMessager().getMessage(MultiverseMessage.CONFLICT_NOT_FOUND);
            if (sender != null) {
                this.getMessager().sendMessage(sender, message);
            }
            MVILog.info(message);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Messager getMessager() {
        return messager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setMessager(Messager messager) {
        if (messager == null)
            throw new IllegalArgumentException("The new messager can't be null!");

        this.messager = messager;
    }

    /**
     * @return The required protocol version of core.
     */
    public int getRequiredProtocol() {
        return this.requiresProtocol;
    }

    /**
     * @return The World Group manager for this plugin.
     */
    public WorldGroupManager getGroupManager() {
        if (this.worldGroupManager == null) {
            this.worldGroupManager = new SimpleWorldGroupManager(this);
        }
        return this.worldGroupManager;
    }

    /**
     * @return The Profile manager for this plugin.
     */
    public ProfileManager getProfileManager() {
        if (this.profileManager == null) {
            this.profileManager = new WeakProfileManager(this);
        }
        return this.profileManager;
    }

    /**
     * Gets the server's root-folder as {@link File}.
     *
     * @return The server's root-folder
     */
    public File getServerFolder() {
        return serverFolder;
    }

    /**
     * Sets this server's root-folder.
     *
     * @param newServerFolder The new server-root
     */
    public void setServerFolder(File newServerFolder) {
        if (!newServerFolder.isDirectory())
            throw new IllegalArgumentException("That's not a folder!");

        this.serverFolder = newServerFolder;
    }

    /**
     * @return A set of default shares (all false)
     */
    /*
    public Shares getDefaultShares() {
        return this.defaultShares;
    }
    */
}

