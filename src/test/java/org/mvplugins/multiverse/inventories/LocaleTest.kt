package org.mvplugins.multiverse.inventories

import org.mockbukkit.mockbukkit.entity.PlayerMock
import org.mvplugins.multiverse.core.commandtools.MVCommandManager
import org.mvplugins.multiverse.core.locale.message.Message
import org.mvplugins.multiverse.inventories.util.MVInvi18n
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class LocaleTest : TestWithMockBukkit() {

    private lateinit var player: PlayerMock
    private lateinit var commandManager: MVCommandManager

    @BeforeTest
    fun setUp() {
        commandManager = serviceLocator.getService(MVCommandManager::class.java).takeIf { it != null } ?: run {
            throw IllegalStateException("Locales is not available as a service") }
        player = server.addPlayer("Benji_0224")
    }

    @Test
    fun `Test locale message`() {
        assertEquals(
            "a test-string from the resource",
            Message.of(MVInvi18n.TEST_STRING).formatted(commandManager.locales, commandManager.getCommandIssuer(player))
        )
    }
}
