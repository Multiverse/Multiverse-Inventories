package org.mvplugins.multiverse.inventories.profile

import com.dumptruckman.minecraft.util.Logging
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.junit.jupiter.api.Test
import org.mvplugins.multiverse.inventories.TestWithMockBukkit
import org.mvplugins.multiverse.inventories.profile.container.ContainerType
import org.mvplugins.multiverse.inventories.share.Sharables
import java.util.*
import java.util.function.Consumer
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FilePerformanceTest : TestWithMockBukkit() {

    private lateinit var profileDataSource: ProfileDataSource

    @BeforeTest
    fun setUp() {
        profileDataSource = serviceLocator.getService(ProfileDataSource::class.java).takeIf { it != null } ?: run {
            throw IllegalStateException("ProfileDataSource is not available as a service") }
        Logging.setDebugLevel(0)
    }

    @Test
    fun `Test 10K global profiles`() {
        val startTime = System.nanoTime()
        for (i in 0..9999) {
            val globalProfile = profileDataSource.getGlobalProfile("player-$i", UUID.randomUUID())
            globalProfile.setLoadOnLogin(true)
            profileDataSource.updateGlobalProfile(globalProfile)
        }
        Logging.info("Time taken: " + (System.nanoTime() - startTime) / 1000000 + "ms")

        profileDataSource.clearAllCache();
        Thread.sleep(800) // Wait for files to write finish

        val startTime2 = System.nanoTime()
        for (i in 0..9999) {
            val globalProfile = profileDataSource.getGlobalProfile("player-$i", UUID.randomUUID())
            globalProfile.setLoadOnLogin(false)
            profileDataSource.updateGlobalProfile(globalProfile)
        }
        Logging.info("Time taken: " + (System.nanoTime() - startTime2) / 1000000 + "ms")
    }

    @Test
    fun `Test 1K player profiles`() {
        server.setPlayers(1000)
        val startTime = System.nanoTime()
        for (i in 0..999) {
            val player = server.getPlayer(i)
            for (gameMode in GameMode.entries) {
                val playerProfile = profileDataSource.getPlayerData(
                    ContainerType.WORLD, "world", ProfileTypes.forGameMode(gameMode), player.uniqueId)
                playerProfile.set(Sharables.HEALTH, 5.0)
                playerProfile.set(Sharables.OFF_HAND, ItemStack(Material.STONE_BRICKS, 10))
                playerProfile.set(Sharables.INVENTORY, arrayOf(
                    ItemStack(Material.STONE_BRICKS, 10),
                    ItemStack(Material.ACACIA_LOG, 10),
                    createItemStack(Material.BOW, 1, { itemStack ->
                        itemStack.addEnchantment(Enchantment.UNBREAKING, 2)
                    }),
                    ItemStack(Material.WATER_BUCKET, 64)
                ))
                playerProfile.set(Sharables.POTIONS, arrayOf(
                    PotionEffect(PotionEffectType.POISON, 100, 1),
                    PotionEffect(PotionEffectType.SPEED, 50, 1),
                ))
                profileDataSource.updatePlayerData(playerProfile)
            }
        }
        Logging.info("Time taken: " + (System.nanoTime() - startTime) / 1000000 + "ms")

        profileDataSource.clearAllCache()
        Thread.sleep(800) // Wait for files to write finish

        val startTime2 = System.nanoTime()
        for (i in 0..999) {
            val player = server.getPlayer(i)
            for (gameMode in GameMode.entries) {
                val playerProfile = profileDataSource.getPlayerData(
                    ContainerType.WORLD, "world", ProfileTypes.forGameMode(gameMode), player.uniqueId
                )
                assertEquals(5.0, playerProfile.get(Sharables.HEALTH))
                assertEquals(ItemStack(Material.STONE_BRICKS, 10), playerProfile.get(Sharables.OFF_HAND))
            }
        }
        Logging.info("Time taken: " + (System.nanoTime() - startTime2) / 1000000 + "ms")

        val startTime3 = System.nanoTime()
        for (i in 0..999) {
            val player = server.getPlayer(i)
            for (gameMode in GameMode.entries) {
                profileDataSource.removePlayerData(
                    ContainerType.WORLD, "world", ProfileTypes.forGameMode(gameMode), player.uniqueId
                )
                val playerProfile = profileDataSource.getPlayerData(
                    ContainerType.WORLD, "world", ProfileTypes.forGameMode(gameMode), player.uniqueId
                )
                assertNull(playerProfile.get(Sharables.HEALTH))
                assertNull(playerProfile.get(Sharables.OFF_HAND))
            }
        }
        Logging.info("Time taken: " + (System.nanoTime() - startTime3) / 1000000 + "ms")
    }

    fun createItemStack(material: Material, amount: Int = 1, modify: Consumer<ItemStack>): ItemStack {
        val itemStack = ItemStack(material, amount)
        modify.accept(itemStack)
        return itemStack
    }
}
