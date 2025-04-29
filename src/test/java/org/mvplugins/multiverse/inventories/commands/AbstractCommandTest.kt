package org.mvplugins.multiverse.inventories.commands

import org.mockbukkit.mockbukkit.command.ConsoleCommandSenderMock
import org.mockbukkit.mockbukkit.entity.PlayerMock
import org.mvplugins.multiverse.inventories.TestWithMockBukkit
import kotlin.test.BeforeTest

abstract class AbstractCommandTest : TestWithMockBukkit() {

    protected lateinit var console: ConsoleCommandSenderMock
    protected lateinit var player: PlayerMock

    @BeforeTest
    fun setUpCommand() {
        console = server.consoleSender
        player = server.addPlayer("benwoo1110");
    }
}
