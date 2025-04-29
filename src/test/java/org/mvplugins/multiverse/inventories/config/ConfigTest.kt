package org.mvplugins.multiverse.inventories.config

import org.mvplugins.multiverse.inventories.TestWithMockBukkit
import java.io.File
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

class ConfigTest : TestWithMockBukkit() {

    private lateinit var config : InventoriesConfig
    private lateinit var configFile : File

    @BeforeTest
    fun setUp() {
        configFile = File(Path.of(multiverseInventories.dataFolder.absolutePath, "config.yml").absolutePathString())
        if (configFile.exists()) configFile.delete()

        config = serviceLocator.getActiveService(InventoriesConfig::class.java).takeIf { it != null } ?: run {
            throw IllegalStateException("InventoriesConfig is not available as a service") }

        assertTrue(config.load().isSuccess)
        assertTrue(config.save().isSuccess)
    }

    @Test
    fun `Config is fresh`() {
        assertConfigEquals("/config/fresh_config.yml", "config.yml")
    }

    @Test
    fun `Migrate from old config`() {
        writeResourceToConfigFile("/config/old_config.yml", "config.yml")
        assertTrue(config.load().isSuccess)
        assertTrue(config.save().isSuccess)
        assertConfigEquals("/config/migrated_config.yml", "config.yml")
    }
}
