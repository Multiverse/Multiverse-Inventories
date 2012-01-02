package com.onarandombox.multiverseprofiles;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.multiverseprofiles.config.ProfilesConfig;
import com.onarandombox.multiverseprofiles.config.ProfilesConfigImpl;
import com.onarandombox.multiverseprofiles.data.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * @author dumptruckman
 */
public class MultiverseProfiles {

    private MultiverseProfiles() {
    }

    private final int requiresProtocol = 10;

    private MultiverseProfilesPlugin pluginInstance;

    private MultiverseCore core = null;
    private ProfilesConfig config = null;
    private ProfilesData data = null;

    private HashMap<MultiverseWorld, WorldProfile> worldProfiles = new HashMap<MultiverseWorld, WorldProfile>();
    private HashMap<MultiverseWorld, List<WorldGroup>> worldGroups = new HashMap<MultiverseWorld, List<WorldGroup>>();
    private Shares defaultShares = new Shares();

    public MultiverseProfiles(MultiverseProfilesPlugin plugin) {
        this.pluginInstance = plugin;
    }

    public MultiverseProfilesPlugin getPlugin() {
        return pluginInstance;
    }

    public int getRequiredProtocol() {
        return this.requiresProtocol;
    }

    protected void setCore(MultiverseCore core) {
        this.core = core;
    }

    public MultiverseCore getCore() {
        return this.core;
    }

    protected void loadConfig() throws Exception {
        this.config = new ProfilesConfigImpl();
        this.config.load();
    }

    public ProfilesConfig getConfig() {
        return this.config;
    }

    protected void loadData() throws IOException {
        this.data = new ProfilesDataImpl();
        this.data.load();
    }

    public ProfilesData getData() {
        return this.data;
    }

    public void addWorldProfile(WorldProfile worldProfile) {
        this.worldProfiles.put(worldProfile.getMVWorld(), worldProfile);
    }

    public WorldProfile getWorldProfile(MultiverseWorld world) {
        return this.worldProfiles.get(world);
    }

    public HashMap<MultiverseWorld, List<WorldGroup>> getWorldGroups() {
        return this.worldGroups;
    }

    public void setDefaultShares(Shares shares) {
        this.defaultShares = shares;
    }

    //public static Shares getDefaultShares() {
    //    return defaultShares;
    //}
}
