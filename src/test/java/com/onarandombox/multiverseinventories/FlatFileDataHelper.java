package com.onarandombox.multiverseinventories;

import com.onarandombox.multiverseinventories.profile.ProfileDataSource;
import com.onarandombox.multiverseinventories.profile.container.ContainerType;
import org.bukkit.OfflinePlayer;

import java.io.File;
import java.io.IOException;

public class FlatFileDataHelper {

    private FlatFileProfileDataSource data;

    public FlatFileDataHelper(ProfileDataSource data) {
        if (!(data instanceof FlatFileProfileDataSource)) {
            throw new ClassCastException("Must be instance of FlatFilePlayerData");
        }
        this.data = (FlatFileProfileDataSource) data;
    }

    public File getPlayerFile(ContainerType type, String dataName, OfflinePlayer player) throws IOException {
        return data.getPlayerFile(type, dataName, player);
    }
}
