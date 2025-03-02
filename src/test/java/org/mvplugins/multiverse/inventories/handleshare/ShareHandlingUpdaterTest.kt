package org.mvplugins.multiverse.inventories.handleshare

import org.mockbukkit.mockbukkit.entity.PlayerMock
import org.mvplugins.multiverse.inventories.TestWithMockBukkit
import org.mvplugins.multiverse.inventories.profile.ProfileDataSource
import org.mvplugins.multiverse.inventories.profile.ProfileKey
import org.mvplugins.multiverse.inventories.profile.ProfileTypes
import org.mvplugins.multiverse.inventories.profile.container.ContainerType
import org.mvplugins.multiverse.inventories.share.Sharables
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ShareHandlingUpdaterTest : TestWithMockBukkit() {

    private lateinit var profileDataSource: ProfileDataSource
    private lateinit var player: PlayerMock

    @BeforeTest
    fun setUp() {
        player = server.addPlayer("benthecat10")
        profileDataSource = serviceLocator.getService(ProfileDataSource::class.java).takeIf { it != null } ?: run {
            throw IllegalStateException("ProfileDataSource is not available as a service")
        }
    }

    @Test
    fun `Test updating profile`() {
        player.health = 4.4
        player.maxHealth = 15.1

        val playerProfile = profileDataSource.getPlayerDataNow(
            ProfileKey.create(ContainerType.WORLD, "world", ProfileTypes.SURVIVAL, player.uniqueId))
        ShareHandlingUpdater.updateProfile(multiverseInventories, player, PersistingProfile(Sharables.allOf(), playerProfile))

        assertEquals(4.4, playerProfile.get(Sharables.HEALTH))
        assertEquals(15.1, playerProfile.get(Sharables.MAX_HEALTH))
    }

    @Test
    fun `Test updating player`() {
        val playerProfile = profileDataSource.getPlayerDataNow(
            ProfileKey.create(ContainerType.WORLD, "world", ProfileTypes.SURVIVAL, player.uniqueId))
        playerProfile.set(Sharables.HEALTH, 4.4)
        playerProfile.set(Sharables.MAX_HEALTH, 15.1)

        ShareHandlingUpdater.updatePlayer(multiverseInventories, player, PersistingProfile(Sharables.allOf(), playerProfile))

        assertEquals(4.4, player.health)
        assertEquals(15.1, player.maxHealth)
    }
}
