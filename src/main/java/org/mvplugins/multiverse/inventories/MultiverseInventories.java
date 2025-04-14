package org.mvplugins.multiverse.inventories;

import com.dumptruckman.minecraft.util.Logging;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.mvplugins.multiverse.core.MultiverseCoreApi;
import org.mvplugins.multiverse.core.config.CoreConfig;
import org.mvplugins.multiverse.core.destination.DestinationsProvider;
import org.mvplugins.multiverse.core.inject.PluginServiceLocatorFactory;
import org.mvplugins.multiverse.core.module.MultiverseModule;
import org.mvplugins.multiverse.core.utils.StringFormatter;
import org.mvplugins.multiverse.inventories.command.MVInvCommandConditions;
import org.mvplugins.multiverse.inventories.commands.InventoriesCommand;
import org.mvplugins.multiverse.inventories.command.MVInvCommandCompletion;
import org.mvplugins.multiverse.inventories.command.MVInvCommandContexts;
import org.mvplugins.multiverse.inventories.config.InventoriesConfig;
import org.mvplugins.multiverse.inventories.dataimport.DataImportManager;
import org.mvplugins.multiverse.inventories.dataimport.DataImporter;
import org.mvplugins.multiverse.inventories.destination.LastLocationDestination;
import org.mvplugins.multiverse.inventories.handleshare.ShareHandleListener;
import org.mvplugins.multiverse.inventories.handleshare.SingleShareWriter;
import org.mvplugins.multiverse.inventories.handleshare.SpawnChangeListener;
import org.mvplugins.multiverse.inventories.handleshare.WriteOnlyShareHandler;
import org.mvplugins.multiverse.inventories.profile.ProfileCacheManager;
import org.mvplugins.multiverse.inventories.profile.ProfileDataSource;
import org.mvplugins.multiverse.inventories.profile.key.GlobalProfileKey;
import org.mvplugins.multiverse.inventories.profile.key.ProfileTypes;
import org.mvplugins.multiverse.inventories.profile.container.ProfileContainerStoreProvider;
import org.mvplugins.multiverse.inventories.profile.group.WorldGroupManager;
import org.mvplugins.multiverse.inventories.share.Sharables;
import org.mvplugins.multiverse.inventories.util.ItemStackConverter;
import org.mvplugins.multiverse.inventories.util.Perm;
import org.bukkit.Bukkit;
import org.mvplugins.multiverse.core.command.MVCommandManager;
import org.mvplugins.multiverse.core.inject.PluginServiceLocator;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jakarta.inject.Provider;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.external.vavr.control.Try;

/**
 * Multiverse-Inventories plugin main class.
 */
@Service
public class MultiverseInventories extends MultiverseModule {

    private static final double TARGET_CORE_API_VERSION = 5.0;

    @Inject
    private Provider<CoreConfig> coreConfig;
    @Inject
    private Provider<DestinationsProvider> destinationsProvider;
    @Inject
    private Provider<InventoriesConfig> inventoriesConfig;
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
    private Provider<ProfileCacheManager> profileCacheManager;
    @Inject
    private Provider<ProfileContainerStoreProvider> profileContainerStoreProvider;
    @Inject
    private Provider<DataImportManager> dataImportManager;
    @Inject
    private Provider<MVInvCommandCompletion> mvInvCommandCompletion;
    @Inject
    private Provider<MVInvCommandContexts> mvInvCommandContexts;
    @Inject
    private Provider<MVInvCommandConditions> mvInvCommandConditions;

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

        initializeDependencyInjection(new MultiverseInventoriesPluginBinder(this));
        ProfileTypes.init(this);
        Sharables.init(this);
        Perm.register(this);
        ItemStackConverter.init(this);
        Logging.fine("ItemStackConverter is using byte serialization: " + ItemStackConverter.hasByteSerializeSupport());
        this.reloadConfig();
        inventoriesConfig.get().save().onFailure(e -> Logging.severe("Failed to save config file!"));

        // Register Stuff
        this.registerEvents();
        this.setUpLocales();
        this.registerCommands();
        this.registerDestinations();

        // Hook plugins that can be imported from
        this.hookImportables();

        // Init other extensions
        this.hookLuckPerms();
        this.dupingPatch = InventoriesDupingPatch.enableDupingPatch(this);

        Logging.config("Version %s (API v%s) Enabled - By %s",
                this.getDescription().getVersion(), getVersionAsNumber(), StringFormatter.joinAnd(this.getDescription().getAuthors()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDisable() {
        super.onDisable();

        for (final Player player : getServer().getOnlinePlayers()) {
            SingleShareWriter.of(this, player, Sharables.LAST_LOCATION).write(player.getLocation().clone());
            new WriteOnlyShareHandler(this, player).handleSharing();
            if (inventoriesConfig.get().getApplyPlayerdataOnJoin()) {
                profileDataSource.get().modifyGlobalProfile(
                        GlobalProfileKey.create(player), profile -> profile.setLoadOnLogin(true));
            }
        }

        this.dupingPatch.disable();
        this.shutdownDependencyInjection();
        Logging.shutdown();
    }

    private void registerEvents() {
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
    }

    private void registerCommands() {
        Try.run(() -> {
            mvInvCommandCompletion.get();
            mvInvCommandContexts.get();
            mvInvCommandConditions.get();
        }).onFailure(e -> {
            Logging.warning("Failed to register command completers: %s", e.getMessage());
        });
        registerCommands(InventoriesCommand.class);
    }

    private void registerDestinations() {
        destinationsProvider.get().registerDestination(serviceLocator.getService(LastLocationDestination.class));
    }

    private void hookImportables() {
        serviceLocator.getAllServices(DataImporter.class).forEach(dataImporter -> {
            dataImportManager.get().register(dataImporter);
        });
    }

    private void hookLuckPerms() {
        Try.run(() -> Class.forName("net.luckperms.api.LuckPerms"))
                .onFailure(e -> Logging.fine("Luckperms is not installed!"))
                .andThenTry(() -> {
                    Logging.fine("Found luckperms!");
                    serviceLocator.getService(WorldGroupContextCalculator.class);
                });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getTargetCoreVersion() {
        return TARGET_CORE_API_VERSION;
    }

    /**
     * Nulls the config object and reloads a new one, also resetting the world groups in memory.
     */
    @Override
    public void reloadConfig() {
        try {
            Logging.setDebugLevel(coreConfig.get().getGlobalDebug());

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
                profileCacheManager.get().clearAllCache();
            }

            Logging.fine("Reloaded all config and groups!");
        } catch (Exception e) {  // Catch errors loading the config file and exit out if found.
            Logging.severe("Encountered an error while loading the configuration file. Disabling...");
            Logging.severe(e.getMessage());
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        this.getServer().getScheduler().runTaskLater(this, () -> {
            // Create initial World Group for first run IF NO GROUPS EXIST
            if (inventoriesConfig.get().getFirstRun()) {
                Logging.info("First run!");
                if (worldGroupManager.get().getGroups().isEmpty()) {
                    worldGroupManager.get().createDefaultGroup();
                }

                inventoriesConfig.get().setFirstRun(false);
            }
            worldGroupManager.get().checkForConflicts(null);
        }, 1L);
    }

    public boolean isUsingSpawnChangeEvent() {
        return usingSpawnChangeEvent;
    }
}
