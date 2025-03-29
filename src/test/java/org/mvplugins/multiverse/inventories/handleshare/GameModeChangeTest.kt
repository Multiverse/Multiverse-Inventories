package org.mvplugins.multiverse.inventories.handleshare

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.junit.jupiter.api.Assertions.assertEquals
import org.mvplugins.multiverse.core.world.WorldManager
import org.mvplugins.multiverse.core.world.options.CreateWorldOptions
import org.mvplugins.multiverse.inventories.TestWithMockBukkit
import org.mvplugins.multiverse.inventories.config.InventoriesConfig
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class GameModeChangeTest : TestWithMockBukkit() {

    @BeforeTest
    fun setUp() {
        val worldManager = serviceLocator.getActiveService(WorldManager::class.java).takeIf { it != null } ?: run {
            throw IllegalStateException("WorldManager is not available as a service")
        }
        assertTrue(worldManager.createWorld(CreateWorldOptions.worldName("world")).isSuccess)
        val inventoriesConfig = serviceLocator.getActiveService(InventoriesConfig::class.java).takeIf { it != null } ?: run {
            throw IllegalStateException("InventoriesConfig is not available as a service")
        }
        writeResourceToConfigFile("/gameplay/gamemode_change_groups.yml", "groups.yml")
        multiverseInventories.reloadConfig()
        inventoriesConfig.setEnableGamemodeShareHandling(true)
    }

    @Test
    fun `Test change game mode`() {
        val player = server.addPlayer("Benji_0224")
        val survivalItems = arrayOf(
            ItemStack.of(Material.STONE_BRICKS, 64),
            ItemStack.of(Material.SAND, 64),
        )
        player.inventory.contents = survivalItems

        player.gameMode = org.bukkit.GameMode.CREATIVE
        Thread.sleep(10)
        assertNotEquals(survivalItems[0], player.inventory.getItem(0))
        assertNotEquals(survivalItems[1], player.inventory.getItem(1))
        val creativeItems = arrayOf(
            ItemStack.of(Material.OBSIDIAN, 64),
            ItemStack.of(Material.HOPPER, 64),
        )
        player.inventory.contents = creativeItems

        player.gameMode = org.bukkit.GameMode.SURVIVAL
        Thread.sleep(10)
        assertEquals(survivalItems[0], player.inventory.getItem(0))
        assertEquals(survivalItems[1], player.inventory.getItem(1))
        assertNotEquals(creativeItems[0], player.inventory.getItem(0))
        assertNotEquals(creativeItems[1], player.inventory.getItem(1))

        player.gameMode = org.bukkit.GameMode.CREATIVE
        Thread.sleep(10)
        assertEquals(creativeItems[0], player.inventory.getItem(0))
        assertEquals(creativeItems[1], player.inventory.getItem(1))
        assertNotEquals(survivalItems[0], player.inventory.getItem(0))
        assertNotEquals(survivalItems[1], player.inventory.getItem(1))
    }
}
