package com.onarandombox.multiverseinventories;

import com.dumptruckman.minecraft.util.Logging;
import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVPlugin;
import com.onarandombox.MultiverseCore.commands.HelpCommand;
import com.onarandombox.commandhandler.CommandHandler;
import com.onarandombox.multiverseinventories.profile.ProfileDataSource;
import com.onarandombox.multiverseinventories.profile.WorldGroupManager;
import com.onarandombox.multiverseinventories.profile.container.ContainerType;
import com.onarandombox.multiverseinventories.profile.container.ProfileContainerStore;
import com.onarandombox.multiverseinventories.share.Sharables;
import com.onarandombox.multiverseinventories.command.AddSharesCommand;
import com.onarandombox.multiverseinventories.command.AddWorldCommand;
import com.onarandombox.multiverseinventories.command.CreateGroupCommand;
import com.onarandombox.multiverseinventories.command.DeleteGroupCommand;
import com.onarandombox.multiverseinventories.command.GroupCommand;
import com.onarandombox.multiverseinventories.command.ImportCommand;
import com.onarandombox.multiverseinventories.command.InfoCommand;
import com.onarandombox.multiverseinventories.command.ListCommand;
import com.onarandombox.multiverseinventories.command.MigrateCommand;
import com.onarandombox.multiverseinventories.command.ReloadCommand;
import com.onarandombox.multiverseinventories.command.RemoveSharesCommand;
import com.onarandombox.multiverseinventories.command.RemoveWorldCommand;
import com.onarandombox.multiverseinventories.command.SpawnCommand;
import com.onarandombox.multiverseinventories.command.ToggleCommand;
import com.onarandombox.multiverseinventories.locale.Message;
import com.onarandombox.multiverseinventories.locale.Messager;
import com.onarandombox.multiverseinventories.locale.Messaging;
import com.onarandombox.multiverseinventories.migration.ImportManager;
import com.onarandombox.multiverseinventories.util.Perm;
import me.drayshak.WorldInventories.WorldInventories;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import uk.co.tggl.pluckerpluck.multiinv.MultiInv;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.logging.Level;

/**
 * Multiverse-Inventories plugin main class.
 */
public class MultiverseInventories extends JavaPlugin implements MVPlugin, Messaging {

    private static MultiverseInventories inventoriesPlugin;

    public static MultiverseInventories getPlugin() {
        return inventoriesPlugin;
    }

    private final int requiresProtocol = 22;
    private final InventoriesListener inventoriesListener = new InventoriesListener(this);
    private final AdventureListener adventureListener = new AdventureListener(this);

    private Messager messager = new DefaultMessager(this);
    private WorldGroupManager worldGroupManager = null;
    private ProfileContainerStore worldProfileContainerStore = null;
    private ProfileContainerStore groupProfileContainerStore = null;
    private ImportManager importManager = new ImportManager(this);

    private CommandHandler commandHandler = null;
    private MultiverseCore core = null;
    private InventoriesConfig config = null;
    private FlatFileProfileDataSource data = null;

    private InventoriesDupingPatch dupingPatch;

    private File serverFolder = new File(System.getProperty("user.dir"));

    {
        inventoriesPlugin = this;
    }

    public MultiverseInventories() {
        super();
    }

