/******************************************************************************
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.                       *
 * Multiverse 2 is licensed under the BSD License.                            *
 * For more information please check the README.md file included              *
 * with this project.                                                         *
 ******************************************************************************/

package org.mvplugins.multiverse.inventories;

import org.mvplugins.multiverse.inventories.share.Sharables;
import org.mvplugins.multiverse.inventories.util.TestInstanceCreator;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

        // Initialize a fake command
        Command mockCommand = mock(Command.class);
        when(mockCommand.getName()).thenReturn("mvinv");

        Command mockCoreCommand = mock(Command.class);
        when(mockCoreCommand.getName()).thenReturn("mv");

        // Assert debug mode is off
        assertEquals(0, inventories.getMVIConfig().getGlobalDebug());

        // Send the debug command.
        String[] debugArgs = new String[]{"debug", "3"};
        plugin.onCommand(mockCommandSender, mockCoreCommand, "", debugArgs);

        assertEquals(3, inventories.getMVIConfig().getGlobalDebug());

        // Send the debug command.
        String[] reloadArgs = new String[] { "reload" };
        plugin.onCommand(mockCommandSender, mockCommand, "", reloadArgs);

        assertEquals(3, inventories.getMVIConfig().getGlobalDebug());
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

        // Initialize a fake command
        Command mockCommand = mock(Command.class);
        when(mockCommand.getName()).thenReturn("mvinv");

        // Send the debug command.
        String[] debugArgs = new String[]{ "info", "default"};
        plugin.onCommand(mockCommandSender, mockCommand, "", debugArgs);
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

        // Initialize a fake command
        Command mockCommand = mock(Command.class);
        when(mockCommand.getName()).thenReturn("mvinv");

        assertFalse(inventories.getMVIConfig().getOptionalShares().contains(Sharables.ECONOMY));
        // Send the debug command.
        String[] cmdArgs = new String[]{ "toggle", "economy" };
        plugin.onCommand(mockCommandSender, mockCommand, "", cmdArgs);
        assertTrue(inventories.getMVIConfig().getOptionalShares().contains(Sharables.ECONOMY));
        cmdArgs = new String[]{ "reload" };
        plugin.onCommand(mockCommandSender, mockCommand, "", cmdArgs);
        assertTrue(inventories.getMVIConfig().getOptionalShares().contains(Sharables.ECONOMY));
        cmdArgs = new String[]{ "toggle", "economy" };
        plugin.onCommand(mockCommandSender, mockCommand, "", cmdArgs);
        assertFalse(inventories.getMVIConfig().getOptionalShares().contains(Sharables.ECONOMY));
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

        // Initialize a fake command
        Command mockCommand = mock(Command.class);
        when(mockCommand.getName()).thenReturn("mvinv");

        String[] cmdArgs = new String[]{ "rmworld", "world", "default" };
        plugin.onCommand(mockCommandSender, mockCommand, "", cmdArgs);
        cmdArgs = new String[]{ "rmworld", "world_nether", "default" };
        plugin.onCommand(mockCommandSender, mockCommand, "", cmdArgs);
        cmdArgs = new String[]{ "rmworld", "world_the_end", "default" };
        plugin.onCommand(mockCommandSender, mockCommand, "", cmdArgs);

        cmdArgs = new String[]{ "reload" };
        plugin.onCommand(mockCommandSender, mockCommand, "", cmdArgs);

        cmdArgs = new String[]{ "info", "default" };
        plugin.onCommand(mockCommandSender, mockCommand, "", cmdArgs);
    }
}
