package org.mvplugins.multiverse.inventories.profile

import com.dumptruckman.minecraft.util.Logging
import org.junit.jupiter.api.Test
import org.mvplugins.multiverse.inventories.TestWithMockBukkit
import org.mvplugins.multiverse.inventories.profile.key.ContainerType
import org.mvplugins.multiverse.inventories.profile.key.ProfileKey
import org.mvplugins.multiverse.inventories.profile.key.ProfileTypes
import kotlin.test.BeforeTest

class ProfileDataSourceTest : TestWithMockBukkit() {

    private lateinit var profileDataSource: ProfileDataSource

    @BeforeTest
    fun setUp() {
        profileDataSource = serviceLocator.getService(ProfileDataSource::class.java).takeIf { it != null } ?: run {
            throw IllegalStateException("ProfileDataSource is not available as a service") }
    }

    @Test
    fun `getPlayerData called twice`() {
        server.setPlayers(1)
        writeResourceToConfigFile("/playerdata.json", "worlds/world/Player0.json")
        val key = ProfileKey.of(ContainerType.WORLD, "world", ProfileTypes.SURVIVAL, server.getPlayer("Player0"))
        profileDataSource.getPlayerProfile(key).thenAccept { profile -> Logging.info(profile.toString()) }
        profileDataSource.getPlayerProfile(key).thenAccept { profile -> Logging.info(profile.toString()) }
        profileDataSource.getPlayerProfile(key).thenAccept { profile -> Logging.info(profile.toString()) }
        Logging.info("Getting player data...")
    }
}
