/******************************************************************************
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.                       *
 * Multiverse 2 is licensed under the BSD License.                            *
 * For more information please check the README.md file included              *
 * with this project.                                                         *
 ******************************************************************************/

package com.onarandombox.multiverseinventories.util;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.listeners.MVEntityListener;
import com.onarandombox.MultiverseCore.listeners.MVPlayerListener;
import com.onarandombox.MultiverseCore.listeners.MVWeatherListener;
import com.onarandombox.MultiverseCore.utils.FileUtils;
import com.onarandombox.MultiverseCore.utils.TestingMode;
import com.onarandombox.MultiverseCore.utils.WorldManager;
import com.onarandombox.multiverseinventories.InventoriesListener;
import com.onarandombox.multiverseinventories.MultiverseInventories;
import junit.framework.Assert;
import org.bukkit.*;
import org.bukkit.World.Environment;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitScheduler;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.easymock.PowerMock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.MockGateway;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.reflect.Whitebox;

import java.io.File;
import java.lang.reflect.Field;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@PrepareForTest({InventoriesListener.class})
public class TestInstanceCreator {
    private MultiverseInventories plugin;
    private MultiverseCore core;
    private Server mockServer;
    private CommandSender commandSender;

    public static final File invDirectory = new File("bin/test/server/plugins/inventories-test");
    public static final File coreDirectory = new File("bin/test/server/plugins/core-test");
    public static final File serverDirectory = new File("bin/test/server");
    public static final File worldsDirectory = new File("bin/test/server");

