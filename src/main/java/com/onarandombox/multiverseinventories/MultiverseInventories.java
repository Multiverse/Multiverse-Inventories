package com.onarandombox.multiverseinventories;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVPlugin;
import com.onarandombox.multiverseinventories.config.MIConfig;
import com.onarandombox.multiverseinventories.config.SimpleMIConfig;
import com.onarandombox.multiverseinventories.data.MIData;
import com.onarandombox.multiverseinventories.data.SimpleMIData;
import com.onarandombox.multiverseinventories.listener.MIPlayerListener;
import com.onarandombox.multiverseinventories.locale.Messager;
import com.onarandombox.multiverseinventories.locale.Messaging;
import com.onarandombox.multiverseinventories.locale.MultiverseMessage;
import com.onarandombox.multiverseinventories.locale.SimpleMessager;
import com.onarandombox.multiverseinventories.permission.MIPerms;
import com.onarandombox.multiverseinventories.player.PlayerProfile;
import com.onarandombox.multiverseinventories.util.MIDebug;
import com.onarandombox.multiverseinventories.util.MILog;
import com.onarandombox.multiverseinventories.world.*;
import com.pneumaticraft.commandhandler.CommandHandler;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

/**
 * @author dumptruckman
 */
public class MultiverseInventories extends JavaPlugin implements MVPlugin, Messaging {

    private final Shares defaultShares = new SimpleShares(
            Sharing.FALSE, Sharing.FALSE, Sharing.FALSE, Sharing.FALSE, Sharing.FALSE);

    protected CommandHandler commandHandler;
    private final int requiresProtocol = 9;
    private MultiverseCore core = null;

    private final MIPlayerListener playerListener = new MIPlayerListener(this);

    private MIConfig config = null;
    private MIData data = null;

    private Messager messager = new SimpleMessager(this);

    private HashMap<String, WorldProfile> worldProfiles = new HashMap<String, WorldProfile>();
    private HashMap<String, List<WorldGroup>> worldGroups = new HashMap<String, List<WorldGroup>>();

    final public void onDisable() {
        // Display disable message/version info
        MILog.info("disabled.", true);
    }

