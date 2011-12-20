package com.onarandombox.multiverseinventories;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.multiverseinventories.config.MVIConfigImpl;
import com.onarandombox.multiverseinventories.config.MVIConfig;
import com.onarandombox.multiverseinventories.data.MVIData;
import com.onarandombox.multiverseinventories.data.MVIDataImpl;
import com.onarandombox.multiverseinventories.data.MVIWorldGroup;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author dumptruckman
 */
public class MVIManager {

    private static final int requiresProtocol = 10;
    private static JavaPlugin pluginInstance = null;
    private static MultiverseCore core = null;
    private static MVIConfig config = null;
    private static MVIData data = null;
    private static List<MVIWorldGroup> worldGroups = new ArrayList<MVIWorldGroup>();

    protected static void wipeStaticInstances() {
        MVIManager.pluginInstance = null;
        MVIManager.core = null;
        MVIManager.config = null;
        MVIManager.data = null;
        MVIManager.worldGroups = null;
    }

    protected static void setPluginInstance(JavaPlugin plugin) {
        MVIManager.pluginInstance = plugin;
    }

    public static JavaPlugin getPlugin() {
        return pluginInstance;
    }

    protected static int getRequiredProtocol() {
        return MVIManager.requiresProtocol;
    }

    protected static void setCore(MultiverseCore core) {
        MVIManager.core = core;
    }

    public static MultiverseCore getCore() {
        return MVIManager.core;
    }

    protected static void loadConfig() throws Exception {
        MVIManager.config = new MVIConfigImpl();
        MVIManager.config.load();
    }

    public static MVIConfig getConfig() {
        return MVIManager.config;
    }

    protected static void loadData() throws IOException {
        MVIManager.data = new MVIDataImpl();
        MVIManager.data.load();
    }

    public static MVIData getData() {
        return MVIManager.data;
    }

    public static void setWorldGroups(List<MVIWorldGroup> worldGroups) {
        MVIManager.worldGroups = worldGroups;
    }

    public static List<MVIWorldGroup> getWorldGroups() {
        return MVIManager.worldGroups;
    }
}
