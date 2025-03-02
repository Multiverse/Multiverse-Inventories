package org.mvplugins.multiverse.inventories;

import com.dumptruckman.bukkit.configuration.json.JsonConfiguration;
import org.mvplugins.multiverse.inventories.profile.container.ContainerType;
import org.mvplugins.multiverse.inventories.share.Sharables;
import org.mvplugins.multiverse.inventories.share.Shares;
import org.mvplugins.multiverse.inventories.util.TestInstanceCreator;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
        Location oldLocation = player.getLocation();
        Location location = new Location(mockServer.getWorld(toWorld), 0.0, 70.0, 0.0);
        player.teleport(location);
        assertEquals(location, player.getLocation());
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

    @Test
    public void testBasicWorldChange() throws IOException {

        // Initialize a fake command
        Command mockCommand = mock(Command.class);
        when(mockCommand.getName()).thenReturn("mvinv");

        Command mockCoreCommand = mock(Command.class);
        when(mockCoreCommand.getName()).thenReturn("mv");

        // Assert debug mode is off
        assertEquals(0, inventories.getMVIConfig().getGlobalDebug());

        // Send the debug command.
        String[] cmdArgs = new String[]{"debug", "3"};
        inventories.onCommand(mockCommandSender, mockCoreCommand, "", cmdArgs);

        // remove world2 from default group
        cmdArgs = new String[]{"rmworld", "world2", "default"};
        inventories.onCommand(mockCommandSender, mockCommand, "", cmdArgs);

        // Verify removal
        assertFalse(inventories.getGroupManager().getDefaultGroup().getWorlds().contains("world2"));
        cmdArgs = new String[]{"info", "default"};
        inventories.onCommand(mockCommandSender, mockCommand, "", cmdArgs);

        assertEquals(3, inventories.getMVIConfig().getGlobalDebug());

        Player player = inventories.getServer().getPlayerExact("dumptruckman");

        addToInventory(player.getInventory(), getFillerInv());
        String originalInventory = player.getInventory().toString();

        changeWorld(player, "world", "world_nether");

        String newInventory = player.getInventory().toString();
        assertEquals(originalInventory, newInventory);

        changeWorld(player, "world_nether", "world2");

        assertNotSame(originalInventory, newInventory);

        cmdArgs = new String[]{"reload"};
        inventories.onCommand(mockCommandSender, mockCommand, "", cmdArgs);

        changeWorld(player, "world2", "world");

        newInventory = player.getInventory().toString();
        assertEquals(originalInventory, newInventory);
    }

    @Test
    public void testNegativeSharables() {

        // Initialize a fake command
        Command mockCommand = mock(Command.class);
        when(mockCommand.getName()).thenReturn("mvinv");

        Command mockCoreCommand = mock(Command.class);
        when(mockCoreCommand.getName()).thenReturn("mv");

        // Assert debug mode is off
        assertEquals(0, inventories.getMVIConfig().getGlobalDebug());

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
        assertTrue(inventories.getGroupManager().getDefaultGroup().getShares().isSharing(shares));

        // Reload to ensure things are saving to config.yml
        cmdArgs = new String[]{"reload"};
        inventories.onCommand(mockCommandSender, mockCommand, "", cmdArgs);

        assertTrue(inventories.getGroupManager().getDefaultGroup().getShares().isSharing(shares));

        // Verify removal
        assertFalse(inventories.getGroupManager().getDefaultGroup().getWorlds().contains("world2"));
        cmdArgs = new String[]{"info", "default"};
        inventories.onCommand(mockCommandSender, mockCommand, "", cmdArgs);

        assertEquals(3, inventories.getMVIConfig().getGlobalDebug());

        Player player = inventories.getServer().getPlayerExact("dumptruckman");

        float satTest = 0.349F;
        player.setSaturation(satTest);
        double hpTest = 13D;
        player.setHealth(hpTest);
        addToInventory(player.getInventory(), getFillerInv());
        String originalInventory = player.getInventory().toString();

        changeWorld(player, "world", "world_nether");
        String newInventory = player.getInventory().toString();

        // Inventory and health should be same, saturation different (from original values)
        assertEquals(originalInventory, newInventory);
        assertNotSame(satTest, player.getSaturation());
        assertEquals(hpTest, player.getHealth(), 0);

        changeWorld(player, "world_nether", "world2");
        newInventory = player.getInventory().toString();

        // Inventory, health and saturation should be different (from original values)
        assertNotSame(originalInventory, newInventory);
        assertNotSame(satTest, player.getSaturation());
        assertNotSame(hpTest, player.getHealth());

        cmdArgs = new String[]{"reload"};
        inventories.onCommand(mockCommandSender, mockCommand, "", cmdArgs);

        changeWorld(player, "world2", "world");
        newInventory = player.getInventory().toString();

        // Inventory, health and saturation should be same (from original values)
        assertEquals(originalInventory, newInventory);
        assertEquals(satTest, player.getSaturation(), 0);
        assertEquals(hpTest, player.getHealth(), 0);

        changeWorld(player, "world", "world2");
        newInventory = player.getInventory().toString();

        // Inventory, health and saturation should be different (from original values)
        assertNotSame(originalInventory, newInventory);
        assertNotSame(satTest, player.getSaturation());
        assertNotSame(hpTest, player.getHealth());

        cmdArgs = new String[]{"reload"};
        inventories.onCommand(mockCommandSender, mockCommand, "", cmdArgs);

        changeWorld(player, "world2", "world");

        newInventory = player.getInventory().toString();
        assertEquals(originalInventory, newInventory);
    }

    @Test
    public void testGroupedSharesWorldChange() throws Exception {

        // Initialize a fake command
        Command mockCommand = mock(Command.class);
        when(mockCommand.getName()).thenReturn("mvinv");

        Command mockCoreCommand = mock(Command.class);
        when(mockCoreCommand.getName()).thenReturn("mv");

        // Assert debug mode is off
        assertEquals(0, inventories.getMVIConfig().getGlobalDebug());

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

        assertFalse(inventories.getGroupManager().getDefaultGroup().getShares().isSharing(Sharables.all()));
        assertTrue(inventories.getGroupManager().getDefaultGroup().getShares().isSharing(Sharables.INVENTORY));
        assertTrue(inventories.getGroupManager().getDefaultGroup().getShares().isSharing(Sharables.ARMOR));
        assertTrue(inventories.getGroupManager().getDefaultGroup().getShares().isSharing(Sharables.SATURATION));

        // Reload to ensure things are saving to config.yml
        cmdArgs = new String[]{"reload"};
        inventories.onCommand(mockCommandSender, mockCommand, "", cmdArgs);

        assertFalse(inventories.getGroupManager().getDefaultGroup().getShares().isSharing(Sharables.all()));
        assertTrue(inventories.getGroupManager().getDefaultGroup().getShares().isSharing(Sharables.INVENTORY));
        assertTrue(inventories.getGroupManager().getDefaultGroup().getShares().isSharing(Sharables.ARMOR));
        assertTrue(inventories.getGroupManager().getDefaultGroup().getShares().isSharing(Sharables.SATURATION));

        // Verify removal
        assertFalse(inventories.getGroupManager().getDefaultGroup().getWorlds().contains("world2"));
        cmdArgs = new String[]{"info", "default"};
        inventories.onCommand(mockCommandSender, mockCommand, "", cmdArgs);

        assertEquals(3, inventories.getMVIConfig().getGlobalDebug());

        Player player = inventories.getServer().getPlayerExact("dumptruckman");

        float satTest = 0.349F;
        player.setSaturation(satTest);
        addToInventory(player.getInventory(), getFillerInv());
        String originalInventory = player.getInventory().toString();

        // Changing to world within same group, nothing should change.
        changeWorld(player, "world", "world_nether");

        String newInventory = player.getInventory().toString();
        assertEquals(originalInventory, newInventory);
        assertEquals(satTest, player.getSaturation(), 0);

        changeWorld(player, "world_nether", "world2");
        newInventory = player.getInventory().toString();

        assertNotSame(originalInventory, newInventory);
        assertNotSame(satTest, player.getSaturation());

        cmdArgs = new String[]{"reload"};
        inventories.onCommand(mockCommandSender, mockCommand, "", cmdArgs);

        changeWorld(player, "world2", "world");

        newInventory = player.getInventory().toString();
        assertEquals(originalInventory, newInventory);
        assertEquals(satTest, player.getSaturation(), 0);

        changeWorld(player, "world", "world2");
        originalInventory = player.getInventory().toString();

        assertNotSame(originalInventory, newInventory);
        assertNotSame(satTest, player.getSaturation());

        FlatFileDataHelper dataHelper = new FlatFileDataHelper(inventories.getData());
        File playerFile = dataHelper.getPlayerFile(ContainerType.GROUP, "default", "dumptruckman");
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
        assertEquals(originalInventory, newInventory);
    }

    @Test
    public void testLastLocation() throws Exception {

        // Initialize a fake command
        Command mockCommand = mock(Command.class);
        when(mockCommand.getName()).thenReturn("mvinv");

        Command mockCoreCommand = mock(Command.class);
        when(mockCoreCommand.getName()).thenReturn("mv");

        // Assert debug mode is off
        assertEquals(0, inventories.getMVIConfig().getGlobalDebug());

        // Send the debug command.
        String[] cmdArgs = new String[]{"debug", "3"};
        inventories.onCommand(mockCommandSender, mockCoreCommand, "", cmdArgs);

        // remove world2 from default group
        cmdArgs = new String[]{"rmworld", "world2", "default"};
        inventories.onCommand(mockCommandSender, mockCommand, "", cmdArgs);

        // remove all shares from default group
        cmdArgs = new String[]{"rmshares", "all", "default"};
        inventories.onCommand(mockCommandSender, mockCommand, "", cmdArgs);
        // enable last_location share
        cmdArgs = new String[]{"toggle", "last_location"};
        inventories.onCommand(mockCommandSender, mockCommand, "", cmdArgs);
        // add last_location share to default group
        cmdArgs = new String[]{"addshares", "last_location", "default"};
        inventories.onCommand(mockCommandSender, mockCommand, "", cmdArgs);

        assertFalse(inventories.getGroupManager().getDefaultGroup().getShares().isSharing(Sharables.all()));
        assertTrue(inventories.getMVIConfig().getOptionalShares().isSharing(Sharables.LAST_LOCATION));
        assertTrue(inventories.getGroupManager().getDefaultGroup().getShares().isSharing(Sharables.LAST_LOCATION));

        // Reload to ensure things are saving to config.yml
        cmdArgs = new String[]{"reload"};
        inventories.onCommand(mockCommandSender, mockCommand, "", cmdArgs);

        assertFalse(inventories.getGroupManager().getDefaultGroup().getShares().isSharing(Sharables.all()));
        assertTrue(inventories.getMVIConfig().getOptionalShares().isSharing(Sharables.LAST_LOCATION));
        assertTrue(inventories.getGroupManager().getDefaultGroup().getShares().isSharing(Sharables.LAST_LOCATION));

        // Verify removal
        assertFalse(inventories.getGroupManager().getDefaultGroup().getWorlds().contains("world2"));
        cmdArgs = new String[]{"info", "default"};
        inventories.onCommand(mockCommandSender, mockCommand, "", cmdArgs);

        assertEquals(3, inventories.getMVIConfig().getGlobalDebug());

        Player player = inventories.getServer().getPlayerExact("dumptruckman");

        // Move player within group and to a different location than spawn
        changeWorld(player, "world", "world_nether");
        Location lastLocation = new Location(mockServer.getWorld("world_nether"), 10, 10, 10);
        player.teleport(lastLocation);

        // Move player out of group
        changeWorld(player, "world_nether", "world2");
        assertNotSame(lastLocation, player.getLocation());

        // Move player back to group
        changeWorld(player, "world2", "world_nether");
        assertEquals(lastLocation, player.getLocation());
    }

    @Test
    public void testOptionalsForUngroupedWorlds() throws Exception {

        // Initialize a fake command
        Command mockCommand = mock(Command.class);
        when(mockCommand.getName()).thenReturn("mvinv");

        Command mockCoreCommand = mock(Command.class);
        when(mockCoreCommand.getName()).thenReturn("mv");

        // Assert debug mode is off
        assertEquals(0, inventories.getMVIConfig().getGlobalDebug());

        // Assert UseOptionalsForUngroupedWorlds is set to true
        assertTrue(inventories.getMVIConfig().usingOptionalsForUngrouped());

        // Change UseOptionalsForUngroupedWorlds to false, then assert that it is false
        inventories.getMVIConfig().setUsingOptionalsForUngrouped(false);
        assertFalse(inventories.getMVIConfig().usingOptionalsForUngrouped());

        // Send the debug command.
        String[] cmdArgs = new String[]{"debug", "3"};
        inventories.onCommand(mockCommandSender, mockCoreCommand, "", cmdArgs);

        // remove world2 from default group
        cmdArgs = new String[]{"rmworld", "world2", "default"};
        inventories.onCommand(mockCommandSender, mockCommand, "", cmdArgs);

        // remove all shares from default group
        cmdArgs = new String[]{"rmshares", "all", "default"};
        inventories.onCommand(mockCommandSender, mockCommand, "", cmdArgs);
        // enable last_location share
        cmdArgs = new String[]{"toggle", "last_location"};
        inventories.onCommand(mockCommandSender, mockCommand, "", cmdArgs);
        // add last_location share to default group
        cmdArgs = new String[]{"addshares", "last_location", "default"};
        inventories.onCommand(mockCommandSender, mockCommand, "", cmdArgs);
        // add inventory share to default group
        cmdArgs = new String[]{"addshares", "inventory", "default"};
        inventories.onCommand(mockCommandSender, mockCommand, "", cmdArgs);

        assertFalse(inventories.getGroupManager().getDefaultGroup().getShares().isSharing(Sharables.all()));
        assertTrue(inventories.getMVIConfig().getOptionalShares().isSharing(Sharables.LAST_LOCATION));
        assertTrue(inventories.getGroupManager().getDefaultGroup().getShares().isSharing(Sharables.LAST_LOCATION));

        // Reload to ensure things are saving to config.yml
        cmdArgs = new String[]{"reload"};
        inventories.onCommand(mockCommandSender, mockCommand, "", cmdArgs);

        assertFalse(inventories.getGroupManager().getDefaultGroup().getShares().isSharing(Sharables.all()));
        assertTrue(inventories.getMVIConfig().getOptionalShares().isSharing(Sharables.LAST_LOCATION));
        assertTrue(inventories.getGroupManager().getDefaultGroup().getShares().isSharing(Sharables.LAST_LOCATION));
        assertFalse(inventories.getMVIConfig().usingOptionalsForUngrouped());

        // Verify removal
        assertFalse(inventories.getGroupManager().getDefaultGroup().getWorlds().contains("world2"));
        cmdArgs = new String[]{"info", "default"};
        inventories.onCommand(mockCommandSender, mockCommand, "", cmdArgs);

        assertEquals(3, inventories.getMVIConfig().getGlobalDebug());

        Player player = inventories.getServer().getPlayerExact("dumptruckman");

        // get inventories as strings
        String emptyInventory = player.getInventory().toString();
        addToInventory(player.getInventory(), getFillerInv());
        String originalInventory = player.getInventory().toString();

        // Move player within group and to a different location than spawn
        changeWorld(player, "world", "world_nether");
        Location lastLocation = new Location(mockServer.getWorld("world_nether"), 10, 10, 10);
        player.teleport(lastLocation);

        // make sure inventory is the same
        assertEquals(originalInventory, player.getInventory().toString());

        // Move player out of group
        changeWorld(player, "world_nether", "world2");
        assertNotSame(lastLocation, player.getLocation());
        assertEquals(emptyInventory, player.getInventory().toString());

        // Move player back to group
        changeWorld(player, "world2", "world_nether");
        assertEquals(lastLocation, player.getLocation());
        assertEquals(originalInventory, player.getInventory().toString());

        // Move player within group again
        // Note: newLocation must match the location given made in changeWorld()
        Location newLocation = new Location(mockServer.getWorld("world"), 0, 70, 0);
        changeWorld(player, "world_nether", "world");
        assertEquals(originalInventory, player.getInventory().toString());
        // The following two assertions mean the same thing (they're redundant)
        // but they help in understanding what is being tested.
        assertNotSame(lastLocation, player.getLocation());
        assertEquals(newLocation, player.getLocation());

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
        assertTrue(inventories.getGroupManager().checkGroups().isEmpty());

        cmdArgs = new String[]{"rmworld", "world_the_end", "test"};
        inventories.onCommand(mockCommandSender, mockCommand, "", cmdArgs);
        cmdArgs = new String[]{"reload"};
        inventories.onCommand(mockCommandSender, mockCommand, "", cmdArgs);

        assertFalse(inventories.getGroupManager().checkGroups().isEmpty());

    }
}
