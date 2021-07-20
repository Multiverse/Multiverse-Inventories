package com.onarandombox.multiverseinventories;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.multiverseinventories.profile.GlobalProfile;
import com.onarandombox.multiverseinventories.util.MockPlayerFactory;
import com.onarandombox.multiverseinventories.util.TestInstanceCreator;
import junit.framework.Assert;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestPlayerNameChange {
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
        Location oldLocation = player.getLocation();
        Location location = new Location(mockServer.getWorld(toWorld), 0.0, 70.0, 0.0);
        player.teleport(location);
        Assert.assertEquals(location, player.getLocation());
        listener.playerTeleport(new PlayerTeleportEvent(player, oldLocation, location));
        listener.playerChangedWorld(new PlayerChangedWorldEvent(player, mockServer.getWorld(fromWorld)));
    }

    public void addToInventory(PlayerInventory inventory, Map<Integer, ItemStack> items) {
        for (Map.Entry<Integer, ItemStack> invEntry : items.entrySet()) {
            inventory.setItem(invEntry.getKey(), invEntry.getValue());
        }
    }

    public static Map<Integer, ItemStack> getFillerInv() {
        Map<Integer, ItemStack> fillerItems = new HashMap<Integer, ItemStack>();
        fillerItems.put(3, new ItemStack(Material.BOW, 1));
        fillerItems.put(13, new ItemStack(Material.DIRT, 64));
        fillerItems.put(36, new ItemStack(Material.IRON_HELMET, 1));
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK, 1);
        fillerItems.put(1, book);
        ItemStack leather = new ItemStack(Material.LEATHER_BOOTS, 1);
        fillerItems.put(2, leather);
        return fillerItems;
    }

    public static Map<Integer, ItemStack> getFillerInv2() {
        Map<Integer, ItemStack> fillerItems = new HashMap<Integer, ItemStack>();
        fillerItems.put(3, new ItemStack(Material.DIAMOND_PICKAXE, 1));
        fillerItems.put(13, new ItemStack(Material.STONE, 64));
        fillerItems.put(36, new ItemStack(Material.IRON_HELMET, 1));
        ItemStack book = new ItemStack(Material.BOOK, 1);
        fillerItems.put(1, book);
        ItemStack leather = new ItemStack(Material.GOLDEN_BOOTS, 1);
        fillerItems.put(2, leather);
        return fillerItems;
    }

    public void doPlayerJoin(Player player) throws UnknownHostException {
        this.listener.playerPreLogin(new AsyncPlayerPreLoginEvent(player.getName(),
                InetAddress.getLocalHost(), player.getUniqueId()));
        this.listener.playerJoin(new PlayerJoinEvent(player, null));
    }

    public void doPlayerQuit(Player player) {
        this.listener.playerQuit(new PlayerQuitEvent(player, null));
    }

    public void changePlayerName(Player player, String targetName) {
        Assert.assertNotNull(player);

        String oldName = player.getName();
        UUID oldUUID = player.getUniqueId();

        MockPlayerFactory.changeName(player, targetName);

        String newName = player.getName();
        UUID newUUID = player.getUniqueId();

        Assert.assertEquals(oldName, "dumptruckman");
        Assert.assertEquals(newName, "benwoo1110");
        Assert.assertEquals(oldUUID, newUUID);
    }

    public GlobalProfile getAndCheckGlobalProfile(Player player) {
        GlobalProfile globalProfile = this.inventories.getData().getGlobalProfile(player.getName(), player.getUniqueId());
        Assert.assertEquals(globalProfile.getLastKnownName(), player.getName());
        Assert.assertEquals(globalProfile.getPlayerName(), player.getName());
        Assert.assertEquals(globalProfile.getPlayerUUID(), player.getUniqueId());

        return globalProfile;
    }

    /**
     * Ensure that player data is migrated on name change.
     */
    @Test
    public void TestPlayerNameChangeMigration() throws UnknownHostException {
        // Initialize a fake command
        Command mockCommand = mock(Command.class);
        when(mockCommand.getName()).thenReturn("mvinv");

        Command mockCoreCommand = mock(Command.class);
        when(mockCoreCommand.getName()).thenReturn("mv");

        // Assert debug mode is off
        Assert.assertEquals(0, this.inventories.getMVIConfig().getGlobalDebug());

        // Send the debug command.
        String[] cmdArgs = new String[]{"debug", "3"};
        this.inventories.onCommand(this.mockCommandSender, mockCoreCommand, "", cmdArgs);

        cmdArgs = new String[]{"reload"};
        inventories.onCommand(mockCommandSender, mockCommand, "", cmdArgs);

        // Assert debug mode is on
        Assert.assertEquals(3, inventories.getMVIConfig().getGlobalDebug());

        // Getting player
        Player player = this.mockServer.getPlayerExact("dumptruckman");
        Assert.assertNotNull(player);

        doPlayerJoin(player);
        GlobalProfile globalProfile = getAndCheckGlobalProfile(player);

        // Set inv for world
        addToInventory(player.getInventory(), getFillerInv());
        String worldInvData = player.getInventory().toString();

        changeWorld(player, "world", "world2");
        Assert.assertNotSame(player.getInventory().toString(), worldInvData);

        // Set inv for world2
        addToInventory(player.getInventory(), getFillerInv2());
        String world2InvData = player.getInventory().toString();

        doPlayerQuit(player);
        changePlayerName(player, "benwoo1110");
        doPlayerJoin(player);

        globalProfile = getAndCheckGlobalProfile(player);
        Assert.assertNotSame(player.getInventory().toString(), worldInvData);
        Assert.assertEquals(player.getInventory().toString(), world2InvData);

        // Go back to world_nether which is in group default
        // i.e. Should be the same inv as world.
        changeWorld(player, "world2", "world_nether");
        Assert.assertNotSame(player.getInventory().toString(), world2InvData);
        Assert.assertEquals(player.getInventory().toString(), worldInvData);

        // Go back to world
        changeWorld(player, "world_nether", "world");
        Assert.assertNotSame(player.getInventory().toString(), world2InvData);
        Assert.assertEquals(player.getInventory().toString(), worldInvData);
    }
}