    /**
     * This is for unit testing.
     * @param loader The PluginLoader to use.
     * @param description The Description file to use.
     * @param dataFolder The folder that other datafiles can be found in.
     * @param file The location of the plugin.
     */
    public MultiverseInventories(JavaPluginLoader loader, PluginDescriptionFile description, File dataFolder, File file) {
        super(loader, description, dataFolder, file);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDisable() {
        for (final Player player : getServer().getOnlinePlayers()) {
            final String world = player.getWorld().getName();
            //getData().updateLastWorld(player.getName(), world);
            if (getMVIConfig().usingLoggingSaveLoad()) {
                ShareHandlingUpdater.updateProfile(this, player, new DefaultPersistingProfile(Sharables.allOf(),
                        getWorldProfileContainerStore().getContainer(world).getPlayerData(player)));
                getData().setLoadOnLogin(player.getName(), true);
            }
        }

        this.dupingPatch.disable();

        Logging.shutdown();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onEnable() {
        Logging.init(this);
        Perm.register(this);

        MultiverseCore mvCore;
        mvCore = (MultiverseCore) this.getServer().getPluginManager().getPlugin("Multiverse-Core");
        // Test if the Core was found, if not we'll disable this plugin.
        if (mvCore == null) {
            Logging.severe("Multiverse-Core not found, disabling...");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }
        this.setCore(mvCore);

        if (this.getCore().getProtocolVersion() < this.getRequiredProtocol()) {
            Logging.severe("Your Multiverse-Core is OUT OF DATE");
            Logging.severe("This version of Multiverse-Inventories requires Protocol Level: " + this.getRequiredProtocol());
            Logging.severe("Your of Core Protocol Level is: " + this.getCore().getProtocolVersion());
            Logging.severe("Grab an updated copy at: ");
            Logging.severe("http://bukkit.onarandombox.com/?dir=multiverse-core");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.reloadConfig();

        try {
            this.getMessager().setLocale(new Locale(this.getMVIConfig().getLocale()));
        } catch (IllegalArgumentException e) {
            Logging.severe(e.getMessage());
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Initialize data class
        //this.getWorldProfileContainerStore().setWorldProfiles(this.getData().getWorldProfiles());

        Logging.setDebugLevel(getCore().getMVConfig().getGlobalDebug());

        this.getCore().incrementPluginCount();

        // Register Events
        Bukkit.getPluginManager().registerEvents(inventoriesListener, this);
        if (Bukkit.getPluginManager().getPlugin("Multiverse-Adventure") != null) {
            Bukkit.getPluginManager().registerEvents(adventureListener, this);
        }
        if (getCore().getProtocolVersion() >= 24) {
            new CoreDebugListener(this);
        }

        // Register Commands
        this.registerCommands();

        // Hook plugins that can be imported from
        this.hookImportables();

        Sharables.init(this);

        this.dupingPatch = InventoriesDupingPatch.enableDupingPatch(this);

        // Display enable message/version info
        Logging.log(true, Level.INFO, "enabled.");
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
        this.getCommandHandler().registerCommand(new CreateGroupCommand(this));
        this.getCommandHandler().registerCommand(new DeleteGroupCommand(this));
        this.getCommandHandler().registerCommand(new SpawnCommand(this));
        this.getCommandHandler().registerCommand(new GroupCommand(this));
        this.getCommandHandler().registerCommand(new ToggleCommand(this));
        this.getCommandHandler().registerCommand(new MigrateCommand(this));
        for (com.onarandombox.commandhandler.Command c : this.commandHandler.getAllCommands()) {
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
        Logging.log(level, msg);
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
        buffer += logAndAddToPasteBinBuffer("=== Settings ===");
        buffer += logAndAddToPasteBinBuffer("First Run: " + this.getMVIConfig().isFirstRun());
        buffer += logAndAddToPasteBinBuffer("Using Bypass: " + this.getMVIConfig().isUsingBypass());
        buffer += logAndAddToPasteBinBuffer("Default Ungrouped Worlds: " + this.getMVIConfig().isDefaultingUngroupedWorlds());
        buffer += logAndAddToPasteBinBuffer("Save and Load on Log In and Out: " + this.getMVIConfig().usingLoggingSaveLoad());
        buffer += logAndAddToPasteBinBuffer("Using GameMode Profiles: " + this.getMVIConfig().isUsingGameModeProfiles());
        buffer += logAndAddToPasteBinBuffer("=== Shares ===");
        buffer += logAndAddToPasteBinBuffer("Optionals for Ungrouped Worlds: " + this.getMVIConfig().usingOptionalsForUngrouped());
        buffer += logAndAddToPasteBinBuffer("Enabled Optionals: " + this.getMVIConfig().getOptionalShares());
        buffer += logAndAddToPasteBinBuffer("=== Groups ===");
        for (WorldGroup group : this.getGroupManager().getGroups()) {
            buffer += logAndAddToPasteBinBuffer(group.toString());
        }
        return buffer;
    }

    /**
     * Builds a String containing Multiverse-Inventories' version info.
     *
     * @return The version info.
     */
    public String getVersionInfo() {
        StringBuilder versionInfo = new StringBuilder("[Multiverse-Inventories] Multiverse-Inventories Version: " + this.getDescription().getVersion() + '\n'
                + "[Multiverse-Inventories] === Settings ===" + '\n'
                + "[Multiverse-Inventories] First Run: " + this.getMVIConfig().isFirstRun() + '\n'
                + "[Multiverse-Inventories] Using Bypass: " + this.getMVIConfig().isUsingBypass() + '\n'
                + "[Multiverse-Inventories] Default Ungrouped Worlds: " + this.getMVIConfig().isDefaultingUngroupedWorlds() + '\n'
                + "[Multiverse-Inventories] Save and Load on Log In and Out: " + this.getMVIConfig().usingLoggingSaveLoad() + '\n'
                + "[Multiverse-Inventories] Using GameMode Profiles: " + this.getMVIConfig().isUsingGameModeProfiles() + '\n'
                + "[Multiverse-Inventories] === Shares ===" + '\n'
                + "[Multiverse-Inventories] Optionals for Ungrouped Worlds: " + this.getMVIConfig().usingOptionalsForUngrouped() + '\n'
                + "[Multiverse-Inventories] Enabled Optionals: " + this.getMVIConfig().getOptionalShares() + '\n'
                + "[Multiverse-Inventories] === Groups ===" + '\n');

        for (WorldGroup group : this.getGroupManager().getGroups()) {
            versionInfo.append("[Multiverse-Inventories] ").append(group.toString()).append('\n');
        }

        return versionInfo.toString();
    }

    private String logAndAddToPasteBinBuffer(String string) {
        Logging.info(string);
        return Logging.getPrefixedMessage(string + '\n', false);
    }

    /**
     * @return the Config object which contains settings for this plugin.
     */
    public InventoriesConfig getMVIConfig() {
        return this.config;
    }

    /**
     * Nulls the config object and reloads a new one, also resetting the world groups in memory.
     */
    @Override
    public void reloadConfig() {
        try {
            this.config = new InventoriesConfig(this);
            this.worldGroupManager = new YamlWorldGroupManager(this, this.config.getConfig());
            this.worldProfileContainerStore = new WeakProfileContainerStore(this, ContainerType.WORLD);
            this.groupProfileContainerStore = new WeakProfileContainerStore(this, ContainerType.GROUP);

            if (data != null) {
                this.data.clearCache();
            }

            //this.data = null;
            Logging.fine("Loaded config file!");
        } catch (IOException e) {  // Catch errors loading the config file and exit out if found.
            Logging.severe(this.getMessager().getMessage(Message.ERROR_CONFIG_LOAD));
            Logging.severe(e.getMessage());
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            @Override
            public void run() {
                // Create initial World Group for first run IF NO GROUPS EXIST
                if (getMVIConfig().isFirstRun()) {
                    Logging.info("First run!");
                    if (getGroupManager().getGroups().isEmpty()) {
                        getGroupManager().createDefaultGroup();
                    }

                    getMVIConfig().setFirstRun(false);
                }
                getGroupManager().checkForConflicts(null);
            }
        }, 1L);
    }

    /**
     * @return the PlayerData object which contains data for this plugin.
     */
    public ProfileDataSource getData() {
        if (this.data == null) {
            // Loads the data
            try {
                this.data = new FlatFileProfileDataSource(this);
            } catch (IOException e) {  // Catch errors loading the language file and exit out if found.
                Logging.severe(this.getMessager().getMessage(Message.ERROR_DATA_LOAD));
                Logging.severe(e.getMessage());
                Bukkit.getPluginManager().disablePlugin(this);
                return null;
            }
        }
        return this.data;
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
        if (messager == null) {
            throw new IllegalArgumentException("The new messager can't be null!");
        }
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
        return this.worldGroupManager;
    }

    /**
     * Returns the world profile container store for this plugin.
     * <p>Player profiles for an individual world can be found here.</p>
     *
     * @return the world profile container store for this plugin.
     */
    public ProfileContainerStore getWorldProfileContainerStore() {
        return worldProfileContainerStore;
    }

    /**
     * Returns the group profile container store for this plugin.
     * <p>Player profiles for a world group can be found here.</p>
     *
     * @return the group profile container store for this plugin.
     */
    public ProfileContainerStore getGroupProfileContainerStore() {
        return groupProfileContainerStore;
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
        if (!newServerFolder.isDirectory()) {
            throw new IllegalArgumentException("That's not a folder!");
        }
        this.serverFolder = newServerFolder;
    }
}

