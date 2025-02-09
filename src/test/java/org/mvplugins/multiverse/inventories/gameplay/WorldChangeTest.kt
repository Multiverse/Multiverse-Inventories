package org.mvplugins.multiverse.inventories.gameplay

import com.dumptruckman.minecraft.util.Logging
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.mvplugins.multiverse.core.world.WorldManager
import org.mvplugins.multiverse.core.world.options.CreateWorldOptions
import org.mvplugins.multiverse.inventories.TestWithMockBukkit
import kotlin.test.*

class WorldChangeTest : TestWithMockBukkit() {

    @BeforeTest
    fun setUp() {
        val worldManager = serviceLocator.getActiveService(WorldManager::class.java).takeIf { it != null } ?: run {
            throw IllegalStateException("WorldManager is not available as a service") }
        assertTrue(worldManager.createWorld(CreateWorldOptions.worldName("world1")).isSuccess)
        assertTrue(worldManager.createWorld(CreateWorldOptions.worldName("world2")).isSuccess)
        assertTrue(worldManager.createWorld(CreateWorldOptions.worldName("world3")).isSuccess)
        assertTrue(worldManager.createWorld(CreateWorldOptions.worldName("world4")).isSuccess)
    }

    @Test
    fun `Test shares`() {
        writeResourceToConfigFile("/gameplay/world_change_groups.yml", "groups.yml")
        multiverseInventories.reloadConfig()
        val player = server.addPlayer("Benji_0224")
        Logging.fine("player world: " + server.getPlayer("Benji_0224")?.world?.name)
        val stack = ItemStack.of(Material.STONE_BRICKS, 64)
        player.inventory.setItem(0, stack)
        val startTime = System.nanoTime()
        server.getWorld("world2")?.let { player.teleport(it.spawnLocation) }
        assertEquals(stack, player.inventory.getItem(0))
        server.getWorld("world4")?.let { player.teleport(it.spawnLocation) }
        assertNotEquals(stack, player.inventory.getItem(0))
        server.getWorld("world2")?.let { player.teleport(it.spawnLocation) }
        assertEquals(stack, player.inventory.getItem(0))
        Logging.info("Time taken: " + (System.nanoTime() - startTime) / 1000000 + "ms")
    }
}
