package com.onarandombox.multiverseinventories.data;

import com.onarandombox.multiverseinventories.profile.container.ContainerType;

import java.io.File;
import java.io.IOException;

public class FlatFileDataHelper {

    private FlatFilePlayerData data;

    public FlatFileDataHelper(PlayerData data) {
        if (!(data instanceof FlatFilePlayerData)) {
            throw new ClassCastException("Must be instance of FlatFilePlayerData");
        }
        this.data = (FlatFilePlayerData) data;
    }

    public File getPlayerFile(ContainerType type, String dataName, String playerName) throws IOException {
        return data.getPlayerFile(type, dataName, playerName);
    }
}
