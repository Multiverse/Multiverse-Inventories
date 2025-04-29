package org.mvplugins.multiverse.inventories

import kotlin.test.Test
import kotlin.test.assertNotNull

open class MockBukkitTest : TestWithMockBukkit() {

    @Test
    fun `MockBukkit loads the plugin`() {
        assertNotNull(multiverseInventories)
    }
}
