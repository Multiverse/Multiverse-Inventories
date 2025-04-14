package org.mvplugins.multiverse.inventories.profile

import com.dumptruckman.minecraft.util.Logging
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.junit.jupiter.api.Test
import org.mvplugins.multiverse.core.utils.CoreLogging
import org.mvplugins.multiverse.core.world.WorldManager
import org.mvplugins.multiverse.core.world.options.CreateWorldOptions
import org.mvplugins.multiverse.inventories.TestWithMockBukkit
import org.mvplugins.multiverse.inventories.profile.key.ContainerType
import org.mvplugins.multiverse.inventories.profile.key.GlobalProfileKey
import org.mvplugins.multiverse.inventories.profile.key.ProfileKey
import org.mvplugins.multiverse.inventories.profile.key.ProfileTypes
import org.mvplugins.multiverse.inventories.share.Sharables
import org.mvplugins.multiverse.inventories.util.FutureNow
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future
import java.util.function.Consumer
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FilePerformanceTest : TestWithMockBukkit() {

    private lateinit var worldManager: WorldManager
    private lateinit var profileDataSource: ProfileDataSource
    private lateinit var profileCacheManager: ProfileCacheManager

    @BeforeTest
    fun setUp() {
        worldManager = serviceLocator.getActiveService(WorldManager::class.java).takeIf { it != null } ?: run {
            throw IllegalStateException("WorldManager is not available as a service") }
        profileDataSource = serviceLocator.getService(ProfileDataSource::class.java).takeIf { it != null } ?: run {
            throw IllegalStateException("ProfileDataSource is not available as a service") }
        profileCacheManager = serviceLocator.getService(ProfileCacheManager::class.java).takeIf { it != null } ?: run {
            throw IllegalStateException("ProfileCacheManager is not available as a service") }
        CoreLogging.setDebugLevel(0);
        Logging.setDebugLevel(0)
        assertTrue(worldManager.createWorld(CreateWorldOptions.worldName("world")).isSuccess)
        assertTrue(worldManager.createWorld(CreateWorldOptions.worldName("world2")).isSuccess)
    }

    @Test
    fun `Test 1K global profiles`() {
        val startTime = System.nanoTime()
        val futures = ArrayList<Future<Void>>(1000)
        for (i in 0..1000) {
            futures.add(profileDataSource.modifyGlobalProfile(GlobalProfileKey.create(UUID.randomUUID()), { globalProfile ->
                globalProfile.setLoadOnLogin(true)
            }))
        }
        for (future in futures) {
            future.get()
        }
        Logging.info("Time taken: " + (System.nanoTime() - startTime) / 1000000 + "ms")
    }

    @Test
    fun `Test 1K player profiles`() {
        server.setPlayers(1000)
        val startTime = System.nanoTime()
        val futures = ArrayList<Future<Void>>(1000)
        for (i in 0..999) {
            val player = server.getPlayer(i)
            for (gameMode in GameMode.entries) {
                val playerProfile = FutureNow.get(profileDataSource.getPlayerProfile(
                    ProfileKey.create(ContainerType.WORLD, "world", ProfileTypes.forGameMode(gameMode), player.uniqueId)))
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
                futures.add(profileDataSource.updatePlayerProfile(playerProfile))
            }
        }
        for (future in futures) {
            future.get()
        }
        Logging.info("Time taken: " + (System.nanoTime() - startTime) / 1000000 + "ms")
        profileCacheManager.clearAllCache()

        val startTime2 = System.nanoTime()
        val futures2 = ArrayList<CompletableFuture<PlayerProfile>>(1000)
        for (i in 0..999) {
            val player = server.getPlayer(i)
            for (gameMode in GameMode.entries) {
                futures2.add(profileDataSource.getPlayerProfile(
                    ProfileKey.create(ContainerType.WORLD, "world", ProfileTypes.forGameMode(gameMode), player.uniqueId)))
            }
        }
        for (future in futures2) {
            val playerProfile = future.get()
            assertEquals(5.0, playerProfile.get(Sharables.HEALTH))
            assertEquals(ItemStack(Material.STONE_BRICKS, 10), playerProfile.get(Sharables.OFF_HAND))
        }
        Logging.info("Time taken: " + (System.nanoTime() - startTime2) / 1000000 + "ms")

        val startTime3 = System.nanoTime()
        val futures3 = ArrayList<Future<Void>>(1000)
        for (i in 0..999) {
            val player = server.getPlayer(i)
            for (gameMode in GameMode.entries) {
                futures3.add(profileDataSource.deletePlayerProfile(
                    ProfileKey.create(ContainerType.WORLD, "world", ProfileTypes.forGameMode(gameMode), player.uniqueId)))
                val playerProfile = FutureNow.get(profileDataSource.getPlayerProfile(
                    ProfileKey.create(ContainerType.WORLD, "world", ProfileTypes.forGameMode(gameMode), player.uniqueId)))
                assertNull(playerProfile.get(Sharables.HEALTH))
                assertNull(playerProfile.get(Sharables.OFF_HAND))
            }
        }
        for (future in futures3) {
            future.get()
        }
        Logging.info("Time taken: " + (System.nanoTime() - startTime3) / 1000000 + "ms")

        val cacheStats = profileCacheManager.getCacheStats()
        Logging.info(cacheStats.values.toString())
        for (cacheStat in cacheStats) {
            Logging.info(cacheStat.key + ": " + cacheStat.value.averageLoadPenalty() / 1000000 + "ms")
        }
    }

    private fun createItemStack(material: Material, amount: Int = 1, modify: Consumer<ItemStack>): ItemStack {
        val itemStack = ItemStack(material, amount)
        modify.accept(itemStack)
        return itemStack
    }

    @Test
    fun `50 players join the server consecutively`() {
        val startTime = System.nanoTime()
        server.setPlayers(50)
        Logging.info("Time taken: " + (System.nanoTime() - startTime) / 1000000 + "ms")
    }

    @Test
    fun `Teleport 50 players consecutively`() {
        for (i in 0..49) {
            writeResourceToConfigFile("/playerdata.json", "worlds/world2/Player$i.json")
        }
        server.setPlayers(50)
        val startTime = System.nanoTime()
        for (player in server.playerList.onlinePlayers) {
            server.getWorld("world2")?.let { player.teleport(it.spawnLocation) }
        }
        Logging.info("Time taken: " + (System.nanoTime() - startTime) / 1000000 + "ms")
        val cacheStats = profileCacheManager.getCacheStats()
        for (cacheStat in cacheStats) {
            Logging.info(cacheStat.key + ": " + cacheStat.value.averageLoadPenalty() / 1000000 + "ms")
        }
    }
}
