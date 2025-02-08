package org.mvplugins.multiverse.inventories.profile

import com.dumptruckman.minecraft.util.Logging
import org.junit.jupiter.api.Test
import org.mvplugins.multiverse.inventories.TestWithMockBukkit
import java.util.*
import kotlin.test.BeforeTest

class FilePerformanceTest : TestWithMockBukkit() {

    private lateinit var profileDataSource: ProfileDataSource

    @BeforeTest
    fun setUp() {
        profileDataSource = serviceLocator.getService(ProfileDataSource::class.java).takeIf { it != null } ?: run {
            throw IllegalStateException("ProfileDataSource is not available as a service") }
    }

    @Test
    fun `Test 20K global profiles`() {
        val startTime = System.nanoTime()
        for (i in 1..20000) {
            val globalProfile = profileDataSource.getGlobalProfile("player-$i", UUID.randomUUID())
            globalProfile.setLoadOnLogin(true)
            profileDataSource.updateGlobalProfile(globalProfile)
        }
        Logging.info("Time taken: " + (System.nanoTime() - startTime) / 1000000 + "ms")

        profileDataSource.clearAllCache();

        val startTime2 = System.nanoTime()
        for (i in 1..20000) {
            val globalProfile = profileDataSource.getGlobalProfile("player-$i", UUID.randomUUID())
            globalProfile.setLoadOnLogin(false)
            profileDataSource.updateGlobalProfile(globalProfile)
        }
        Logging.info("Time taken: " + (System.nanoTime() - startTime2) / 1000000 + "ms")
    }
}