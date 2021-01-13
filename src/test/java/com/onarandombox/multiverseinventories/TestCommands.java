/******************************************************************************
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.                       *
 * Multiverse 2 is licensed under the BSD License.                            *
 * For more information please check the README.md file included              *
 * with this project.                                                         *
 ******************************************************************************/

package com.onarandombox.multiverseinventories;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.multiverseinventories.share.Sharables;
import com.onarandombox.multiverseinventories.util.TestInstanceCreator;
import junit.framework.Assert;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({MultiverseInventories.class, PluginDescriptionFile.class, JavaPluginLoader.class, MultiverseCore.class})
@PowerMockIgnore("javax.script.*")
public class TestCommands {
    TestInstanceCreator creator;
    Server mockServer;
    CommandSender mockCommandSender;

    @Before
    public void setUp() throws Exception {
        creator = new TestInstanceCreator();
        assertTrue(creator.setUp());
        mockServer = creator.getServer();
        mockCommandSender = creator.getCommandSender();
    }

    @After
    public void tearDown() throws Exception {
        creator.tearDown();
    }

    @Test
    public void testDebugReload() {
        // Pull a core instance from the server.
        Plugin plugin = mockServer.getPluginManager().getPlugin("Multiverse-Inventories");
        MultiverseInventories inventories = (MultiverseInventories) plugin;

        // Make sure Core is not null
        assertNotNull(plugin);

        // Make sure Core is enabled
        assertTrue(plugin.isEnabled());

        // Make a fake server folder to fool MV into thinking a world folder exists.
        File serverDirectory = new File(creator.getPlugin().getServerFolder(), "world");
        serverDirectory.mkdirs();

        // Assert debug mode is off
        Assert.assertEquals(0, inventories.getMVIConfig().getGlobalDebug());

        // Send the debug command.
        assertTrue(creator.dispatch(mockCommandSender, "mv debug 3"));

        Assert.assertEquals(3, inventories.getMVIConfig().getGlobalDebug());

        // Send the debug command.
        inventories.reloadConfig();

        Assert.assertEquals(3, inventories.getMVIConfig().getGlobalDebug());
    }

    @Test
    public void testInfoCommand() {
        // Pull a core instance from the server.
        Plugin plugin = mockServer.getPluginManager().getPlugin("Multiverse-Inventories");
        MultiverseInventories inventories = (MultiverseInventories) plugin;

        // Make sure Core is not null
        assertNotNull(plugin);

        // Make sure Core is enabled
        assertTrue(plugin.isEnabled());
        // Send the debug command.
        assertTrue(creator.dispatch(mockCommandSender, "mvinv info default"));
    }

    @Test
    public void testToggleCommand() {
        // Pull a core instance from the server.
        Plugin plugin = mockServer.getPluginManager().getPlugin("Multiverse-Inventories");
        MultiverseInventories inventories = (MultiverseInventories) plugin;

        // Make sure Core is not null
        assertNotNull(plugin);

        // Make sure Core is enabled
        assertTrue(plugin.isEnabled());

        Assert.assertFalse(inventories.getMVIConfig().getOptionalShares().contains(Sharables.ECONOMY));
        // Test economy optional share
        assertTrue(creator.dispatch(mockCommandSender, "mvinv toggle economy"));
        Assert.assertTrue(inventories.getMVIConfig().getOptionalShares().contains(Sharables.ECONOMY));
        inventories.reloadConfig();
        Assert.assertTrue(inventories.getMVIConfig().getOptionalShares().contains(Sharables.ECONOMY));
        assertTrue(creator.dispatch(mockCommandSender, "mvinv toggle economy"));
        Assert.assertFalse(inventories.getMVIConfig().getOptionalShares().contains(Sharables.ECONOMY));
    }

    @Test
    public void testGroupNoWorlds() {
        // Pull a core instance from the server.
        Plugin plugin = mockServer.getPluginManager().getPlugin("Multiverse-Inventories");
        MultiverseInventories inventories = (MultiverseInventories) plugin;

        // Make sure Core is not null
        assertNotNull(plugin);

        // Make sure Core is enabled
        assertTrue(plugin.isEnabled());


        assertTrue(creator.dispatch(mockCommandSender, "mvinv removeworld world default"));
        assertTrue(creator.dispatch(mockCommandSender, "mvinv removeworld world_nether default"));
        assertTrue(creator.dispatch(mockCommandSender, "mvinv removeworld world_the_end default"));

        inventories.reloadConfig();

        assertTrue(creator.dispatch(mockCommandSender, "mvinv info default"));
    }
}
