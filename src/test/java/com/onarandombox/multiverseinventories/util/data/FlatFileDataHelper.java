package com.onarandombox.multiverseinventories.util.data;

import com.onarandombox.multiverseinventories.api.profile.ContainerType;
import com.onarandombox.multiverseinventories.api.profile.PlayerData;

import java.io.File;

public class FlatFileDataHelper {

    private FlatFilePlayerData data;

    public FlatFileDataHelper(PlayerData data) {
        if (!(data instanceof FlatFilePlayerData)) {
            throw new ClassCastException("Must be instance of FlatFilePlayerData");
        }
        this.data = (FlatFilePlayerData) data;
    }

    public File getPlayerFile(ContainerType type, String dataName, String playerName) {
        return data.getPlayerFile(type, dataName, playerName);
    }
}