    final public void onEnable() {
        MILog.init(this);
        MIPerms.register(this);

        MultiverseCore core;
        core = (MultiverseCore) this.getServer().getPluginManager().getPlugin("Multiverse-Core");
        // Test if the Core was found, if not we'll disable this plugin.
        if (core == null) {
            MILog.info("Multiverse-Core not found, will keep looking.");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }
        this.setCore(core);

        if (this.getCore().getProtocolVersion() < this.getRequiredProtocol()) {
            MILog.severe("Your Multiverse-Core is OUT OF DATE");
            MILog.severe("This version of Profiles requires Protocol Level: " + this.getRequiredProtocol());
            MILog.severe("Your of Core Protocol Level is: " + this.getCore().getProtocolVersion());
            MILog.severe("Grab an updated copy at: ");
            MILog.severe("http://bukkit.onarandombox.com/?dir=multiverse-core");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        MIDebug.init(this);

        // Get world groups from config
        this.worldGroups = this.getConf().getWorldGroups();
        // Set debug mode from config
        MILog.setDebugMode(this.getConf().isDebugging());

        try {
            this.getMessager().setLocale(new Locale(this.getConf().getLocale()));
        } catch (IllegalArgumentException e) {
            MILog.severe(e.getMessage());
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Initialize data class
        this.worldProfiles = this.getData().getWorldProfiles();

        this.getCore().incrementPluginCount();

        // Register Events
        this.registerEvents();

        // Display enable message/version info
        MILog.info("enabled.", true);
    }

    private void registerEvents() {
        final PluginManager pm = getServer().getPluginManager();
        // Event registering goes here
        pm.registerEvent(Event.Type.PLAYER_CHANGED_WORLD, playerListener, Event.Priority.Normal, this);
    }

    public void log(Level level, String msg) {
        MILog.log(level, msg, false);
        MIDebug.log(level, msg);
    }

    public MultiverseCore getCore() {
        return this.core;
    }

    public void setCore(MultiverseCore core) {
        this.core = core;
    }

    public int getProtocolVersion() {
        return 1;
    }

    public String dumpVersionInfo(String buffer) {
        buffer += this.logAndAddToPasteBinBuffer("Multiverse-Inventories Version: " + this.getDescription().getVersion());
        buffer += this.logAndAddToPasteBinBuffer("Bukkit Version: " + this.getServer().getVersion());
        buffer += this.logAndAddToPasteBinBuffer("Special Code: FRN001");
        return buffer;
    }

    private String logAndAddToPasteBinBuffer(String string) {
        MILog.info(string);
        return MILog.getString(string + "\n", false);
    }

    public MIConfig getConf() {
        if (this.config == null) {
            // Loads the configuration
            try {
                this.config = new SimpleMIConfig(this);
            } catch (Exception e) {  // Catch errors loading the config file and exit out if found.
                MILog.severe(this.getMessager().getMessage(MultiverseMessage.ERROR_CONFIG_LOAD));
                MILog.severe(e.getMessage());
                Bukkit.getPluginManager().disablePlugin(this);
                return null;
            }
        }
        return this.config;
    }

    public MIData getData() {
        if (this.data == null) {
            // Loads the data
            try {
                this.data = new SimpleMIData(this);
            } catch (IOException e) {  // Catch errors loading the language file and exit out if found.
                MILog.severe(this.getMessager().getMessage(MultiverseMessage.ERROR_DATA_LOAD));
                MILog.severe(e.getMessage());
                Bukkit.getPluginManager().disablePlugin(this);
                return null;
            }
        }
        return this.data;
    }

    /**
     * {@inheritDoc}
     */
    public Messager getMessager() {
        return messager;
    }

    /**
     * {@inheritDoc}
     */
    public void setMessager(Messager messager) {
        if (messager == null)
            throw new IllegalArgumentException("The new messager can't be null!");

        this.messager = messager;
    }

    public int getRequiredProtocol() {
        return this.requiresProtocol;
    }

    public void addWorldProfile(WorldProfile worldProfile) {
        this.worldProfiles.put(worldProfile.getWorld(), worldProfile);
    }

    public WorldProfile getWorldProfile(String worldName) {
        WorldProfile worldProfile = this.worldProfiles.get(worldName);
        if (worldProfile == null) {
            worldProfile = new SimpleWorldProfile(worldName);
            this.worldProfiles.put(worldName, worldProfile);
        }
        return worldProfile;
    }

    public HashMap<String, List<WorldGroup>> getWorldGroups() {
        return this.worldGroups;
    }

    public Shares getDefaultShares() {
        return defaultShares;
    }

    public void handleSharing(Player player, World fromWorld, World toWorld, Shares shares) {
        WorldProfile fromWorldProfile = this.getWorldProfile(fromWorld.getName());
        PlayerProfile fromWorldPlayerProfile = fromWorldProfile.getPlayerData(player);
        WorldProfile toWorldProfile = this.getWorldProfile(toWorld.getName());
        PlayerProfile toWorldPlayerProfile = toWorldProfile.getPlayerData(player);

        // persist current stats for previous world if not sharing
        // then load any saved data
        if (shares.isSharingInventory() != Sharing.TRUE) {
            fromWorldPlayerProfile.setInventoryContents(player.getInventory().getContents());
            fromWorldPlayerProfile.setArmorContents(player.getInventory().getArmorContents());
            player.getInventory().clear();
            player.getInventory().setContents(toWorldPlayerProfile.getInventoryContents());
            player.getInventory().setArmorContents(toWorldPlayerProfile.getArmorContents());
        }
        if (shares.isSharingHealth() != Sharing.TRUE) {
            fromWorldPlayerProfile.setHealth(player.getHealth());
            player.setHealth(toWorldPlayerProfile.getHealth());
        }
        if (shares.isSharingHunger() != Sharing.TRUE) {
            fromWorldPlayerProfile.setFoodLevel(player.getFoodLevel());
            fromWorldPlayerProfile.setExhaustion(player.getExhaustion());
            fromWorldPlayerProfile.setSaturation(player.getSaturation());
            player.setFoodLevel(toWorldPlayerProfile.getFoodLevel());
            player.setExhaustion(toWorldPlayerProfile.getExhaustion());
            player.setSaturation(toWorldPlayerProfile.getSaturation());
        }
        if (shares.isSharingExp() != Sharing.TRUE) {
            fromWorldPlayerProfile.setExp(player.getExp());
            fromWorldPlayerProfile.setLevel(player.getLevel());
            player.setExp(toWorldPlayerProfile.getExp());
            player.setLevel(toWorldPlayerProfile.getLevel());
        }
        if (shares.isSharingEffects() != Sharing.TRUE) {
            // Where is the effects API??
        }

        String playerDataPath = getPlayerDataString(fromWorldProfile, fromWorldPlayerProfile);
        ConfigurationSection section = this.getData().getData().getConfigurationSection(playerDataPath);
        if (section == null) {
            section = this.getData().getData().createSection(playerDataPath);
        }
        fromWorldPlayerProfile.serialize(section);

        playerDataPath = getPlayerDataString(toWorldProfile, toWorldPlayerProfile);
        section = this.getData().getData().getConfigurationSection(playerDataPath);
        if (section == null) {
            section = this.getData().getData().createSection(playerDataPath);
        }
        toWorldPlayerProfile.serialize(section);
        this.getData().save();
    }

    private String getPlayerDataString(WorldProfile worldProfile, PlayerProfile playerProfile) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(worldProfile.getWorld());
        stringBuilder.append(".playerData.");
        stringBuilder.append(playerProfile.getPlayer().getName());
        return stringBuilder.toString();
    }
}
