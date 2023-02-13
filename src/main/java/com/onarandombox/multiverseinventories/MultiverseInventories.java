package com.onarandombox.multiverseinventories;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

import com.dumptruckman.minecraft.util.Logging;
import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVCore;
import com.onarandombox.MultiverseCore.api.MVPlugin;
import com.onarandombox.MultiverseCore.commandtools.MVCommandManager;
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
import com.onarandombox.multiverseinventories.command.tools.MVInvCommandCompletions;
import com.onarandombox.multiverseinventories.command.tools.MVInvCommandConditions;
import com.onarandombox.multiverseinventories.command.tools.MVInvCommandContexts;
import com.onarandombox.multiverseinventories.locale.Message;
import com.onarandombox.multiverseinventories.locale.Messager;
import com.onarandombox.multiverseinventories.locale.Messaging;
import com.onarandombox.multiverseinventories.migration.ImportManager;
import com.onarandombox.multiverseinventories.profile.ProfileDataSource;
import com.onarandombox.multiverseinventories.profile.WorldGroupManager;
import com.onarandombox.multiverseinventories.profile.container.ContainerType;
import com.onarandombox.multiverseinventories.profile.container.ProfileContainerStore;
import com.onarandombox.multiverseinventories.share.Sharables;
import com.onarandombox.multiverseinventories.util.Perm;
import me.drayshak.WorldInventories.WorldInventories;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import uk.co.tggl.pluckerpluck.multiinv.MultiInv;

/**
 * Multiverse-Inventories plugin main class.
 */
public class MultiverseInventories extends JavaPlugin implements MVPlugin, Messaging {

    private static final int PROTOCOL = 50;

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

    private MVCore core = null;
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
    public void onLoad() {
        Logging.init(this);
        this.getDataFolder().mkdirs();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void onEnable() {
        this.core = (MVCore) this.getServer().getPluginManager().getPlugin("Multiverse-Core");
        if (this.core == null) {
            Logging.severe("Core not found! You must have Multiverse-Core installed to use this plugin!");
            Logging.severe("Grab a copy at: ");
            Logging.severe("https://dev.bukkit.org/projects/multiverse-core");
            Logging.severe("Disabling!");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }
        if (this.core.getProtocolVersion() < this.getProtocolVersion()) {
            Logging.severe("Your Multiverse-Core is OUT OF DATE");
            Logging.severe("This version of " + this.getDescription().getName() + " requires Protocol Level: " + this.getProtocolVersion());
            Logging.severe("Your of Core Protocol Level is: " + this.core.getProtocolVersion());
            Logging.severe("Grab an updated copy at: ");
            Logging.severe("https://dev.bukkit.org/projects/multiverse-core");
            Logging.severe("Disabling!");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }
        Logging.setDebugLevel(core.getMVConfig().getGlobalDebug());
        this.core.incrementPluginCount();
        this.onMVPluginEnable();
        Logging.config("Version %s (API v%s) Enabled - By %s", this.getDescription().getVersion(), getProtocolVersion(), getAuthors());
    }

    private void onMVPluginEnable() {
        Perm.register(this);

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

        this.core.decrementPluginCount();
        Logging.shutdown();
    }

    private void registerCommands() {
        MVInvCommandCompletions.init(this);
        MVInvCommandConditions.init(this);
        MVInvCommandContexts.init(this);

        MVCommandManager commandManager = this.getCore().getMVCommandManager();
        commandManager.registerCommand(new AddSharesCommand(this));
        commandManager.registerCommand(new AddWorldCommand(this));
        commandManager.registerCommand(new CreateGroupCommand(this));
        commandManager.registerCommand(new DeleteGroupCommand(this));
        commandManager.registerCommand(new GroupCommand(this));
        commandManager.registerCommand(new ImportCommand(this));
        commandManager.registerCommand(new InfoCommand(this));
        commandManager.registerCommand(new ListCommand(this));
        commandManager.registerCommand(new MigrateCommand(this));
        commandManager.registerCommand(new ReloadCommand(this));
        commandManager.registerCommand(new RemoveSharesCommand(this));
        commandManager.registerCommand(new RemoveWorldCommand(this));
        commandManager.registerCommand(new SpawnCommand(this));
        commandManager.registerCommand(new ToggleCommand(this));
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
    public MVCore getCore() {
        return this.core;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getProtocolVersion() {
        return PROTOCOL;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAuthors() {
        List<String> authorsList = this.getDescription().getAuthors();
        if (authorsList.size() == 0) {
            return "";
        }

        StringBuilder authors = new StringBuilder();
        authors.append(authorsList.get(0));

        for (int i = 1; i < authorsList.size(); i++) {
            if (i == authorsList.size() - 1) {
                authors.append(" and ").append(authorsList.get(i));
            } else {
                authors.append(", ").append(authorsList.get(i));
            }
        }

        return authors.toString();
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

