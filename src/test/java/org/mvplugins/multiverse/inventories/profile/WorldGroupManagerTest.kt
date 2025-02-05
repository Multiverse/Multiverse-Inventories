package org.mvplugins.multiverse.inventories.profile

import org.mvplugins.multiverse.core.world.WorldManager
import org.mvplugins.multiverse.core.world.options.CreateWorldOptions
import org.mvplugins.multiverse.inventories.TestWithMockBukkit
import org.mvplugins.multiverse.inventories.profile.group.WorldGroupManager
import org.mvplugins.multiverse.inventories.share.Sharables
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WorldGroupManagerTest : TestWithMockBukkit() {

    private lateinit var worldGroupManager: WorldGroupManager

    @BeforeTest
    fun setUp() {
        worldGroupManager =
            serviceLocator.getActiveService(WorldGroupManager::class.java).takeIf { it != null } ?: run {
                throw IllegalStateException("WorldGroupManager is not available as a service")
            }

        val worldManager = serviceLocator.getActiveService(WorldManager::class.java).takeIf { it != null } ?: run {
            throw IllegalStateException("WorldManager is not available as a service")
        }
        assertTrue(worldManager.createWorld(CreateWorldOptions.worldName("world")).isSuccess)
        assertTrue(worldManager.createWorld(CreateWorldOptions.worldName("world_nether")).isSuccess)
    }

    @Test
    fun `First run creates default group`() {
        writeResourceToConfigFile("/group/empty.yml", "groups.yml")
        assertTrue(worldGroupManager.load().isSuccess)
        worldGroupManager.createDefaultGroup()
        assertEquals("default", worldGroupManager.defaultGroup.name)
        assertEquals(setOf("world", "world_nether"), worldGroupManager.defaultGroup.worlds)
        assertConfigEquals("/group/default_group.yml", "groups.yml")
    }

    @Test
    fun `Create a new group`() {
        val group = worldGroupManager.newEmptyGroup("test")
        group.addWorld("test1")
        group.addWorld("test2")
        group.shares.setSharing(Sharables.ALL_INVENTORY, true)
        worldGroupManager.updateGroup(group)
        assertConfigEquals("/group/test_group.yml", "groups.yml")
    }
}