    public boolean setUp() {
        TestingMode.enable();
        try {
            FileUtils.deleteFolder(invDirectory);
            FileUtils.deleteFolder(serverDirectory);
            Assert.assertFalse(invDirectory.exists());
            invDirectory.mkdirs();
            Assert.assertTrue(invDirectory.exists());

            MockGateway.MOCK_STANDARD_METHODS = false;

            // Initialize the Mock server.
            mockServer = mock(Server.class);
            JavaPluginLoader mockPluginLoader = PowerMock.createMock(JavaPluginLoader.class);
            Whitebox.setInternalState(mockPluginLoader, "server", mockServer);
            when(mockServer.getName()).thenReturn("TestBukkit");
            Logger.getLogger("Minecraft").setParent(Util.logger);
            when(mockServer.getLogger()).thenReturn(Util.logger);
            when(mockServer.getWorldContainer()).thenReturn(worldsDirectory);

            // Return a fake PDF file.
            PluginDescriptionFile pdf = PowerMockito.spy(new PluginDescriptionFile("Multiverse-Inventories", "2.4-test",
                    "com.onarandombox.multiverseinventories.MultiverseInventories"));
            when(pdf.getAuthors()).thenReturn(new ArrayList<String>());
            plugin = PowerMockito.spy(new MultiverseInventories(mockPluginLoader, pdf, invDirectory, new File(invDirectory, "testPluginFile")));
            doReturn(pdf).when(plugin).getDescription();
            doReturn(true).when(plugin).isEnabled();
            PluginDescriptionFile pdfCore = PowerMockito.spy(new PluginDescriptionFile("Multiverse-Core", "2.2-Test",
                    "com.onarandombox.MultiverseCore.MultiverseCore"));
            when(pdfCore.getAuthors()).thenReturn(new ArrayList<String>());
            core = PowerMockito.spy(new MultiverseCore(mockPluginLoader, pdf, coreDirectory, new File(coreDirectory, "testPluginFile")));
            doReturn(pdfCore).when(core).getDescription();
            doReturn(true).when(core).isEnabled();
            doReturn(Util.logger).when(core).getLogger();
            plugin.setServerFolder(serverDirectory);
            doReturn(core).when(plugin).getCore();

            // Let's let all MV files go to bin/test
            doReturn(invDirectory).when(plugin).getDataFolder();
            // Let's let all MV files go to bin/test
            doReturn(coreDirectory).when(core).getDataFolder();

            // Add Core to the list of loaded plugins
            JavaPlugin[] plugins = new JavaPlugin[]{plugin, core};

            // Mock the Plugin Manager
            PluginManager mockPluginManager = PowerMockito.mock(PluginManager.class);
            when(mockPluginManager.getPlugins()).thenReturn(plugins);
            when(mockPluginManager.getPlugin("Multiverse-Inventories")).thenReturn(plugin);
            when(mockPluginManager.getPlugin("Multiverse-Core")).thenReturn(core);
            when(mockPluginManager.getPermission(anyString())).thenReturn(null);

            // Make some fake folders to fool the fake MV into thinking these worlds exist
            File worldNormalFile = new File(plugin.getServerFolder(), "world");
            Util.log("Creating world-folder: " + worldNormalFile.getAbsolutePath());
            worldNormalFile.mkdirs();
            MockWorldFactory.makeNewMockWorld("world", Environment.NORMAL, WorldType.NORMAL);
            File worldNetherFile = new File(plugin.getServerFolder(), "world_nether");
            Util.log("Creating world-folder: " + worldNetherFile.getAbsolutePath());
            worldNetherFile.mkdirs();
            MockWorldFactory.makeNewMockWorld("world_nether", Environment.NETHER, WorldType.NORMAL);
            File worldSkylandsFile = new File(plugin.getServerFolder(), "world_the_end");
            Util.log("Creating world-folder: " + worldSkylandsFile.getAbsolutePath());
            worldSkylandsFile.mkdirs();
            MockWorldFactory.makeNewMockWorld("world_the_end", Environment.THE_END, WorldType.NORMAL);
            File world2File = new File(plugin.getServerFolder(), "world2");
            Util.log("Creating world-folder: " + world2File.getAbsolutePath());
            world2File.mkdirs();
            MockWorldFactory.makeNewMockWorld("world2", Environment.NORMAL, WorldType.NORMAL);

            // Initialize the Mock server.
            mockServer = mock(Server.class);
            when(mockServer.getName()).thenReturn("TestBukkit");
            Logger.getLogger("Minecraft").setParent(Util.logger);
            when(mockServer.getLogger()).thenReturn(Util.logger);
            when(mockServer.getWorldContainer()).thenReturn(worldsDirectory);
            when(plugin.getServer()).thenReturn(mockServer);
            when(core.getServer()).thenReturn(mockServer);
            when(mockServer.getPluginManager()).thenReturn(mockPluginManager);
            Answer<Player> playerAnswer = invocationOnMock -> {
                String name = invocationOnMock.getArgument(0);
                if (name == null) return null;
                return MockPlayerFactory.getOrCreateMockPlayer(name, mockServer);
            };
            when(mockServer.getPlayerExact(anyString())).thenAnswer(playerAnswer);
            when(mockServer.getOfflinePlayer(anyString())).thenAnswer(playerAnswer);
            when(mockServer.getOfflinePlayers()).thenAnswer(
                    (Answer<OfflinePlayer[]>) invocation -> MockPlayerFactory.getAllPlayers().toArray(new Player[0]));
            when(mockServer.getOnlinePlayers()).thenAnswer(
                    (Answer<Collection<Player>>) invocation -> MockPlayerFactory.getAllPlayers());
            Answer<Player> uuidPlayerAnswer = invocationOnMock -> {
                UUID uuid = invocationOnMock.getArgument(0);
                if (uuid == null) return null;
                return MockPlayerFactory.getOrCreateMockPlayer(uuid, mockServer);
            };
            doAnswer(uuidPlayerAnswer).when(mockServer).getPlayer(any(UUID.class));
            doAnswer(uuidPlayerAnswer).when(mockServer).getOfflinePlayer(any(UUID.class));

            try {
                PotionEffectType.registerPotionEffectType(mockPotionEffectType(1, "SPEED"));
                PotionEffectType.registerPotionEffectType(mockPotionEffectType(2, "SLOW"));
                PotionEffectType.registerPotionEffectType(mockPotionEffectType(3, "FAST_DIGGING"));
                PotionEffectType.registerPotionEffectType(mockPotionEffectType(4, "SLOW_DIGGING"));
                PotionEffectType.registerPotionEffectType(mockPotionEffectType(5, "INCREASE_DAMAGE"));
                PotionEffectType.registerPotionEffectType(mockPotionEffectType(6, "HEAL"));
                PotionEffectType.registerPotionEffectType(mockPotionEffectType(7, "HARM"));
                PotionEffectType.registerPotionEffectType(mockPotionEffectType(8, "JUMP"));
                PotionEffectType.registerPotionEffectType(mockPotionEffectType(9, "CONFUSION"));
                PotionEffectType.registerPotionEffectType(mockPotionEffectType(10, "REGENERATION"));
                PotionEffectType.registerPotionEffectType(mockPotionEffectType(11, "DAMAGE_RESISTANCE"));
                PotionEffectType.registerPotionEffectType(mockPotionEffectType(12, "FIRE_RESISTANCE"));
                PotionEffectType.registerPotionEffectType(mockPotionEffectType(13, "WATER_BREATHING"));
                PotionEffectType.registerPotionEffectType(mockPotionEffectType(14, "INVISIBILITY"));
                PotionEffectType.registerPotionEffectType(mockPotionEffectType(15, "BLINDNESS"));
                PotionEffectType.registerPotionEffectType(mockPotionEffectType(16, "NIGHT_VISION"));
            } catch (IllegalArgumentException ignore) {
                // Already registered in this context.
            }

            // Give the server some worlds
            when(mockServer.getWorld(anyString())).thenAnswer(new Answer<World>() {
                public World answer(InvocationOnMock invocation) throws Throwable {
                    String arg;
                    try {
                        arg = (String) invocation.getArguments()[0];
                    } catch (Exception e) {
                        return null;
                    }
                    return MockWorldFactory.getWorld(arg);
                }
            });

            when(mockServer.getWorld(any(UUID.class))).thenAnswer(new Answer<World>() {
                @Override
                public World answer(InvocationOnMock invocation) throws Throwable {
                    UUID arg;
                    try {
                        arg = (UUID) invocation.getArguments()[0];
                    } catch (Exception e) {
                        return null;
                    }
                    return MockWorldFactory.getWorld(arg);
                }
            });

            when(mockServer.getWorlds()).thenAnswer(new Answer<List<World>>() {
                public List<World> answer(InvocationOnMock invocation) throws Throwable {
                    return MockWorldFactory.getWorlds();
                }
            });



            when(mockServer.createWorld(Matchers.isA(WorldCreator.class))).thenAnswer(
                    new Answer<World>() {
                        public World answer(InvocationOnMock invocation) throws Throwable {
                            WorldCreator arg;
                            try {
                                arg = (WorldCreator) invocation.getArguments()[0];
                            } catch (Exception e) {
                                return null;
                            }
                            // Add special case for creating null worlds.
                            // Not sure I like doing it this way, but this is a special case
                            if (arg.name().equalsIgnoreCase("nullworld")) {
                                return MockWorldFactory.makeNewNullMockWorld(arg.name(), arg.environment(), arg.type());
                            }
                            return MockWorldFactory.makeNewMockWorld(arg.name(), arg.environment(), arg.type());
                        }
                    });

            when(mockServer.unloadWorld(anyString(), anyBoolean())).thenReturn(true);

            // add mock scheduler
            BukkitScheduler mockScheduler = mock(BukkitScheduler.class);
            when(mockScheduler.scheduleSyncDelayedTask(any(Plugin.class), any(Runnable.class), anyLong())).
                    thenAnswer(new Answer<Integer>() {
                        public Integer answer(InvocationOnMock invocation) throws Throwable {
                            Runnable arg;
                            try {
                                arg = (Runnable) invocation.getArguments()[1];
                            } catch (Exception e) {
                                return null;
                            }
                            arg.run();
                            return null;
                        }
                    });
            when(mockScheduler.scheduleSyncDelayedTask(any(Plugin.class), any(Runnable.class))).
                    thenAnswer(new Answer<Integer>() {
                        public Integer answer(InvocationOnMock invocation) throws Throwable {
                            Runnable arg;
                            try {
                                arg = (Runnable) invocation.getArguments()[1];
                            } catch (Exception e) {
                                return null;
                            }
                            arg.run();
                            return null;
                        }
                    });
            when(mockServer.getScheduler()).thenReturn(mockScheduler);

            ItemFactory itemFactory = MockItemMeta.mockItemFactory();
            when(mockServer.getItemFactory()).thenReturn(itemFactory);


            UnsafeValues unsafeValues = mock(UnsafeValues.class);
            doAnswer(i -> Material.getMaterial(i.getArgument(0))).when(unsafeValues).getMaterial(any(), anyInt());
            when(mockServer.getUnsafe()).thenReturn(unsafeValues);

            // Set InventoriesListener
            InventoriesListener il = PowerMockito.spy(new InventoriesListener(plugin));
            Field inventoriesListenerField = MultiverseInventories.class.getDeclaredField("inventoriesListener");
            inventoriesListenerField.setAccessible(true);
            inventoriesListenerField.set(plugin, il);

            // Set Core
            Field coreField = MultiverseInventories.class.getDeclaredField("core");
            coreField.setAccessible(true);
            coreField.set(plugin, core);

            // Set server
            Field serverfield = JavaPlugin.class.getDeclaredField("server");
            serverfield.setAccessible(true);
            serverfield.set(plugin, mockServer);

            // Set worldManager
            WorldManager wm = PowerMockito.spy(new WorldManager(core));
            Field worldmanagerfield = MultiverseCore.class.getDeclaredField("worldManager");
            worldmanagerfield.setAccessible(true);
            worldmanagerfield.set(core, wm);

            // Set playerListener
            MVPlayerListener pl = PowerMockito.spy(new MVPlayerListener(core));
            Field playerlistenerfield = MultiverseCore.class.getDeclaredField("playerListener");
            playerlistenerfield.setAccessible(true);
            playerlistenerfield.set(core, pl);

            // Set entityListener
            MVEntityListener el = PowerMockito.spy(new MVEntityListener(core));
            Field entitylistenerfield = MultiverseCore.class.getDeclaredField("entityListener");
            entitylistenerfield.setAccessible(true);
            entitylistenerfield.set(core, el);

            // Set weatherListener
            MVWeatherListener wl = PowerMockito.spy(new MVWeatherListener(core));
            Field weatherlistenerfield = MultiverseCore.class.getDeclaredField("weatherListener");
            weatherlistenerfield.setAccessible(true);
            weatherlistenerfield.set(core, wl);

            // Init our command sender
            final Logger commandSenderLogger = Logger.getLogger("CommandSender");
            commandSenderLogger.setParent(Util.logger);
            commandSender = mock(CommandSender.class);
            doAnswer(new Answer<Void>() {
                public Void answer(InvocationOnMock invocation) throws Throwable {
                    commandSenderLogger.info(ChatColor.stripColor((String) invocation.getArguments()[0]));
                    return null;
                }
            }).when(commandSender).sendMessage(anyString());
            when(commandSender.getServer()).thenReturn(mockServer);
            when(commandSender.getName()).thenReturn("MockCommandSender");
            when(commandSender.isPermissionSet(anyString())).thenReturn(true);
            when(commandSender.isPermissionSet(Matchers.isA(Permission.class))).thenReturn(true);
            when(commandSender.hasPermission(anyString())).thenReturn(true);
            when(commandSender.hasPermission(Matchers.isA(Permission.class))).thenReturn(true);
            when(commandSender.addAttachment(plugin)).thenReturn(null);
            when(commandSender.isOp()).thenReturn(true);

            Field singletonServerField = Bukkit.class.getDeclaredField("server");
            singletonServerField.setAccessible(true);
            singletonServerField.set(null, mockServer);

            // Load Multiverse Core
            core.onLoad();
            plugin.onLoad();

            // Enable it.
            core.onEnable();
            plugin.onEnable();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean tearDown() {
        /*
        List<MultiverseWorld> worlds = new ArrayList<MultiverseWorld>(core.getMVWorldManager()
                .getMVWorlds());
        for (MultiverseWorld world : worlds) {
            core.getMVWorldManager().deleteWorld(world.getName());
        }
        */

        Server maybeNullServer = getServer();
        PluginManager maybeNullPluginManager = maybeNullServer.getPluginManager();
        Plugin plugin = maybeNullPluginManager.getPlugin("Multiverse-Inventories");
        //Plugin plugin = getServer().getPluginManager().getPlugin("Multiverse-Inventories");
        MultiverseInventories inventories = (MultiverseInventories) plugin;
        inventories.onDisable();

        MockWorldFactory.clearWorlds();

        plugin = getServer().getPluginManager().getPlugin("Multiverse-Core");
        MultiverseCore core = (MultiverseCore) plugin;
        core.onDisable();

        try {
            Field serverField = Bukkit.class.getDeclaredField("server");
            serverField.setAccessible(true);
            serverField.set(Class.forName("org.bukkit.Bukkit"), null);
        } catch (Exception e) {
            Util.log(Level.SEVERE,
                    "Error while trying to unregister the server from Bukkit. Has Bukkit changed?");
            e.printStackTrace();
            Assert.fail(e.getMessage());
            return false;
        }

        // Dont remove so that we can see the data result after the test.
        // FileUtils.deleteFolder(serverDirectory);

        return true;
    }

    public MultiverseInventories getPlugin() {
        return this.plugin;
    }

    public Server getServer() {
        return this.mockServer;
    }

    public CommandSender getCommandSender() {
        return commandSender;
    }

    private PotionEffectType mockPotionEffectType(int id, String name) throws Exception {
        PotionEffectType potionEffectType = mock(PotionEffectType.class);
        Field idField = PotionEffectType.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(potionEffectType, id);
        when(potionEffectType.getId()).thenReturn(id);
        when(potionEffectType.getName()).thenReturn(name);
        return potionEffectType;
    }
}
