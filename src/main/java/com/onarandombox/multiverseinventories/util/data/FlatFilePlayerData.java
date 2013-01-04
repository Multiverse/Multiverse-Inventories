package com.onarandombox.multiverseinventories.util.data;

import com.dumptruckman.minecraft.util.Logging;
import com.feildmaster.lib.configuration.EnhancedConfiguration;
import com.onarandombox.multiverseinventories.ProfileTypes;
import com.onarandombox.multiverseinventories.api.Inventories;
import com.onarandombox.multiverseinventories.api.profile.ContainerType;
import com.onarandombox.multiverseinventories.api.profile.GlobalProfile;
import com.onarandombox.multiverseinventories.api.profile.PlayerData;
import com.onarandombox.multiverseinventories.api.profile.PlayerProfile;
import com.onarandombox.multiverseinventories.api.profile.ProfileType;
import com.onarandombox.multiverseinventories.util.EncodedConfiguration;
import com.onarandombox.multiverseinventories.util.EncodedJsonConfiguration;
import com.onarandombox.multiverseinventories.util.JsonConfiguration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;

/**
 * Implementation of PlayerData.
 */
public class FlatFilePlayerData implements PlayerData {

    private static final String YML = ".yml";
    private static final String JSON = ".json";
    private final File worldFolder;
    private final File groupFolder;
    private final File playerFolder;
    private final Inventories inventories;
    private final FileWriteThread fileWriteThread;

    public FlatFilePlayerData(Inventories plugin) throws IOException {
        this.inventories = plugin;
        // Make the data folders
        plugin.getDataFolder().mkdirs();

        // Check if the data file exists.  If not, create it.
        this.worldFolder = new File(plugin.getDataFolder(), "worlds");
        if (!this.worldFolder.exists()) {
            if (!this.worldFolder.mkdirs()) {
                throw new IOException("Could not create world folder!");
            }
        }
        this.groupFolder = new File(plugin.getDataFolder(), "groups");
        if (!this.groupFolder.exists()) {
            if (!this.groupFolder.mkdirs()) {
                throw new IOException("Could not create group folder!");
            }
        }
        this.playerFolder = new File(plugin.getDataFolder(), "players");
        if (!this.playerFolder.exists()) {
            if (!this.playerFolder.mkdirs()) {
                throw new IOException("Could not create player folder!");
            }
        }
        fileWriteThread = new FileWriteThread();
        fileWriteThread.start();
    }

