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
import kotlin.test.*

class WorldChangeTest : TestWithMockBukkit() {

    private lateinit var inventoriesConfig: InventoriesConfig

    @BeforeTest
    fun setUp() {
        inventoriesConfig = serviceLocator.getActiveService(InventoriesConfig::class.java).takeIf { it != null } ?: run {
            throw IllegalStateException("InventoriesConfig is not available as a service")
        }
        val worldManager = serviceLocator.getActiveService(WorldManager::class.java).takeIf { it != null } ?: run {
            throw IllegalStateException("WorldManager is not available as a service") }

        writeResourceToConfigFile("/gameplay/world_change_groups.yml", "groups.yml")
        multiverseInventories.reloadConfig()

        assertTrue(worldManager.createWorld(CreateWorldOptions.worldName("world1")).isSuccess)
        assertTrue(worldManager.createWorld(CreateWorldOptions.worldName("world2")).isSuccess)
        assertTrue(worldManager.createWorld(CreateWorldOptions.worldName("world3")).isSuccess)
        assertTrue(worldManager.createWorld(CreateWorldOptions.worldName("world4")).isSuccess)
        assertTrue(worldManager.createWorld(CreateWorldOptions.worldName("ungrouped")).isSuccess)
        assertTrue(worldManager.createWorld(CreateWorldOptions.worldName("default")).isSuccess)
    }

    @Test
    fun `World change between groups`() {
        val player = server.addPlayer("Benji_0224")
        server.getWorld("world1")?.let { player.teleport(it.spawnLocation) }
        val stack = ItemStack.of(Material.STONE_BRICKS, 64)
        player.inventory.setItem(0, stack)
        player.health = 5.5
        player.totalExperience = 10

        server.getWorld("world2")?.let { player.teleport(it.spawnLocation) }
        assertEquals(stack, player.inventory.getItem(0))
        assertEquals(5.5, player.health)
        assertEquals(10, player.totalExperience)

        server.getWorld("world4")?.let { player.teleport(it.spawnLocation) }
        assertNotEquals(stack, player.inventory.getItem(0))
        assertNotEquals(5.5, player.health)
        assertEquals(player.totalExperience, 0)

        server.getWorld("world2")?.let { player.teleport(it.spawnLocation) }
        assertEquals(stack, player.inventory.getItem(0))
        assertEquals(5.5, player.health)
        assertEquals(player.totalExperience, 0)
    }

    @Test
    fun `World change between group and ungrouped worlds`() {
        val player = server.addPlayer("Benji_0224")

        server.getWorld("world3")?.let { player.teleport(it.spawnLocation) }
        val stack = ItemStack.of(Material.STONE_BRICKS, 64)
        player.inventory.setItem(0, stack)
        player.health = 5.5
        player.totalExperience = 10

        server.getWorld("ungrouped")?.let { player.teleport(it.spawnLocation) }
        assertEquals(ItemStack.empty(), player.inventory.getItem(0))
        assertEquals(20.0, player.health)
        assertEquals(0, player.totalExperience)

        val stack2 = ItemStack.of(Material.HOPPER, 23)
        player.inventory.setItem(0, stack2)
        player.health = 2.5
        player.totalExperience = 5

        server.getWorld("world3")?.let { player.teleport(it.spawnLocation) }
        assertEquals(stack, player.inventory.getItem(0))
        assertEquals(5.5, player.health)
        assertEquals(10, player.totalExperience)

        server.getWorld("ungrouped")?.let { player.teleport(it.spawnLocation) }
        assertEquals(stack2, player.inventory.getItem(0))
        assertEquals(2.5, player.health)
        assertEquals(5, player.totalExperience)
    }

    @Test
    fun `World change with defaultUngroupedWorlds enabled`() {
        inventoriesConfig.defaultUngroupedWorlds = true
        inventoriesConfig.save()

        val player = server.addPlayer("Benji_0224")

        server.getWorld("default")?.let { player.teleport(it.spawnLocation) }
        val stack = ItemStack.of(Material.WATER_BUCKET, 64)
        player.inventory.setItem(0, stack)
        player.health = 5.5
        player.totalExperience = 10

        server.getWorld("ungrouped")?.let { player.teleport(it.spawnLocation) }
        assertEquals(stack, player.inventory.getItem(0))
        assertEquals(5.5, player.health)
        assertEquals(10, player.totalExperience)

        server.getWorld("default")?.let { player.teleport(it.spawnLocation) }
        assertEquals(stack, player.inventory.getItem(0))
        assertEquals(5.5, player.health)
        assertEquals(10, player.totalExperience)
    }

    @Test
    fun `World change with last_location optional share`() {
        inventoriesConfig.activeOptionalShares = Sharables.fromSharables(Sharables.LAST_LOCATION)

        val world1Loc = Location(Bukkit.getWorld("world1")!!, 1.0, 2.0, 3.0)
        val world2Loc = Location(Bukkit.getWorld("world2")!!, 4.0, 5.0, 6.0)

        val player = server.addPlayer("Benji_0224")
        assertTrue(player.teleport(world1Loc.clone()))
        assertLocationEquals(world1Loc, player.location)

        player.teleport(Bukkit.getWorld("world2")!!.spawnLocation.clone())
        assertLocationEquals(Bukkit.getWorld("world2")!!.spawnLocation, player.location)
        player.teleport(world2Loc.clone())
        assertLocationEquals(world2Loc, player.location)

        player.teleport(Bukkit.getWorld("world1")!!.spawnLocation.clone())
        assertLocationEquals(world1Loc, player.location)

        player.teleport(Bukkit.getWorld("world2")!!.spawnLocation.clone())
        assertLocationEquals(world2Loc, player.location)
    }
}
