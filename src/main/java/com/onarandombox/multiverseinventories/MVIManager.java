package com.onarandombox.multiverseinventories;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.multiverseinventories.config.MVIConfigImpl;
import com.onarandombox.multiverseinventories.config.MVIConfig;
import com.onarandombox.multiverseinventories.data.*;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.HashMap;
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

    private static HashMap<World, WorldProfile> worldProfiles = new HashMap<World, WorldProfile>();
    private static HashMap<World, List<WorldGroup>> worldGroups = new HashMap<World, List<WorldGroup>>();
    private static Shares defaultShares = new Shares();

    protected static void wipeStaticInstances() {
        MVIManager.pluginInstance = null;
        MVIManager.core = null;
        MVIManager.config = null;
        MVIManager.data = null;
        MVIManager.worldProfiles = null;
        MVIManager.worldGroups = null;
        MVIManager.defaultShares = null;
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

    public static WorldProfile getWorldProfile(World world) {
        return MVIManager.worldProfiles.get(world);
    }

    public static HashMap<World, List<WorldGroup>> getWorldGroups() {
        return MVIManager.worldGroups;
    }

    public static void setDefaultShares(Shares shares) {
        MVIManager.defaultShares = shares;
    }

    //public static Shares getDefaultShares() {
    //    return defaultShares;
    //}
}
