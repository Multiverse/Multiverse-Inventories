package com.onarandombox.multiverseinventories;

import com.onarandombox.MultiverseAdventure.event.MVAResetFinishedEvent;
import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.multiverseinventories.util.TestInstanceCreator;
import junit.framework.Assert;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
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

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({MultiverseInventories.class, PluginDescriptionFile.class, JavaPluginLoader.class, MultiverseCore.class, AdventureListener.class})
@PowerMockIgnore("javax.script.*")
@Ignore
public class TestResetWorld {
    TestInstanceCreator creator;
    Server mockServer;
    CommandSender mockCommandSender;
    MultiverseInventories inventories;
    InventoriesListener listener;
    AdventureListener aListener;

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
        Field field = MultiverseInventories.class.getDeclaredField("adventureListener");
        field.setAccessible(true);
        aListener = new AdventureListener(inventories);
        field.set(inventories, aListener);
        field = MultiverseInventories.class.getDeclaredField("inventoriesListener");
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
    public void testWorldReset() {

        // Initialize a fake command
        Command mockCommand = mock(Command.class);
        when(mockCommand.getName()).thenReturn("mvinv");

        Command mockCoreCommand = mock(Command.class);
        when(mockCoreCommand.getName()).thenReturn("mv");

        // Assert debug mode is off
        Assert.assertEquals(0, inventories.getMVIConfig().getGlobalDebug());

        // Send the debug command.
        String[] cmdArgs = new String[]{"debug", "3"};
        inventories.onCommand(mockCommandSender, mockCoreCommand, "", cmdArgs);

        // remove world2 from default group
        cmdArgs = new String[]{"rmworld", "world2", "default"};
        inventories.onCommand(mockCommandSender, mockCommand, "", cmdArgs);

        Player player = inventories.getServer().getPlayerExact("dumptruckman");

        changeWorld(player, "world", "world2");
        Map<Integer, ItemStack> fillerItems = new HashMap<Integer, ItemStack>();
        fillerItems.put(3, new ItemStack(Material.BOW, 1));
        fillerItems.put(13, new ItemStack(Material.DIRT, 64));
        fillerItems.put(36, new ItemStack(Material.IRON_HELMET, 1));
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK, 1);
        BookMeta bookMeta = (BookMeta) book.getItemMeta();
        bookMeta.setAuthor("dumptruckman");
        bookMeta.setPages("This is my freaking", "book", "man");
        bookMeta.setDisplayName("Super Book");
        book.setItemMeta(bookMeta);
        fillerItems.put(1, book);
        ItemStack leather = new ItemStack(Material.LEATHER_BOOTS, 1);
        LeatherArmorMeta leatherMeta = (LeatherArmorMeta) leather.getItemMeta();
        leatherMeta.setColor(Color.PURPLE);
        leatherMeta.setLore(Arrays.asList("Aww fuck yeah", "Lore"));
        leather.setItemMeta(leatherMeta);
        fillerItems.put(2, leather);
        addToInventory(player.getInventory(), fillerItems);
        String originalInventory = player.getInventory().toString();

        changeWorld(player, "world2", "world");
        String newInventory = player.getInventory().toString();

        Assert.assertNotSame(originalInventory, newInventory);

        aListener.worldReset(new MVAResetFinishedEvent("world2"));
        changeWorld(player, "world", "world2");
        String inventoryAfterReset = player.getInventory().toString();

        Assert.assertEquals(newInventory, inventoryAfterReset);
    }
}
