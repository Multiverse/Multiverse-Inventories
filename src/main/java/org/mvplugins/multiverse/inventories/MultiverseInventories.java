package org.mvplugins.multiverse.inventories;

import com.dumptruckman.minecraft.util.Logging;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.mvplugins.multiverse.core.MultiverseCoreApi;
import org.mvplugins.multiverse.core.MultiversePlugin;
import org.mvplugins.multiverse.core.config.MVCoreConfig;
import org.mvplugins.multiverse.core.inject.PluginServiceLocatorFactory;
import org.mvplugins.multiverse.core.utils.StringFormatter;
import org.mvplugins.multiverse.inventories.commands.InventoriesCommand;
import org.mvplugins.multiverse.inventories.commandtools.MVInvCommandCompletion;
import org.mvplugins.multiverse.inventories.commandtools.MVInvCommandContexts;
import org.mvplugins.multiverse.inventories.config.InventoriesConfig;
import org.mvplugins.multiverse.inventories.dataimport.DataImportManager;
import org.mvplugins.multiverse.inventories.dataimport.DataImporter;
import org.mvplugins.multiverse.inventories.handleshare.ShareHandleListener;
import org.mvplugins.multiverse.inventories.handleshare.ShareHandlingUpdater;
import org.mvplugins.multiverse.inventories.handleshare.SpawnChangeListener;
import org.mvplugins.multiverse.inventories.handleshare.PersistingProfile;
import org.mvplugins.multiverse.inventories.profile.ProfileDataSource;
import org.mvplugins.multiverse.inventories.profile.container.ContainerType;
import org.mvplugins.multiverse.inventories.profile.container.ProfileContainerStoreProvider;
import org.mvplugins.multiverse.inventories.profile.group.WorldGroupManager;
import org.mvplugins.multiverse.inventories.share.Sharables;
import org.mvplugins.multiverse.inventories.util.Perm;
import org.bukkit.Bukkit;
import org.mvplugins.multiverse.core.commandtools.MVCommandManager;
import org.mvplugins.multiverse.core.inject.PluginServiceLocator;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jakarta.inject.Provider;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.external.vavr.control.Try;

/**
 * Multiverse-Inventories plugin main class.
 */
@Service
public class MultiverseInventories extends MultiversePlugin {

    private static final int PROTOCOL = 50;

    @Inject
    private Provider<InventoriesConfig> inventoriesConfig;
    @Inject
    private Provider<MVCommandManager> commandManager;
    @Inject
    private Provider<MVCoreConfig> mvCoreConfig;
    @Inject
    private Provider<ShareHandleListener> shareHandleListener;
    @Inject
    private Provider<RespawnListener> respawnListener;
    @Inject
    private Provider<MVEventsListener> mvEventsListener;
    @Inject
    private Provider<WorldGroupManager> worldGroupManager;
    @Inject
    private Provider<ProfileDataSource> profileDataSource;
    @Inject
    private Provider<ProfileContainerStoreProvider> profileContainerStoreProvider;
    @Inject
    private Provider<DataImportManager> dataImportManager;
    @Inject
    private Provider<MVInvCommandCompletion> mvInvCommandCompletion;
    @Inject
    private Provider<MVInvCommandContexts> mvInvCommandContexts;

    private PluginServiceLocator serviceLocator;
    private InventoriesDupingPatch dupingPatch;
    private boolean usingSpawnChangeEvent = false;

    public MultiverseInventories() {
        super();
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
        super.onEnable();

        initializeDependencyInjection();
        Perm.register(this);
        this.reloadConfig();
        inventoriesConfig.get().save().onFailure(e -> Logging.severe("Failed to save config file!"));

        // Register Events
        PluginManager pluginManager = this.getServer().getPluginManager();
        pluginManager.registerEvents(shareHandleListener.get(), this);
        pluginManager.registerEvents(respawnListener.get(), this);
        pluginManager.registerEvents(mvEventsListener.get(), this);
        if (inventoriesConfig.get().getUseImprovedRespawnLocationDetection()) {
            try {
                Class.forName("org.bukkit.event.player.PlayerSpawnChangeEvent");
                pluginManager.registerEvents(new SpawnChangeListener(this), this);
                usingSpawnChangeEvent = true;
                Logging.fine("Yayy PlayerSpawnChangeEvent will be used!");
            } catch (ClassNotFoundException e) {
                Logging.fine("PlayerSpawnChangeEvent will not be used!");
            }
        }

        // Register Commands
        this.registerCommands();
        // Hook plugins that can be imported from
        this.hookImportables();
        Sharables.init(this);
        this.dupingPatch = InventoriesDupingPatch.enableDupingPatch(this);

        Logging.config("Version %s (API v%s) Enabled - By %s",
                this.getDescription().getVersion(), getTargetCoreProtocolVersion(), StringFormatter.joinAnd(this.getDescription().getAuthors()));
    }

