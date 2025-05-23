package org.mvplugins.multiverse.inventories.profile

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.mockbukkit.mockbukkit.entity.PlayerMock
import org.mvplugins.multiverse.core.world.WorldManager
import org.mvplugins.multiverse.core.world.options.CreateWorldOptions
import org.mvplugins.multiverse.inventories.TestWithMockBukkit
import org.mvplugins.multiverse.inventories.profile.group.WorldGroupManager
import org.mvplugins.multiverse.inventories.profile.key.GlobalProfileKey
import org.mvplugins.multiverse.inventories.util.FutureNow
import java.nio.file.Path
import kotlin.test.*

class PlayerNameChangeTest : TestWithMockBukkit() {

    private lateinit var profileDataSource: ProfileDataSource
    private lateinit var playerNamesMapper: PlayerNamesMapper
    private lateinit var player: PlayerMock

    @BeforeTest
    fun setUp() {
        profileDataSource = serviceLocator.getService(ProfileDataSource::class.java).takeIf { it != null } ?: run {
            throw IllegalStateException("ProfileDataSource is not available as a service") }
        playerNamesMapper = serviceLocator.getService(PlayerNamesMapper::class.java).takeIf { it != null } ?: run {
            throw IllegalStateException("PlayerNamesMapper is not available as a service") }

        val worldManager = serviceLocator.getActiveService(WorldManager::class.java).takeIf { it != null } ?: run {
            throw IllegalStateException("WorldManager is not available as a service") }
        assertTrue(worldManager.createWorld(CreateWorldOptions.worldName("world")).isSuccess)
        assertTrue(worldManager.createWorld(CreateWorldOptions.worldName("world_nether")).isSuccess)
        assertTrue(worldManager.createWorld(CreateWorldOptions.worldName("test")).isSuccess)

        val worldGroupManager = serviceLocator.getActiveService(WorldGroupManager::class.java).takeIf { it != null } ?: run {
            throw IllegalStateException("WorldGroupManager is not available as a service") }
        writeResourceToConfigFile("/gameplay/name_change_groups.yml", "groups.yml")
        assertTrue(worldGroupManager.load().isSuccess)

        player = server.addPlayer("Benji_0224")
        assertEquals(GlobalProfileKey.of(player.uniqueId, "Benji_0224"), playerNamesMapper.getKey("Benji_0224").orNull)
    }

    @Test
    fun `Player name changes`() {
        val stack = ItemStack.of(Material.STONE_BRICKS, 5)
        player.health = 5.0
        player.inventory.setItem(0, stack)

        server.getWorld("world_nether")?.let { player.teleport(it.spawnLocation) }
        assertEquals(5.0, player.health)
        assertEquals(stack, player.inventory.getItem(0))

        server.getWorld("test")?.let { player.teleport(it.spawnLocation) }
        assertNotEquals(5.0, player.health)
        assertNotEquals(stack, player.inventory.getItem(0))

        assertTrue(player.disconnect())
        player.name = "benthecat10"
        assertTrue(player.reconnect())

        server.getWorld("world")?.let { player.teleport(it.spawnLocation) }
        assertEquals(5.0, player.health)
        assertEquals(stack, player.inventory.getItem(0))

        Thread.sleep(100) // wait for files to save

        // check files
        assertTrue(Path.of(multiverseInventories.dataFolder.absolutePath, "worlds", "world", "benthecat10.json").toFile().exists())
        assertTrue(Path.of(multiverseInventories.dataFolder.absolutePath, "worlds", "world_nether", "benthecat10.json").toFile().exists())
        assertTrue(Path.of(multiverseInventories.dataFolder.absolutePath, "worlds", "test", "benthecat10.json").toFile().exists())
        assertTrue(Path.of(multiverseInventories.dataFolder.absolutePath, "groups", "default", "benthecat10.json").toFile().exists())
        assertTrue(Path.of(multiverseInventories.dataFolder.absolutePath, "groups", "test", "benthecat10.json").toFile().exists())

        assertFalse(Path.of(multiverseInventories.dataFolder.absolutePath, "worlds", "world", "Benji_0224.json").toFile().exists())
        assertFalse(Path.of(multiverseInventories.dataFolder.absolutePath, "worlds", "world_nether", "Benji_0224.json").toFile().exists())
        assertFalse(Path.of(multiverseInventories.dataFolder.absolutePath, "worlds", "test", "Benji_0224.json").toFile().exists())
        assertFalse(Path.of(multiverseInventories.dataFolder.absolutePath, "groups", "default", "Benji_0224.json").toFile().exists())
        assertFalse(Path.of(multiverseInventories.dataFolder.absolutePath, "groups", "test", "Benji_0224.json").toFile().exists())

        // check player profile
        assertEquals("benthecat10", FutureNow.get(profileDataSource.getGlobalProfile(GlobalProfileKey.of(player)))?.lastKnownName)

        // check name mapper updated
        assertEquals(GlobalProfileKey.of(player.uniqueId, "benthecat10"), playerNamesMapper.getKey("benthecat10").orNull)
        assertNull(playerNamesMapper.getKey("Benji_0224").orNull)
    }
}
