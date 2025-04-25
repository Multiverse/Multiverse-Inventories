package org.mvplugins.multiverse.inventories.handleshare

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.mvplugins.multiverse.core.world.WorldManager
import org.mvplugins.multiverse.core.world.options.CreateWorldOptions
import org.mvplugins.multiverse.inventories.TestWithMockBukkit
import org.mvplugins.multiverse.inventories.config.InventoriesConfig
import org.mvplugins.multiverse.inventories.share.Sharables
import org.mvplugins.multiverse.inventories.util.PlayerStats
import kotlin.arrayOfNulls
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GameModeChangeTest : TestWithMockBukkit() {

    private lateinit var inventoriesConfig: InventoriesConfig
    private var survivalItems = arrayOfNulls<ItemStack>(PlayerStats.INVENTORY_SIZE)
    private var creativeItems = arrayOfNulls<ItemStack>(PlayerStats.INVENTORY_SIZE)

    @BeforeTest
    fun setUp() {
        inventoriesConfig = serviceLocator.getActiveService(InventoriesConfig::class.java).takeIf { it != null } ?: run {
            throw IllegalStateException("InventoriesConfig is not available as a service")
        }
        val worldManager = serviceLocator.getActiveService(WorldManager::class.java).takeIf { it != null } ?: run {
            throw IllegalStateException("WorldManager is not available as a service")
        }

        writeResourceToConfigFile("/gameplay/gamemode_change_groups.yml", "groups.yml")
        multiverseInventories.reloadConfig()
        inventoriesConfig.enableGamemodeShareHandling = true

        survivalItems[0] = ItemStack.of(Material.STONE_BRICKS, 64)
        survivalItems[1] = ItemStack.of(Material.SAND, 64)

        creativeItems[0] = ItemStack.of(Material.HOPPER, 64)
        creativeItems[1] = ItemStack.of(Material.CAMPFIRE, 64)

        assertTrue(worldManager.createWorld(CreateWorldOptions.worldName("world")).isSuccess)
        assertTrue(worldManager.createWorld(CreateWorldOptions.worldName("ungrouped")).isSuccess)
    }

    @Test
    fun `Test change game mode for grouped worlds with total_xp disabled share`() {
        val player = server.addPlayer("Benji_0224")
        assertTrue(player.teleport(Bukkit.getWorld("world")!!.spawnLocation))

        player.inventory.contents = survivalItems.clone()
        player.totalExperience = 123

        player.gameMode = org.bukkit.GameMode.CREATIVE
        Thread.sleep(5)
        assertInventoryEquals(arrayOfNulls(PlayerStats.INVENTORY_SIZE), player.inventory.contents)
        assertEquals(123, player.totalExperience)

        player.inventory.contents = creativeItems.clone()
        player.totalExperience = 321

        player.gameMode = org.bukkit.GameMode.SURVIVAL
        Thread.sleep(5)
        assertInventoryEquals(survivalItems, player.inventory.contents)
        assertEquals(321, player.totalExperience)

        player.gameMode = org.bukkit.GameMode.CREATIVE
        Thread.sleep(5)
        assertInventoryEquals(creativeItems, player.inventory.contents)
        assertEquals(321, player.totalExperience)
    }

    @Test
    fun `Test change game mode for ungrouped worlds`() {
        val player = server.addPlayer("Benji_0224")
        assertTrue(player.teleport(Bukkit.getWorld("ungrouped")!!.spawnLocation))

        player.inventory.contents = survivalItems.clone()
        player.totalExperience = 123

        player.gameMode = org.bukkit.GameMode.CREATIVE
        Thread.sleep(5)
        assertInventoryEquals(arrayOfNulls(PlayerStats.INVENTORY_SIZE), player.inventory.contents)
        assertEquals(0, player.totalExperience)

        player.inventory.contents = creativeItems.clone()
        player.totalExperience = 321

        player.gameMode = org.bukkit.GameMode.SURVIVAL
        Thread.sleep(5)
        assertInventoryEquals(survivalItems, player.inventory.contents)
        assertEquals(123, player.totalExperience)

        player.gameMode = org.bukkit.GameMode.CREATIVE
        Thread.sleep(5)
        assertInventoryEquals(creativeItems, player.inventory.contents)
        assertEquals(321, player.totalExperience)
    }

    @Test
    fun `Test change game mode for ungrouped worlds with last_location`() {
        inventoriesConfig.defaultUngroupedWorlds = false
        inventoriesConfig.useOptionalsForUngroupedWorlds = true
        inventoriesConfig.activeOptionalShares = Sharables.fromSharables(Sharables.LAST_LOCATION)

        val survivalLocation = Location(Bukkit.getWorld("ungrouped")!!, 1.0, 2.0, 3.0)
        val creativeLocation = Location(Bukkit.getWorld("ungrouped")!!, 4.0, 5.0, 6.0)

        val player = server.addPlayer("Benji_0224")
        assertTrue(player.teleport(survivalLocation.clone()))
        player.inventory.contents = survivalItems.clone()

        player.gameMode = org.bukkit.GameMode.CREATIVE
        assertInventoryEquals(arrayOfNulls(PlayerStats.INVENTORY_SIZE), player.inventory.contents)
        assertLocationEquals(survivalLocation, player.location)

        assertTrue(player.teleport(creativeLocation.clone()))
        player.inventory.contents = creativeItems.clone()

        player.gameMode = org.bukkit.GameMode.SURVIVAL
        assertInventoryEquals(survivalItems, player.inventory.contents)
        assertLocationEquals(survivalLocation, player.location)

        player.gameMode = org.bukkit.GameMode.CREATIVE
        assertInventoryEquals(creativeItems, player.inventory.contents)
        assertLocationEquals(creativeLocation, player.location)
    }

    @Test
    fun `Test change game mode for ungrouped worlds with useOptionalsForUngroupedWorlds disabled`() {
        inventoriesConfig.defaultUngroupedWorlds = false
        inventoriesConfig.useOptionalsForUngroupedWorlds = false
        inventoriesConfig.activeOptionalShares = Sharables.fromSharables(Sharables.LAST_LOCATION)

        val survivalLocation = Location(Bukkit.getWorld("ungrouped")!!, 1.0, 2.0, 3.0)
        val creativeLocation = Location(Bukkit.getWorld("ungrouped")!!, 4.0, 5.0, 6.0)

        val player = server.addPlayer("Benji_0224")
        assertTrue(player.teleport(survivalLocation.clone()))
        player.inventory.contents = survivalItems.clone()

        player.gameMode = org.bukkit.GameMode.CREATIVE
        Thread.sleep(5)
        assertInventoryEquals(arrayOfNulls(PlayerStats.INVENTORY_SIZE), player.inventory.contents)
        assertLocationEquals(survivalLocation, player.location)

        assertTrue(player.teleport(creativeLocation.clone()))
        player.inventory.contents = creativeItems.clone()

        player.gameMode = org.bukkit.GameMode.SURVIVAL
        Thread.sleep(5)
        assertInventoryEquals(survivalItems, player.inventory.contents)
        assertLocationEquals(creativeLocation, player.location)

        player.gameMode = org.bukkit.GameMode.CREATIVE
        Thread.sleep(5)
        assertInventoryEquals(creativeItems, player.inventory.contents)
        assertLocationEquals(creativeLocation, player.location)
    }
}
