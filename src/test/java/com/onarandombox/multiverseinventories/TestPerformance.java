package com.onarandombox.multiverseinventories;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.multiverseinventories.event.ShareHandlingEvent;
import com.onarandombox.multiverseinventories.profile.PlayerProfile;
import com.onarandombox.multiverseinventories.share.Sharable;
import com.onarandombox.multiverseinventories.share.Sharables;
import com.onarandombox.multiverseinventories.util.TestInstanceCreator;
import junit.framework.Assert;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
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
@PrepareForTest({MultiverseInventories.class, PluginDescriptionFile.class, JavaPluginLoader.class, MultiverseCore.class})
@PowerMockIgnore("javax.script.*")
@Ignore
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

    private Player getPreparedPlayer() {
        Player player = inventories.getServer().getPlayerExact("dumptruckman");

        Map<Integer, ItemStack> fillerItems = new HashMap<Integer, ItemStack>();
        for (int i = 0; i < PlayerStats.INVENTORY_SIZE; i++) {
            ItemStack item = new ItemStack(Material.STONE_BRICKS, 64);
            Enchantment mockEnchantment = PowerMockito.mock(Enchantment.class);
            when(mockEnchantment.getName()).thenReturn("Protection");
            item.addUnsafeEnchantment(mockEnchantment, 3);
            mockEnchantment = PowerMockito.mock(Enchantment.class);
            when(mockEnchantment.getName()).thenReturn("Respiration");
            item.addUnsafeEnchantment(mockEnchantment, 3);
            mockEnchantment = PowerMockito.mock(Enchantment.class);
            when(mockEnchantment.getName()).thenReturn("Smite");
            item.addUnsafeEnchantment(mockEnchantment, 3);
            fillerItems.put(i, item);
        }
        addToInventory(player.getInventory(), fillerItems);
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 50, 3));
        player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 50, 3));
        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 50, 3));
        return player;
    }

    @Test
    public void testOverallWorldChangePerformance() {
        int numTests = 100;

        // Initialize a fake command
        Command mockCommand = mock(Command.class);
        when(mockCommand.getName()).thenReturn("mvinv");

        WorldGroup newGroup = inventories.getGroupManager().newEmptyGroup("test");
        newGroup.getShares().mergeShares(Sharables.allOf());
        newGroup.addWorld("world2");
        inventories.getGroupManager().updateGroup(newGroup);

        newGroup = inventories.getGroupManager().newEmptyGroup("test2");
        newGroup.getShares().mergeShares(Sharables.allOf());
        newGroup.addWorld("world");
        inventories.getGroupManager().updateGroup(newGroup);
        newGroup = inventories.getGroupManager().newEmptyGroup("test3");
        newGroup.getShares().mergeShares(Sharables.allOf());
        newGroup.addWorld("world");
        inventories.getGroupManager().updateGroup(newGroup);
        newGroup = inventories.getGroupManager().newEmptyGroup("test4");
        newGroup.getShares().mergeShares(Sharables.allOf());
        newGroup.addWorld("world");
        inventories.getGroupManager().updateGroup(newGroup);
        newGroup = inventories.getGroupManager().newEmptyGroup("test5");
        newGroup.getShares().mergeShares(Sharables.allOf());
        newGroup.addWorld("world");
        inventories.getGroupManager().updateGroup(newGroup);

        // Verify removal
        Assert.assertTrue(!inventories.getGroupManager().getDefaultGroup().getWorlds().contains("world2"));
        String[] cmdArgs = new String[]{"info", "default"};
        inventories.onCommand(mockCommandSender, mockCommand, "", cmdArgs);

        Player player = getPreparedPlayer();

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
            changeWorld(player, "world2", "world");
        }
        double cachedAverage = (total / numTests);

        timeTaken = new double[numTests];
        total = 0;
        for (int i = 0; i < numTests; i++) {
            startTime = System.nanoTime();
            changeWorld(player, "world", "world2");
            endTime = System.nanoTime();
            timeTaken[i] = (endTime - startTime) / 1000000D;
            total += timeTaken[i];
            changeWorld(player, "world2", "world");
            inventories.reloadConfig();
        }
        double uncachedAverage = (total / numTests);

        timeTaken = new double[numTests];
        total = 0;
        for (int i = 0; i < numTests; i++) {
            startTime = System.nanoTime();
            new WorldChangeShareHandler(inventories, player, "world", "world2");
            endTime = System.nanoTime();
            timeTaken[i] = (endTime - startTime) / 1000000D;
            total += timeTaken[i];
        }
        double groupCollectionAverage = (total / numTests);

        timeTaken = new double[numTests];
        total = 0;
        ShareHandlingEvent event = new WorldChangeShareHandler(inventories, player, "world", "world2").createEvent();
        for (int i = 0; i < numTests; i++) {
            startTime = System.nanoTime();
            ShareHandlingUpdater.updateProfile(inventories, player, event.getWriteProfiles().get(0));
            endTime = System.nanoTime();
            timeTaken[i] = (endTime - startTime) / 1000000D;
            total += timeTaken[i];
        }
        double profileUpdateAverage = (total / numTests);

        timeTaken = new double[numTests];
        total = 0;
        event = new WorldChangeShareHandler(inventories, player, "world", "world2").createEvent();
        for (int i = 0; i < numTests; i++) {
            startTime = System.nanoTime();
            ShareHandlingUpdater.updatePlayer(inventories, player, event.getReadProfiles().get(0));
            endTime = System.nanoTime();
            timeTaken[i] = (endTime - startTime) / 1000000D;
            total += timeTaken[i];
        }
        double playerUpdateAverage = (total / numTests);

        System.out.println("Average Time for group collection: " + groupCollectionAverage + "ms");
        System.out.println("Average Time for cached world change: " + cachedAverage + "ms");
        System.out.println("Average Time for cached profile update: " + profileUpdateAverage + "ms");
        System.out.println("Average Time for cached player update: " + playerUpdateAverage + "ms");
        System.out.println("Average Time for uncached world change: " + uncachedAverage + "ms");
    }

    @Test
    public void testIndividualSharesPerformance() {
        int numTests = 100;

        // Initialize a fake command
        Command mockCommand = mock(Command.class);
        when(mockCommand.getName()).thenReturn("mvinv");

        WorldGroup newGroup = inventories.getGroupManager().newEmptyGroup("test");
        newGroup.getShares().mergeShares(Sharables.allOf());
        newGroup.addWorld("world2");
        inventories.getGroupManager().updateGroup(newGroup);

        // Verify removal
        Assert.assertTrue(!inventories.getGroupManager().getDefaultGroup().getWorlds().contains("world2"));
        String[] cmdArgs = new String[]{"info", "default"};
        inventories.onCommand(mockCommandSender, mockCommand, "", cmdArgs);

        Player player = getPreparedPlayer();
        changeWorld(player, "world", "world2");
        changeWorld(player, "world2", "world");

        Map<Sharable, Double> averageUpdatePlayer = new HashMap<Sharable, Double>();
        Map<Sharable, Double> averageUpdateProfile = new HashMap<Sharable, Double>();
        PlayerProfile profile = inventories.getGroupManager().getDefaultGroup().getGroupProfileContainer().getPlayerData(player);
        for (Sharable share : Sharables.all()) {
            if (share.isOptional()) {
                continue;
            }
            long startTime = 0;
            long endTime = 0;
            double[] timeTaken = new double[numTests];
            double total = 0;
            for (int i = 0; i < numTests; i++) {
                startTime = System.nanoTime();
                share.getHandler().updateProfile(profile, player);
                endTime = System.nanoTime();
                timeTaken[i] = (endTime - startTime) / 1000000D;
                total += timeTaken[i];
            }
            averageUpdateProfile.put(share, (total / numTests));
            timeTaken = new double[numTests];
            total = 0;
            for (int i = 0; i < numTests; i++) {
                startTime = System.nanoTime();
                share.getHandler().updatePlayer(player, profile);
                endTime = System.nanoTime();
                timeTaken[i] = (endTime - startTime) / 1000000D;
                total += timeTaken[i];
            }
            averageUpdatePlayer.put(share, (total / numTests));
        }

        for (Sharable share : Sharables.all()) {
            System.out.println("Average Time for " + share.getNames()[0] + ".updatePlayer(): " + averageUpdatePlayer.get(share));
            System.out.println("Average Time for " + share.getNames()[0] + ".updateProfile(): " + averageUpdateProfile.get(share));
        }
    }
    /*
    @Test
    public void testLargeGroupCollectionPerformance() {
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

        Player player = getPreparedPlayer();

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
            changeWorld(player, "world2", "world");
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
            changeWorld(player, "world2", "world");
            cmdArgs = new String[]{"reload"};
            inventories.onCommand(mockCommandSender, mockCommand, "", cmdArgs);
        }
        System.out.println("Average Time for cached world change: " + average + "ms");
        System.out.println("Average Time for uncached world change: " + (total / numTests) + "ms");
    }
    */
}
