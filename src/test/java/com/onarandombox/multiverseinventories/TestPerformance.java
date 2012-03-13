package com.onarandombox.multiverseinventories;

import com.onarandombox.multiverseinventories.api.PlayerStats;
import com.onarandombox.multiverseinventories.api.profile.WorldGroupProfile;
import com.onarandombox.multiverseinventories.share.Sharables;
import com.onarandombox.multiverseinventories.util.TestInstanceCreator;
import junit.framework.Assert;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({MultiverseInventories.class, PluginDescriptionFile.class})
public class TestPerformance {
    TestInstanceCreator creator;
    Server mockServer;
    CommandSender mockCommandSender;
    MultiverseInventories inventories;
    InventoriesListener listener;

    @Before
    public void setUp() throws Exception {
        creator = new TestInstanceCreator();
        assertTrue(creator.setUp());
        mockServer = creator.getServer();
        mockCommandSender = creator.getCommandSender();
        // Pull a core instance from the server.
        Plugin plugin = mockServer.getPluginManager().getPlugin("Multiverse-Inventories");
        // Make sure Core is not null
        assertNotNull(plugin);
        inventories = (MultiverseInventories) plugin;
        Field field = MultiverseInventories.class.getDeclaredField("inventoriesListener");
        field.setAccessible(true);
        listener = (InventoriesListener) field.get(inventories);
        // Make sure Core is enabled
        assertTrue(inventories.isEnabled());


    }

    @After
    public void tearDown() throws Exception {
        creator.tearDown();
    }

    public void changeWorld(Player player, String fromWorld, String toWorld) {
        Location location = new Location(mockServer.getWorld(toWorld), 0.0, 70.0, 0.0);
        player.teleport(location);
        Assert.assertEquals(location, player.getLocation());
        listener.playerChangedWorld(new PlayerChangedWorldEvent(player, mockServer.getWorld(fromWorld)));
    }

    public void addToInventory(PlayerInventory inventory, Map<Integer, ItemStack> items) {
        for (Map.Entry<Integer, ItemStack> invEntry : items.entrySet()) {
            inventory.setItem(invEntry.getKey(), invEntry.getValue());
        }
    }

    @Test
    public void testSharableAPI() {
        
        int numTests = 100;

        // Initialize a fake command
        Command mockCommand = mock(Command.class);
        when(mockCommand.getName()).thenReturn("mvinv");

        WorldGroupProfile newGroup = inventories.getGroupManager().newEmptyGroup("test");
        newGroup.getShares().mergeShares(Sharables.allOf());
        newGroup.addWorld("world2");
        inventories.getGroupManager().addGroup(newGroup, true);

        // Verify removal
        Assert.assertTrue(!inventories.getGroupManager().getDefaultGroup().getWorlds().contains("world2"));
        String[] cmdArgs = new String[]{"info", "default"};
        inventories.onCommand(mockCommandSender, mockCommand, "", cmdArgs);

        Player player = inventories.getServer().getPlayer("dumptruckman");

        Map<Integer, ItemStack> fillerItems = new HashMap<Integer, ItemStack>();
        for (int i = 0; i < PlayerStats.INVENTORY_SIZE; i++) {
            fillerItems.put(i, new ItemStack(Material.DIRT, 64));
        }
        addToInventory(player.getInventory(), fillerItems);

        long startTime = 0;
        long endTime = 0;
        double[] timeTaken = new double[numTests];
        double total = 0;

        for (int i = 0; i < numTests; i++) {
            startTime = System.nanoTime();
            changeWorld(player, "world", "world2");
            endTime = System.nanoTime();
            timeTaken[i] = (endTime - startTime) / 1000000D;
            total += timeTaken[i];
        }
        double average = (total / numTests);


        timeTaken = new double[numTests];
        total = 0;
        for (int i = 0; i < numTests; i++) {
            startTime = System.nanoTime();
            changeWorld(player, "world", "world2");
            endTime = System.nanoTime();
            timeTaken[i] = (endTime - startTime) / 1000000D;
            total += timeTaken[i];
            cmdArgs = new String[]{"reload"};
            inventories.onCommand(mockCommandSender, mockCommand, "", cmdArgs);
        }
        System.out.println("Average Time for cached world change: " + average + "ms");
        System.out.println("Average Time for uncached world change: " + (total / numTests) + "ms");
    }

}