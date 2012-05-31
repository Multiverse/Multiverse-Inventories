package com.onarandombox.multiverseinventories;

import com.onarandombox.multiverseinventories.api.profile.ProfileType;
import com.onarandombox.multiverseinventories.api.profile.ProfileTypeManager;
import com.onarandombox.multiverseinventories.api.share.Shares;

import java.io.File;
import java.util.Collection;
import java.util.Collections;

/**
 * Default implementation of the ProfileTypeManager.
 */
class DefaultProfileTypeManager implements ProfileTypeManager {

    //private CommentedYamlConfiguration profileConfig;

    DefaultProfileTypeManager(File profileFile) {
        //this.profileConfig = new CommentedYamlConfiguration(profileFile, false);
        //this.profileConfig.load();
        //setDefaults();
        //loadProfileTypes();
    }

    private void setDefaults() {
        /*
        if (!this.profileConfig.getConfig().isSet("profile_types")) {
            ConfigurationSection section = this.profileConfig.getConfig().createSection("profile_types");
            section.createSection(ProfileTypes.SURVIVAL.getName())
                    .set("shares", ProfileTypes.SURVIVAL.getShares().toStringList());
            section.createSection(ProfileTypes.CREATIVE.getName())
                    .set("shares", ProfileTypes.CREATIVE.getShares().toStringList());
        }
        String nl = System.getProperty("line.separator");
        this.profileConfig.getConfig().options()
                .header("Here you may set the shares that are used for different profile types."
                + nl + "Profile types are used for things like separate inventories/stats for creative mode."
                + nl + "The shares set for a profile indicate the data that will be saved for the profile type."
                + nl + ProfileTypes.SURVIVAL.getName() + " is the default data, it is recommended to leave this sharing 'all'."
                + nl + ProfileTypes.CREATIVE.getName() + " indicates what will be used when switching data based on game mode.");
        if (!this.profileConfig.save()) {
            Logging.severe("Unable to save profile types!");
        }
        */
    }

    private void loadProfileTypes() {
        /*
        ConfigurationSection section = this.profileConfig.getConfig().getConfigurationSection("profile_types");
        for (String key : section.getKeys(false)) {
            List sharesList = section.getList(key + ".shares");
            if (sharesList != null) {
                ProfileTypes.registerProfileType(key, Sharables.fromList(sharesList));
            }
        }
        */
    }

    @Override
    public void registerProfileType(String name, Shares shares) {
        ProfileTypes.registerProfileType(name, shares);
        //saveProfileTypes();
    }

    @Override
    public ProfileType lookupType(String name) {
        return ProfileTypes.lookupType(name, false);
    }

    private void saveProfileTypes() {
        /*
        Collection<ProfileType> profileTypes = ProfileTypes.getProfileTypes();
        Map<String, Object> toSave = new HashMap<String, Object>(profileTypes.size());
        for (ProfileType profileType : profileTypes) {
            Map<String, Object> data = new HashMap<String, Object>(1);
            data.put("shares", profileType.getShares().toStringList());
            toSave.put(profileType.getName(), data);
        }
        this.profileConfig.getConfig().set("profile_types", toSave);
        this.profileConfig.save();
        */
    }

    @Override
    public Collection<ProfileType> getProfileTypes() {
        return Collections.unmodifiableCollection(ProfileTypes.getProfileTypes());
    }
}
