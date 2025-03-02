package org.mvplugins.multiverse.inventories

import org.junit.jupiter.api.Test
import org.mvplugins.multiverse.inventories.commands.InventoriesCommand
import org.mvplugins.multiverse.inventories.config.InventoriesConfig
import org.mvplugins.multiverse.inventories.dataimport.DataImportManager
import org.mvplugins.multiverse.inventories.handleshare.ShareHandleListener
import org.mvplugins.multiverse.inventories.profile.ProfileDataSource
import org.mvplugins.multiverse.inventories.profile.container.ProfileContainerStoreProvider
import org.mvplugins.multiverse.inventories.profile.group.WorldGroupManager
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class InjectionTest : TestWithMockBukkit() {

    @Test
    fun `InventoriesCommand are available as a service`() {
        assertEquals(17, serviceLocator.getAllActiveServices(InventoriesCommand::class.java).size)
    }

    @Test
    fun `InventoriesConfig is available as a service`() {
        assertNotNull(serviceLocator.getActiveService(InventoriesConfig::class.java))
    }

    @Test
    fun `InventoriesListener is available as a service`() {
        assertNotNull(serviceLocator.getActiveService(ShareHandleListener::class.java))
    }

    @Test
    fun `ProfileDataSource is available as a service`() {
        assertNotNull(serviceLocator.getActiveService(ProfileDataSource::class.java))
    }

    @Test
    fun `ProfileContainerStoreProvider is available as a service`() {
        assertNotNull(serviceLocator.getActiveService(ProfileContainerStoreProvider::class.java))
    }

    @Test
    fun `WorldGroupManager is available as a service`() {
        assertNotNull(serviceLocator.getActiveService(WorldGroupManager::class.java))
    }

    @Test
    fun `DataImportManager is available as a service`() {
        assertNotNull(serviceLocator.getActiveService(DataImportManager::class.java))
    }
}