    private void initializeDependencyInjection() {
        serviceLocator = PluginServiceLocatorFactory.get()
                .registerPlugin(new MultiverseInventoriesPluginBinder(this), MultiverseCoreApi.get().getServiceLocator())
                .flatMap(PluginServiceLocator::enable)
                .getOrElseThrow(exception -> {
                    Logging.severe("Failed to initialize dependency injection!");
                    getServer().getPluginManager().disablePlugin(this);
                    return new RuntimeException(exception);
                });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDisable() {
        super.onDisable();

        for (final Player player : getServer().getOnlinePlayers()) {
            final String world = player.getWorld().getName();
            if (inventoriesConfig.get().getSavePlayerdataOnQuit()) {
                ShareHandlingUpdater.updateProfile(this, player, new PersistingProfile(
                        Sharables.allOf(),
                        profileContainerStoreProvider.get().getStore(ContainerType.WORLD)
                                .getContainer(world)
                                .getPlayerData(player)));
                if (inventoriesConfig.get().getApplyPlayerdataOnJoin()) {
                    profileDataSource.get().modifyGlobalProfile(player, profile -> profile.setLoadOnLogin(true));
                }
            }
        }

        this.dupingPatch.disable();
        Logging.shutdown();
    }

    private void registerCommands() {
        Try.of(() -> commandManager.get())
                .andThenTry(commandManager -> {
                    commandManager.getLocales().addFileResClassLoader(this);
                    commandManager.getLocales().addBundleClassLoader(this.getClassLoader());
                    commandManager.getLocales().addMessageBundles("multiverse-inventories");

                    mvInvCommandCompletion.get();
                    mvInvCommandContexts.get();

                    serviceLocator.getAllServices(InventoriesCommand.class).forEach(commandManager::registerCommand);
                })
                .onFailure(e -> {
                    Logging.severe("Failed to register commands");
                    e.printStackTrace();
                });
    }

    private void hookImportables() {
        serviceLocator.getAllServices(DataImporter.class).forEach(dataImporter -> {
            dataImportManager.get().register(dataImporter);
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getTargetCoreProtocolVersion() {
        return PROTOCOL;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PluginServiceLocator getServiceLocator() {
        return serviceLocator;
    }

    /**
     * Nulls the config object and reloads a new one, also resetting the world groups in memory.
     */
    @Override
    public void reloadConfig() {
        try {
            Logging.setDebugLevel(mvCoreConfig.get().getGlobalDebug());

            inventoriesConfig.get().load().onFailure(e -> {
                Logging.severe("Failed to load config file!");
                Logging.severe(e.getMessage());
            });
            worldGroupManager.get().load().onFailure(e -> {
                Logging.severe("Failed to load world groups!");
                Logging.severe(e.getMessage());
            });
            profileContainerStoreProvider.get().clearCache();

            if (profileDataSource.get() != null) {
                profileDataSource.get().clearAllCache();
            }

            Logging.fine("Reloaded all config and groups!");
        } catch (Exception e) {  // Catch errors loading the config file and exit out if found.
            Logging.severe("Encountered an error while loading the configuration file. Disabling...");
            Logging.severe(e.getMessage());
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            @Override
            public void run() {
                // Create initial World Group for first run IF NO GROUPS EXIST
                if (inventoriesConfig.get().getFirstRun()) {
                    Logging.info("First run!");
                    if (worldGroupManager.get().getGroups().isEmpty()) {
                        worldGroupManager.get().createDefaultGroup();
                    }

                    inventoriesConfig.get().setFirstRun(false);
                }
                worldGroupManager.get().checkForConflicts(null);
            }
        }, 1L);
    }

    public boolean isUsingSpawnChangeEvent() {
        return usingSpawnChangeEvent;
    }
}

