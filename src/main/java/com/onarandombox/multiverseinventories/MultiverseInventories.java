package com.onarandombox.multiverseinventories;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.commands.HelpCommand;
import com.onarandombox.multiverseinventories.api.GroupManager;
import com.onarandombox.multiverseinventories.api.Inventories;
import com.onarandombox.multiverseinventories.api.InventoriesConfig;
import com.onarandombox.multiverseinventories.api.profile.PlayerData;
import com.onarandombox.multiverseinventories.api.profile.ProfileTypeManager;
import com.onarandombox.multiverseinventories.api.profile.WorldGroupProfile;
import com.onarandombox.multiverseinventories.api.profile.WorldProfileManager;
import com.onarandombox.multiverseinventories.api.share.Sharables;
import com.onarandombox.multiverseinventories.command.AddSharesCommand;
import com.onarandombox.multiverseinventories.command.AddWorldCommand;
import com.onarandombox.multiverseinventories.command.DebugCommand;
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
import com.onarandombox.multiverseinventories.migration.ImportManager;
import com.onarandombox.multiverseinventories.util.CommentedInventoriesConfig;
import com.onarandombox.multiverseinventories.util.Logging;
import com.onarandombox.multiverseinventories.util.Perm;
import com.onarandombox.multiverseinventories.util.data.FlatFilePlayerData;
import com.pneumaticraft.commandhandler.multiverse.CommandHandler;
import me.drayshak.WorldInventories.WorldInventories;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
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
public class MultiverseInventories extends JavaPlugin implements Inventories {

    private final int requiresProtocol = 12;
    private final InventoriesListener inventoriesListener = new InventoriesListener(this);
    private final AdventureListener adventureListener = new AdventureListener(this);

    private Messager messager = new DefaultMessager(this);
    private GroupManager groupManager = null;
    private WorldProfileManager worldProfileManager = null;
    private ProfileTypeManager profileTypeManager = null;
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
            //getData().updateWorld(player.getName(), world);
            if (getMVIConfig().usingLoggingSaveLoad()) {
                ShareHandler.updateProfile(this, player, new DefaultPersistingProfile(Sharables.allOf(),
                        getWorldManager().getWorldProfile(world).getPlayerData(player)));
                getData().setLoadOnLogin(player.getName(), true);
            }
        }
        Logging.close();
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
            Logging.severe("This version of Profiles requires Protocol Level: " + this.getRequiredProtocol());
            Logging.severe("Your of Core Protocol Level is: " + this.getCore().getProtocolVersion());
            Logging.severe("Grab an updated copy at: ");
            Logging.severe("http://bukkit.onarandombox.com/?dir=multiverse-core");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }
        Logging.initDebug(this);

        this.reloadConfig();

        try {
            this.getMessager().setLocale(new Locale(this.getMVIConfig().getLocale()));
        } catch (IllegalArgumentException e) {
            Logging.severe(e.getMessage());
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.getProfileTypeManager();

        // Initialize data class
        //this.getWorldManager().setWorldProfiles(this.getData().getWorldProfiles());

        this.getCore().incrementPluginCount();

        // Register Events
        Bukkit.getPluginManager().registerEvents(inventoriesListener, this);
        if (Bukkit.getPluginManager().getPlugin("Multiverse-Adventure") != null) {
            Bukkit.getPluginManager().registerEvents(adventureListener, this);
        }

        try {
            InventoryType.ENDER_CHEST.getClass();
            try {
                Player.class.getMethod("getEnderChest");
                Logging.fine("Ender chest supported through proper Bukkit and Multiverse-Inventories API!");
            } catch (NoSuchMethodException ignore) {
                Bukkit.getPluginManager().registerEvents(new EnderChestListenerEarly1_3_1_RBs(this), this);
                Logging.fine("Ender chest supported for early releases of Bukkit for MC 1.3.1.");
            }
        } catch (NoSuchFieldError ignore) {
            Logging.fine("No ender chest support for pre MC 1.3!");
        }

        // Register Commands
        this.registerCommands();

        // Hook plugins that can be imported from
        this.hookImportables();

        Sharables.init(this);

        // Display enable message/version info
        Logging.info("enabled.", true);
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
        this.getCommandHandler().registerCommand(new DebugCommand(this));
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
     * {@inheritDoc}
     */
    @Override
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
        Logging.log(level, msg, false);
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
     * {@inheritDoc}
     */
    @Override
    public String getVersionInfo() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.logAndAddToPasteBinBuffer("Multiverse-Inventories Version: "
                + this.getDescription().getVersion()));
        builder.append(this.logAndAddToPasteBinBuffer("Debug Level: " + this.getMVIConfig().getGlobalDebug()));
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
        return Logging.getString(string + "\n", false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InventoriesConfig getMVIConfig() {
        if (this.config == null) {
            // Loads the configuration
            try {
                this.config = new CommentedInventoriesConfig(this);
                Logging.fine("Loaded config file!");
            } catch (IOException e) {  // Catch errors loading the config file and exit out if found.
                Logging.severe(this.getMessager().getMessage(Message.ERROR_CONFIG_LOAD));
                Logging.severe(e.getMessage());
                Bukkit.getPluginManager().disablePlugin(this);
                return null;
            }
        }
        return this.config;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reloadConfig() {
        this.config = null;
        this.groupManager = null;
        this.worldProfileManager = null;
        this.profileTypeManager = null;
        //this.data = null;

        ProfileTypes.resetProfileTypes();
        this.getProfileTypeManager();
        this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            @Override
            public void run() {
                // Get world groups from config
                getGroupManager().setGroups(getMVIConfig().getWorldGroups());
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
     * {@inheritDoc}
     */
    @Override
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
        if (messager == null)
            throw new IllegalArgumentException("The new messager can't be null!");

        this.messager = messager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getRequiredProtocol() {
        return this.requiresProtocol;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GroupManager getGroupManager() {
        if (this.groupManager == null) {
            this.groupManager = new DefaultGroupManager(this);
        }
        return this.groupManager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WorldProfileManager getWorldManager() {
        if (this.worldProfileManager == null) {
            this.worldProfileManager = new WeakWorldProfileManager(this);
        }
        return this.worldProfileManager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public File getServerFolder() {
        return serverFolder;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setServerFolder(File newServerFolder) {
        if (!newServerFolder.isDirectory())
            throw new IllegalArgumentException("That's not a folder!");

        this.serverFolder = newServerFolder;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProfileTypeManager getProfileTypeManager() {
        if (this.profileTypeManager == null) {
            this.profileTypeManager = new DefaultProfileTypeManager(new File(this.getDataFolder(), "profiles.yml"));
        }
        return profileTypeManager;
    }
}

