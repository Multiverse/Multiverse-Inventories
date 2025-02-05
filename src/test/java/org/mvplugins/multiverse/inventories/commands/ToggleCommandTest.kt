package org.mvplugins.multiverse.inventories.commands

import org.mvplugins.multiverse.inventories.config.InventoriesConfig
import org.mvplugins.multiverse.inventories.share.Sharables
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ToggleCommandTest : AbstractCommandTest() {

    private lateinit var config : InventoriesConfig

    @BeforeTest
    fun setUp() {
        config = serviceLocator.getActiveService(InventoriesConfig::class.java).takeIf { it != null } ?: run {
            throw IllegalStateException("InventoriesConfig is not available as a service") }
    }

    @Test
    fun `Toggle last_location on and off`() {
        assertFalse(config.optionalShares.contains(Sharables.LAST_LOCATION))
        server.dispatchCommand(console, "mvinv toggle last_location")
        assertTrue(config.optionalShares.contains(Sharables.LAST_LOCATION))
        server.dispatchCommand(console, "mvinv toggle last_location")
        assertFalse(config.optionalShares.contains(Sharables.LAST_LOCATION))
    }

    @Test
    fun `Toggle economy on and off`() {
        assertFalse(config.optionalShares.contains(Sharables.ECONOMY))
        server.dispatchCommand(console, "mvinv toggle economy")
        assertTrue(config.optionalShares.contains(Sharables.ECONOMY))
        server.dispatchCommand(console, "mvinv toggle economy")
        assertFalse(config.optionalShares.contains(Sharables.ECONOMY))
    }
}
