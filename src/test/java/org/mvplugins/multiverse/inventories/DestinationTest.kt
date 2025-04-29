package org.mvplugins.multiverse.inventories

import org.mockbukkit.mockbukkit.entity.PlayerMock
import org.mvplugins.multiverse.core.destination.DestinationsProvider
import org.mvplugins.multiverse.core.world.WorldManager
import org.mvplugins.multiverse.core.world.options.CreateWorldOptions
import kotlin.test.BeforeTest
import kotlin.test.Test

class DestinationTest : TestWithMockBukkit() {

    lateinit var destinationsProvider: DestinationsProvider
    lateinit var player : PlayerMock

    @BeforeTest
    fun setUp() {
        destinationsProvider = serviceLocator.getService(DestinationsProvider::class.java).takeIf { it != null } ?: run {
            throw IllegalStateException("DestinationsProvider is not available as a service") }
        val worldManager = serviceLocator.getActiveService(WorldManager::class.java).takeIf { it != null } ?: run {
            throw IllegalStateException("WorldManager is not available as a service") }
        worldManager.createWorld(CreateWorldOptions.worldName("world"))
        worldManager.createWorld(CreateWorldOptions.worldName("world2"))
        player = server.addPlayer("Benji_0224")
    }

    @Test
    fun `Last location destination`() {
        destinationsProvider.parseDestination("ll:world").peek { it.getLocation(player) }
        destinationsProvider.parseDestination("ll:world2").peek { it.getLocation(player) }
    }
}