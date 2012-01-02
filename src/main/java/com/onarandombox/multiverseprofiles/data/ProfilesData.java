package com.onarandombox.multiverseprofiles.data;

import com.onarandombox.multiverseprofiles.MultiverseProfiles;

import java.io.IOException;

/**
 * @author dumptruckman
 */
public interface ProfilesData {

    /**
     * Loads the data data into memory and sets defaults
     *
     * @throws java.io.IOException
     */
    public void load(MultiverseProfiles mvProfiles) throws IOException;

    public void save(boolean isReload);
}
