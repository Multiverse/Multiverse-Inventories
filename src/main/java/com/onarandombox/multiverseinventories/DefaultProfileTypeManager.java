package com.onarandombox.multiverseinventories;

import com.onarandombox.multiverseinventories.api.profile.ProfileType;
import com.onarandombox.multiverseinventories.api.profile.ProfileTypeManager;
import com.onarandombox.multiverseinventories.api.profile.ProfileTypes;
import com.onarandombox.multiverseinventories.api.share.Sharables;
import com.onarandombox.multiverseinventories.util.CommentedYamlConfiguration;
import com.onarandombox.multiverseinventories.util.Logging;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.util.List;

class DefaultProfileTypeManager extends ProfileTypeManager {

    private CommentedYamlConfiguration profileConfig;

    DefaultProfileTypeManager(File profileFile) {
        this.profileConfig = new CommentedYamlConfiguration(profileFile, false);
        this.profileConfig.load();
        setDefaults();
        loadProfileTypes();
    }

    private void setDefaults() {
        if (!this.profileConfig.getConfig().isSet("profile_types")) {
            ConfigurationSection section = this.profileConfig.getConfig().createSection("profile_types");
            section.createSection("creative_mode").set("shares", ProfileType.CREATIVE.getShares().toStringList());
        }
        this.profileConfig.getConfig().options().header("Here you may set the shares that are used for different profile types.");
        if (!this.profileConfig.save()) {
            Logging.severe("Unable to save profile types!");
        }
    }

    private void loadProfileTypes() {
        ConfigurationSection section = this.profileConfig.getConfig().getConfigurationSection("profile_types");
        for (String key : section.getKeys(false)) {
            List sharesList = section.getList(key + ".shares");
            if (sharesList != null) {
                ProfileTypes.registerProfileType(key, Sharables.fromList(sharesList));
            }
        }
    }
}
