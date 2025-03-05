package org.mvplugins.multiverse.inventories;

import org.mvplugins.multiverse.inventories.profile.PlayerProfile;
import org.mvplugins.multiverse.inventories.share.ProfileEntry;
import org.mvplugins.multiverse.inventories.share.Sharable;
import org.mvplugins.multiverse.inventories.share.SharableHandler;
import org.mvplugins.multiverse.inventories.share.Sharables;
import org.mvplugins.multiverse.inventories.util.TestInstanceCreator;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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

public class TestWSharableAPI {
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

    public final static Sharable<Double> CUSTOM = new Sharable.Builder<Double>("custom", Double.class,
            new SharableHandler<Double>() {
                @Override
                public void updateProfile(PlayerProfile profile, Player player) {
                    profile.set(CUSTOM, (double) player.getMaximumNoDamageTicks());
                }

                @Override
                public boolean updatePlayer(Player player, PlayerProfile profile) {
                    Double value = profile.get(CUSTOM);
                    if (value == null) {
                        // Specify default value
                        player.setMaximumNoDamageTicks(0);
                        return false;
                    }
                    player.setMaximumNoDamageTicks((int) Double.doubleToLongBits(value));
                    return true;
                }
            }).defaultSerializer(new ProfileEntry(false, "custom")).build();
    public final static Sharable<Integer> OPTIONAL = new Sharable.Builder<Integer>("optional", Integer.class,
            new SharableHandler<Integer>() {
                @Override
                public void updateProfile(PlayerProfile profile, Player player) {
                    profile.set(CUSTOM, player.getLastDamage());
                }

                @Override
                public boolean updatePlayer(Player player, PlayerProfile profile) {
                    Double value = profile.get(CUSTOM);
                    if (value == null) {
                        // Specify default value
                        player.setLastDamage(0);
                        return false;
                    }
                    player.setLastDamage(value);
                    return true;
                }
            }).defaultSerializer(new ProfileEntry(false, "optional")).optional().build();

    public final static Sharable<Map> CUSTOM_MAP = new Sharable.Builder<Map>("custom_map", Map.class,
            new SharableHandler<Map>() {
                @Override
                public void updateProfile(PlayerProfile profile, Player player) {
                    Map<String, Object> data = new HashMap<String, Object>();
                    data.put("maxNoDamageTick", player.getMaximumNoDamageTicks());
                    data.put("displayName", player.getDisplayName());
                    profile.set(CUSTOM_MAP, data);
                }

                @Override
                public boolean updatePlayer(Player player, PlayerProfile profile) {
                    Map<String, Object> data = profile.get(CUSTOM_MAP);
                    if (data == null) {
                        // Specify default value
                        player.setMaximumNoDamageTicks(0);
                        player.setDisplayName("poop");
                        return false;
                    }
                    player.setMaximumNoDamageTicks((Integer) data.get("maxNoDamageTick"));
                    player.setDisplayName(data.get("displayName").toString());
                    return true;
                }
            }).defaultSerializer(new ProfileEntry(false, "custom_map")).build();

    @Test
    public void testSharableAPI() {

        assertTrue(Sharables.all().contains(CUSTOM));
        assertTrue(Sharables.all().contains(OPTIONAL));

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

        WorldGroup newGroup = inventories.getGroupManager().newEmptyGroup("test");
        newGroup.getShares().mergeShares(Sharables.allOf());
        newGroup.addWorld("world2");
        newGroup.getShares().remove(OPTIONAL);
        inventories.getGroupManager().updateGroup(newGroup);

        // Verify removal
        assertFalse(inventories.getGroupManager().getDefaultGroup().getWorlds().contains("world2"));
        cmdArgs = new String[]{"info", "default"};
        inventories.onCommand(mockCommandSender, mockCommand, "", cmdArgs);

        assertEquals(3, inventories.getMVIConfig().getGlobalDebug());

        Player player = inventories.getServer().getPlayerExact("dumptruckman");
        //changeWorld(player, "world", "world2");
        //changeWorld(player, "world2", "world");

        //cmdArgs = new String[]{"addshare", "-optional", "default"};
        //inventories.onCommand(mockCommandSender, mockCommand, "", cmdArgs);

        addToInventory(player.getInventory(), TestWorldChanged.getFillerInv());
        player.setMaximumNoDamageTicks(10);
        int lastDamage = 10;
        player.setLastDamage(lastDamage);
        assertEquals(10, player.getMaximumNoDamageTicks());
        String originalInventory = player.getInventory().toString();

        changeWorld(player, "world", "world_nether");
        String newInventory = player.getInventory().toString();
        assertEquals(originalInventory, newInventory);
        assertEquals(10, player.getMaximumNoDamageTicks());
        assertEquals(lastDamage, player.getLastDamage(), 0);

        changeWorld(player, "world_nether", "world2");
        assertEquals(0, player.getMaximumNoDamageTicks());
        assertNotSame(originalInventory, newInventory);
        assertEquals(lastDamage, player.getLastDamage(), 0);
        changeWorld(player, "world2", "world");
        assertEquals(10, player.getMaximumNoDamageTicks());
        assertEquals(originalInventory, newInventory);
        assertEquals(lastDamage, player.getLastDamage(), 0);
    }

}
