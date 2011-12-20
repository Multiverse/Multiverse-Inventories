package com.onarandombox.multiverseinventories.data;

import java.io.IOException;

/**
 * @author dumptruckman
 */
public interface MVIData {

    /**
     * Loads the data data into memory and sets defaults
     * @throws java.io.IOException
     */
    public void load() throws IOException;

    public void save(boolean isReload);
}
