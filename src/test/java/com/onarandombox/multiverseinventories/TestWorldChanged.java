package com.onarandombox.multiverseinventories;

import com.dumptruckman.bukkit.configuration.json.JsonConfiguration;
import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.multiverseinventories.profile.container.ContainerType;
import com.onarandombox.multiverseinventories.share.Sharables;
import com.onarandombox.multiverseinventories.share.Shares;
import com.onarandombox.multiverseinventories.util.TestInstanceCreator;
import junit.framework.Assert;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({MultiverseInventories.class, PluginDescriptionFile.class, JavaPluginLoader.class, MultiverseCore.class})
@PowerMockIgnore("javax.script.*")
public class TestWorldChanged {
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

    @Test
    public void testBasicWorldChange() throws IOException {

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

        // Verify removal
        Assert.assertTrue(!inventories.getGroupManager().getDefaultGroup().getWorlds().contains("world2"));
        cmdArgs = new String[]{"info", "default"};
        inventories.onCommand(mockCommandSender, mockCommand, "", cmdArgs);

        Assert.assertEquals(3, inventories.getMVIConfig().getGlobalDebug());

        Player player = inventories.getServer().getPlayer("dumptruckman");

        addToInventory(player.getInventory(), getFillerInv());
        String originalInventory = player.getInventory().toString();

        changeWorld(player, "world", "world_nether");

        String newInventory = player.getInventory().toString();
        Assert.assertEquals(originalInventory, newInventory);

        changeWorld(player, "world_nether", "world2");

        Assert.assertNotSame(originalInventory, newInventory);

        cmdArgs = new String[]{"reload"};
        inventories.onCommand(mockCommandSender, mockCommand, "", cmdArgs);

        changeWorld(player, "world2", "world");

        newInventory = player.getInventory().toString();
        Assert.assertEquals(originalInventory, newInventory);
    }

    @Test
    public void testNegativeSharables() {

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

        // add inventory shares (inv and armor) to default group
        cmdArgs = new String[]{"addshares", "-saturation", "default"};
        inventories.onCommand(mockCommandSender, mockCommand, "", cmdArgs);

        Shares shares = Sharables.allOf();
        shares.remove(Sharables.SATURATION);
        Assert.assertTrue(inventories.getGroupManager().getDefaultGroup().getShares().isSharing(shares));

        // Reload to ensure things are saving to config.yml
        cmdArgs = new String[]{"reload"};
        inventories.onCommand(mockCommandSender, mockCommand, "", cmdArgs);

        Assert.assertTrue(inventories.getGroupManager().getDefaultGroup().getShares().isSharing(shares));

        // Verify removal
        Assert.assertTrue(!inventories.getGroupManager().getDefaultGroup().getWorlds().contains("world2"));
        cmdArgs = new String[]{"info", "default"};
        inventories.onCommand(mockCommandSender, mockCommand, "", cmdArgs);

        Assert.assertEquals(3, inventories.getMVIConfig().getGlobalDebug());

        Player player = inventories.getServer().getPlayer(UUID.randomUUID());

        float satTest = 0.349F;
        player.setSaturation(satTest);
        double hpTest = 13D;
        player.setHealth(hpTest);
        addToInventory(player.getInventory(), getFillerInv());
        String originalInventory = player.getInventory().toString();

        changeWorld(player, "world", "world_nether");
        String newInventory = player.getInventory().toString();

        // Inventory and health should be same, saturation different (from original values)
        Assert.assertEquals(originalInventory, newInventory);
        Assert.assertNotSame(satTest, player.getSaturation());
        Assert.assertEquals(hpTest, player.getHealth());

        changeWorld(player, "world_nether", "world2");
        newInventory = player.getInventory().toString();

        // Inventory, health and saturation should be different (from original values)
        Assert.assertNotSame(originalInventory, newInventory);
        Assert.assertNotSame(satTest, player.getSaturation());
        Assert.assertNotSame(hpTest, player.getHealth());

        cmdArgs = new String[]{"reload"};
        inventories.onCommand(mockCommandSender, mockCommand, "", cmdArgs);

        changeWorld(player, "world2", "world");
        newInventory = player.getInventory().toString();

        // Inventory, health and saturation should be same (from original values)
        Assert.assertEquals(originalInventory, newInventory);
        Assert.assertEquals(satTest, player.getSaturation());
        Assert.assertEquals(hpTest, player.getHealth());

        changeWorld(player, "world", "world2");
        newInventory = player.getInventory().toString();

        // Inventory, health and saturation should be different (from original values)
        Assert.assertNotSame(originalInventory, newInventory);
        Assert.assertNotSame(satTest, player.getSaturation());
        Assert.assertNotSame(hpTest, player.getHealth());

        cmdArgs = new String[]{"reload"};
        inventories.onCommand(mockCommandSender, mockCommand, "", cmdArgs);

        changeWorld(player, "world2", "world");

        newInventory = player.getInventory().toString();
        Assert.assertEquals(originalInventory, newInventory);
    }

