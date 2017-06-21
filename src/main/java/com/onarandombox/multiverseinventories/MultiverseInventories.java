package com.onarandombox.multiverseinventories;

import com.dumptruckman.minecraft.util.Logging;
import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVPlugin;
import com.onarandombox.MultiverseCore.commands.HelpCommand;
import com.onarandombox.multiverseinventories.api.GroupManager;
import com.onarandombox.multiverseinventories.api.InventoriesConfig;
import com.onarandombox.multiverseinventories.util.data.PlayerData;
import com.onarandombox.multiverseinventories.profile.container.WorldGroupProfile;
import com.onarandombox.multiverseinventories.api.profile.WorldProfileManager;
import com.onarandombox.multiverseinventories.api.share.Sharables;
import com.onarandombox.multiverseinventories.command.AddSharesCommand;
import com.onarandombox.multiverseinventories.command.AddWorldCommand;
import com.onarandombox.multiverseinventories.command.GroupCommand;
import com.onarandombox.multiverseinventories.command.ImportCommand;
import com.onarandombox.multiverseinventories.command.InfoCommand;
import com.onarandombox.multiverseinventories.command.ListCommand;
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
import com.onarandombox.multiverseinventories.util.data.FlatFilePlayerData;
import com.pneumaticraft.commandhandler.multiverse.CommandHandler;
import me.drayshak.WorldInventories.WorldInventories;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
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

    private final int requiresProtocol = 20;
    private final InventoriesListener inventoriesListener = new InventoriesListener(this);
    private final AdventureListener adventureListener = new AdventureListener(this);

    private Messager messager = new DefaultMessager(this);
    private GroupManager groupManager = null;
    private WorldProfileManager worldProfileManager = null;
    private ImportManager importManager = new ImportManager(this);

    private CommandHandler commandHandler = null;
    private MultiverseCore core = null;
    private InventoriesConfig config = null;
    private PlayerData data = null;

    private File serverFolder = new File(System.getProperty("user.dir"));

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDisable() {
        for (final Player player : getServer().getOnlinePlayers()) {
            final String world = player.getWorld().getName();
            //getData().updateLastWorld(player.getName(), world);
            if (getMVIConfig().usingLoggingSaveLoad()) {
                ShareHandler.updateProfile(this, player, new DefaultPersistingProfile(Sharables.allOf(),
                        getWorldManager().getWorldProfile(world).getPlayerData(player)));
                getData().setLoadOnLogin(player.getName(), true);
            }
        }
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
        //this.getWorldManager().setWorldProfiles(this.getData().getWorldProfiles());

        this.getCore().incrementPluginCount();

        // Register Events
        Bukkit.getPluginManager().registerEvents(inventoriesListener, this);
        if (Bukkit.getPluginManager().getPlugin("Multiverse-Adventure") != null) {
            Bukkit.getPluginManager().registerEvents(adventureListener, this);
        }

        // Register Commands
        this.registerCommands();

        // Hook plugins that can be imported from
        this.hookImportables();

        Sharables.init(this);

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
        this.getCommandHandler().registerCommand(new SpawnCommand(this));
        this.getCommandHandler().registerCommand(new GroupCommand(this));
        this.getCommandHandler().registerCommand(new ToggleCommand(this));
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
        builder.append(this.logAndAddToPasteBinBuffer("First Run: " + this.getMVIConfig().isFirstRun()));
        builder.append(this.logAndAddToPasteBinBuffer("Using Bypass: " + this.getMVIConfig().isUsingBypass()));
        builder.append(this.logAndAddToPasteBinBuffer("Default Ungrouped Worlds: "
                + this.getMVIConfig().isDefaultingUngroupedWorlds()));
        builder.append(this.logAndAddToPasteBinBuffer("Using GameMode Profiles: "
                + this.getMVIConfig().isUsingGameModeProfiles()));
        builder.append(this.logAndAddToPasteBinBuffer("=== Groups ==="));
        for (WorldGroupProfile group : this.getGroupManager().getGroups()) {
            builder.append(this.logAndAddToPasteBinBuffer(group.toString()));
        }
        return builder.toString();
    }

    private String logAndAddToPasteBinBuffer(String string) {
        Logging.info(string);
        return Logging.getPrefixedMessage(string + "\n", false);
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
            this.config = new YamlInventoriesConfig(this);
            this.groupManager = new YamlGroupManager(this, new File(getDataFolder(), "groups.yml"),
                    ((YamlInventoriesConfig) config).getConfig());
            this.worldProfileManager = new WeakWorldProfileManager(this);
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
                }
                getGroupManager().checkForConflicts(null);
            }
        }, 1L);
    }

    /**
     * @return the PlayerData object which contains data for this plugin.
     */
    public PlayerData getData() {
        if (this.data == null) {
            // Loads the data
            try {
                this.data = new FlatFilePlayerData(this);
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
    public GroupManager getGroupManager() {
        return this.groupManager;
    }

    /**
     * @return The World/Group Profile manager for this plugin.
     * This is where you find access to individual player data.
     */
    public WorldProfileManager getWorldManager() {
        return this.worldProfileManager;
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

