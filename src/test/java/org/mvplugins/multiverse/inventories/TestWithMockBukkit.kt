package org.mvplugins.multiverse.inventories

import org.bukkit.Location
import org.bukkit.configuration.MemorySection
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.configuration.serialization.ConfigurationSerialization
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.inventory.ItemStackMock
import org.mvplugins.multiverse.core.MultiverseCore
import org.mvplugins.multiverse.core.config.MVCoreConfig
import org.mvplugins.multiverse.core.inject.PluginServiceLocator
import org.mvplugins.multiverse.inventories.mock.MVServerMock
import java.io.File
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Basic abstract test class that sets up MockBukkit and MultiverseCore.
 */
abstract class TestWithMockBukkit {

    protected lateinit var server: MVServerMock
    protected lateinit var multiverseCore: MultiverseCore
    protected lateinit var multiverseInventories: MultiverseInventories
    protected lateinit var serviceLocator : PluginServiceLocator

    @BeforeTest
    fun setUpMockBukkit() {
        ConfigurationSerialization.registerClass(ItemStackMock::class.java)

        server = MockBukkit.mock(MVServerMock())
        multiverseCore = MockBukkit.load(MultiverseCore::class.java)
        multiverseCore.serviceLocator.getService(MVCoreConfig::class.java).globalDebug = 3
        multiverseInventories = MockBukkit.load(MultiverseInventories::class.java)
        serviceLocator = multiverseInventories.serviceLocator
        assertNotNull(server.commandMap)
    }

    @AfterTest
    fun tearDownMockBukkit() {
        server.pluginManager.disablePlugin(multiverseInventories)
        server.pluginManager.disablePlugin(multiverseCore)
        MockBukkit.unmock()
    }

    fun getResourceAsText(path: String): String? = object {}.javaClass.getResource(path)?.readText()

    fun writeResourceToConfigFile(resourcePath: String, configPath: String) {
        val configResource = getResourceAsText(resourcePath)
        assertNotNull(configResource)
        File(Path.of(multiverseInventories.dataFolder.absolutePath, configPath).absolutePathString())
            .writeText(configResource)
    }

    fun assertConfigEquals(expectedPath: String, actualPath: String) {
        val actualString = multiverseInventories.dataFolder.toPath().resolve(actualPath).toFile().readText()
        val expectedString = getResourceAsText(expectedPath)
        assertNotNull(expectedString)

        val actualYaml = YamlConfiguration()
        actualYaml.loadFromString(actualString)
        val actualYamlKeys = HashSet(actualYaml.getKeys(true))

        val expectedYaml = YamlConfiguration()
        expectedYaml.loadFromString(expectedString)
        val expectedYamlKeys = HashSet(expectedYaml.getKeys(true))

        for (key in expectedYamlKeys) {
            assertNotNull(actualYamlKeys.remove(key), "Key $key is missing in actual config")
            val actualValue = actualYaml.get(key)
            if (actualValue is MemorySection) {
                continue
            }
            assertEquals(expectedYaml.get(key), actualYaml.get(key), "Value for $key is different.")
        }
        for (key in actualYamlKeys) {
            assertNull(actualYaml.get(key), "Key $key is present in actual config when it should be empty.")
        }

        assertEquals(0, actualYamlKeys.size,
            "Actual config has more keys than expected config. The following keys are missing: $actualYamlKeys")
    }

    fun assertLocationEquals(expected: Location?, actual: Location?) {
        assertEquals(expected?.world, actual?.world, "Worlds don't match for location comparison ($expected, $actual)")
        assertEquals(expected?.x, actual?.x, "X values don't match for location comparison ($expected, $actual)")
        assertEquals(expected?.y, actual?.y, "Y values don't match for location comparison ($expected, $actual)")
        assertEquals(expected?.z, actual?.z, "Z values don't match for location comparison ($expected, $actual)")
        assertEquals(expected?.yaw, actual?.yaw, "Yaw values don't match for location comparison ($expected, $actual)")
        assertEquals(expected?.pitch, actual?.pitch, "Pitch values don't match for location comparison ($expected, $actual)")
    }
}