    @Test
    public void testGroupedSharesWorldChange() throws Exception {

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

        // remove all shares from default group
        cmdArgs = new String[]{"rmshares", "all", "default"};
        inventories.onCommand(mockCommandSender, mockCommand, "", cmdArgs);
        // add inventory shares (inv and armor) to default group
        cmdArgs = new String[]{"addshares", "inventory,saturation", "default"};
        inventories.onCommand(mockCommandSender, mockCommand, "", cmdArgs);

        Assert.assertFalse(inventories.getGroupManager().getDefaultGroup().getShares().isSharing(Sharables.all()));
        Assert.assertTrue(inventories.getGroupManager().getDefaultGroup().getShares().isSharing(Sharables.INVENTORY));
        Assert.assertTrue(inventories.getGroupManager().getDefaultGroup().getShares().isSharing(Sharables.ARMOR));
        Assert.assertTrue(inventories.getGroupManager().getDefaultGroup().getShares().isSharing(Sharables.SATURATION));

        // Reload to ensure things are saving to config.yml
        cmdArgs = new String[]{"reload"};
        inventories.onCommand(mockCommandSender, mockCommand, "", cmdArgs);

        Assert.assertFalse(inventories.getGroupManager().getDefaultGroup().getShares().isSharing(Sharables.all()));
        Assert.assertTrue(inventories.getGroupManager().getDefaultGroup().getShares().isSharing(Sharables.INVENTORY));
        Assert.assertTrue(inventories.getGroupManager().getDefaultGroup().getShares().isSharing(Sharables.ARMOR));
        Assert.assertTrue(inventories.getGroupManager().getDefaultGroup().getShares().isSharing(Sharables.SATURATION));

        // Verify removal
        Assert.assertTrue(!inventories.getGroupManager().getDefaultGroup().getWorlds().contains("world2"));
        cmdArgs = new String[]{"info", "default"};
        inventories.onCommand(mockCommandSender, mockCommand, "", cmdArgs);

        Assert.assertEquals(3, inventories.getMVIConfig().getGlobalDebug());

        Player player = inventories.getServer().getPlayer("dumptruckman");

        float satTest = 0.349F;
        player.setSaturation(satTest);
        addToInventory(player.getInventory(), getFillerInv());
        String originalInventory = player.getInventory().toString();

        // Changing to world within same group, nothing should change.
        changeWorld(player, "world", "world_nether");

        String newInventory = player.getInventory().toString();
        Assert.assertEquals(originalInventory, newInventory);
        Assert.assertEquals(satTest, player.getSaturation());

        changeWorld(player, "world_nether", "world2");
        newInventory = player.getInventory().toString();

        Assert.assertNotSame(originalInventory, newInventory);
        Assert.assertNotSame(satTest, player.getSaturation());

        cmdArgs = new String[]{"reload"};
        inventories.onCommand(mockCommandSender, mockCommand, "", cmdArgs);

        changeWorld(player, "world2", "world");

        newInventory = player.getInventory().toString();
        Assert.assertEquals(originalInventory, newInventory);
        Assert.assertEquals(satTest, player.getSaturation());

        changeWorld(player, "world", "world2");
        originalInventory = player.getInventory().toString();

        Assert.assertNotSame(originalInventory, newInventory);
        Assert.assertNotSame(satTest, player.getSaturation());

        FlatFileDataHelper dataHelper = new FlatFileDataHelper(inventories.getData());
        File playerFile = dataHelper.getPlayerFile(ContainerType.GROUP, "default", player);
        FileConfiguration playerConfig = JsonConfiguration.loadConfiguration(playerFile);
        playerConfig.set("SURVIVAL." + DataStrings.PLAYER_INVENTORY_CONTENTS, null);
        playerConfig.set("SURVIVAL." + DataStrings.PLAYER_ARMOR_CONTENTS, null);
        try {
            playerConfig.save(playerFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        cmdArgs = new String[]{"reload"};
        inventories.onCommand(mockCommandSender, mockCommand, "", cmdArgs);

        changeWorld(player, "world2", "world");

        newInventory = player.getInventory().toString();
        Assert.assertEquals(originalInventory, newInventory);
    }

    @Test
    public void testGroupingConflictChecker() {

        // Initialize a fake command
        Command mockCommand = mock(Command.class);
        when(mockCommand.getName()).thenReturn("mvinv");

        // remove world2 from default group
        String[] cmdArgs = new String[]{"rmworld", "world2", "default"};
        inventories.onCommand(mockCommandSender, mockCommand, "", cmdArgs);

        WorldGroup group = inventories.getGroupManager().newEmptyGroup("test");
        group.addWorld("world");
        group.addWorld("world_nether");
        group.addWorld("world_the_end");
        group.addWorld("world2");
        group.getShares().setSharing(Sharables.allOf(), true);
        inventories.getGroupManager().updateGroup(group);
        cmdArgs = new String[]{"reload"};
        inventories.onCommand(mockCommandSender, mockCommand, "", cmdArgs);
        Assert.assertTrue(inventories.getGroupManager().checkGroups().isEmpty());

        cmdArgs = new String[]{"rmworld", "world_the_end", "test"};
        inventories.onCommand(mockCommandSender, mockCommand, "", cmdArgs);
        cmdArgs = new String[]{"reload"};
        inventories.onCommand(mockCommandSender, mockCommand, "", cmdArgs);

        Assert.assertFalse(inventories.getGroupManager().checkGroups().isEmpty());

    }
}