    private FileConfiguration getConfigHandle(File file) {
        if (file.getName().endsWith(YML)) {
            try {
                return new EncodedConfiguration(file, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                return new EnhancedConfiguration(file);
            }
        } else {
            try {
                return new EncodedJsonConfiguration(file, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                return new JsonConfiguration(file);
            }
        }
    }

    private File getFolder(ContainerType type, String folderName) {
        File folder;
        switch (type) {
            case GROUP:
                folder = new File(this.groupFolder, folderName);
                break;
            case WORLD:
                folder = new File(this.worldFolder, folderName);
                break;
            default:
                folder = new File(this.worldFolder, folderName);
                break;
        }

        if (!folder.exists()) {
            folder.mkdirs();
        }
        Logging.finer("got data folder: " + folder.getPath() + " from type: " + type);
        return folder;
    }

    /**
     * Retrieves the yaml data file for a player based on a given world/group name.
     *
     * @param type       Indicates whether data is for group or world.
     * @param dataName   The name of the group or world.
     * @param playerName The name of the player.
     * @return The yaml data file for a player.
     */
    File getPlayerFile(ContainerType type, String dataName, String playerName) {
        File jsonPlayerFile = new File(this.getFolder(type, dataName), playerName + JSON);
        File playerFile = new File(this.getFolder(type, dataName), playerName + YML);
        if (jsonPlayerFile.exists()) {
            return jsonPlayerFile;
        } else {
            if (playerFile.exists()) {
                try {
                    jsonPlayerFile.createNewFile();
                } catch (IOException ignore) { }
                return playerFile;
            } else {
                try {
                    jsonPlayerFile.createNewFile();
                } catch (IOException e) {
                    Logging.severe("Could not create necessary player file: " + playerName + JSON);
                    Logging.severe("Your data may not be saved!");
                    Logging.severe(e.getMessage());
                }
            }
        }
        return jsonPlayerFile;
    }

    /**
     * Retrieves the yaml data file for a player for their global data.
     *
     * @param playerName The name of the player.
     * @return The yaml data file for a player.
     */
    File getGlobalFile(String playerName, boolean forceJson) {
        File jsonPlayerFile = new File(playerFolder, playerName + JSON);
        File playerFile = new File(playerFolder, playerName + YML);
        if (!jsonPlayerFile.exists()) {
            if (forceJson) {
                try {
                    jsonPlayerFile.createNewFile();
                } catch (IOException e) {
                    Logging.severe("Could not create necessary player file: " + playerName + YML);
                    Logging.severe("Your data may not be saved!");
                    Logging.severe(e.getMessage());
                }
                return jsonPlayerFile;
            }
            if (playerFile.exists()) {
                return playerFile;
            }
        }
        if (!playerFile.exists()) {
            try {
                playerFile.createNewFile();
            } catch (IOException e) {
                Logging.severe("Could not create necessary player file: " + playerName + YML);
                Logging.severe("Your data may not be saved!");
                Logging.severe(e.getMessage());
            }
        }
        return playerFile;
    }

    private String getPlayerName(File playerFile) {
        if (playerFile.getName().endsWith(YML)) {
            String fileName = playerFile.getName();
            return fileName.substring(0, fileName.length() - YML.length());
        } else {
            return null;
        }
    }

    /*
    private File[] getWorldFolders() {
        return this.worldFolder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(YML);
            }
        });
    }
    */

    private class FileWriteThread extends Thread {

        FileWriteThread() {
            super("MV-Inv Profile Write Thread");
        }

        private final BlockingQueue<PlayerProfile> profileWriteQueue = new LinkedBlockingQueue<PlayerProfile>();
        private final BlockingQueue<PlayerProfile> waitingQueue = new LinkedBlockingQueue<PlayerProfile>();
        
        private volatile boolean waiting = false;

        @Override
        public void run() {
            while(true) {
                try {
                    final PlayerProfile profile = profileWriteQueue.take();
                    processProfileWrite(profile);
                } catch (InterruptedException ignore) { }
            }
        }

        public void queue(final PlayerProfile profile) {
            try {
                final PlayerProfile clonedProfile = profile.clone();
                if (waiting) {
                    waitingQueue.add(clonedProfile);
                } else {
                    profileWriteQueue.add(clonedProfile);
                }
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }

        public void waitUntilEmpty() {
            waiting = true;
            while(!profileWriteQueue.isEmpty()) { }
            waiting = false;
            profileWriteQueue.addAll(waitingQueue);
            waitingQueue.clear();
        }
    }

    private void processProfileWrite(PlayerProfile playerProfile) {
        try {
            File playerFile = this.getPlayerFile(playerProfile.getContainerType(),
                    playerProfile.getContainerName(), playerProfile.getPlayer().getName());
            FileConfiguration playerData = this.getConfigHandle(playerFile);
            playerData.createSection(playerProfile.getProfileType().getName(), playerProfile.serialize());
            try {
                playerData.save(playerFile);
            } catch (IOException e) {
                Logging.severe("Could not save data for player: " + playerProfile.getPlayer().getName()
                        + " for " + playerProfile.getContainerType().toString() + ": " + playerProfile.getContainerName());
                Logging.severe(e.getMessage());
            }
        } catch (final Exception e) {
            Logging.getLogger().log(Level.WARNING, "Error while attempting to write profile data.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updatePlayerData(PlayerProfile playerProfile) {
        fileWriteThread.queue(playerProfile);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PlayerProfile getPlayerData(ContainerType containerType, String dataName, ProfileType profileType, String playerName) {
        fileWriteThread.waitUntilEmpty();
        File playerFile = this.getPlayerFile(containerType, dataName, playerName);
        FileConfiguration playerData = this.getConfigHandle(playerFile);
        if (convertConfig(playerData)) {
            try {
                playerData.save(playerFile);
            } catch (IOException e) {
                Logging.severe("Could not save data for player: " + playerName
                        + " for " + containerType.toString() + ": " + dataName + " after conversion.");
                Logging.severe(e.getMessage());
            }
        }
        ConfigurationSection section = playerData.getConfigurationSection(profileType.getName());
        if (section == null) {
            section = playerData.createSection(profileType.getName());
        }
        return new DefaultPlayerProfile(containerType, dataName, profileType, playerName, convertSection(section));
    }

    private boolean convertConfig(FileConfiguration config) {
        ConfigurationSection section = config.getConfigurationSection("playerData");
        if (section != null) {
            config.set(ProfileTypes.SURVIVAL.getName(), section);
            config.set(ProfileTypes.CREATIVE.getName(), section);
            config.set(ProfileTypes.ADVENTURE.getName(), section);
            config.set("playerData", null);
            Logging.finer("Migrated old player data to new multi-profile format");
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removePlayerData(ContainerType containerType, String dataName, ProfileType profileType, String playerName) {
        if (profileType == null) {
            File playerFile = this.getPlayerFile(containerType, dataName, playerName);
            return playerFile.delete();
        } else {
            File playerFile = this.getPlayerFile(containerType, dataName, playerName);
            FileConfiguration playerData = this.getConfigHandle(playerFile);
            playerData.set(profileType.getName(), null);
            try {
                playerData.save(playerFile);
            } catch (IOException e) {
                Logging.severe("Could not delete data for player: " + playerName
                        + " for " + containerType.toString() + ": " + dataName);
                Logging.severe(e.getMessage());
                return false;
            }
            return true;
        }
    }

    private Map<String, Object> convertSection(ConfigurationSection section) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        for (String key : section.getKeys(false)) {
            Object obj = section.get(key);
            if (obj instanceof ConfigurationSection) {
                resultMap.put(key, convertSection((ConfigurationSection) obj));
            } else {
                resultMap.put(key, obj);
            }
        }
        return resultMap;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GlobalProfile getGlobalProfile(String playerName) {
        // TODO use data caching to avoid excess object creation.
        File playerFile = this.getGlobalFile(playerName, false);
        FileConfiguration playerData = this.getConfigHandle(playerFile);
        ConfigurationSection section = playerData.getConfigurationSection("playerData");
        if (section == null) {
            section = playerData.createSection("playerData");
        }
        return new DefaultGlobalProfile(playerName, convertSection(section));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean updateGlobalProfile(GlobalProfile globalProfile) {
        File playerFile = this.getGlobalFile(globalProfile.getName(), true);
        FileConfiguration playerData = this.getConfigHandle(playerFile);
        playerData.createSection("playerData", globalProfile.serialize());
        try {
            playerData.save(playerFile);
        } catch (IOException e) {
            Logging.severe("Could not save global data for player: " + globalProfile.getName());
            Logging.severe(e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public void updateWorld(String playerName, String worldName) {
        GlobalProfile globalProfile = getGlobalProfile(playerName);
        globalProfile.setWorld(worldName);
        updateGlobalProfile(globalProfile);
    }

    @Override
    public void setLoadOnLogin(final String playerName, final boolean loadOnLogin) {
        final GlobalProfile globalProfile = getGlobalProfile(playerName);
        globalProfile.setLoadOnLogin(loadOnLogin);
        updateGlobalProfile(globalProfile);
    }

    /*
    @Override
    public void updateProfileType(String playerName, ProfileType profileType) {
        GlobalProfile globalProfile = getGlobalProfile(playerName);
        globalProfile.setProfileType(profileType);
        updateGlobalProfile(playerName, globalProfile);
    }
    */
}

