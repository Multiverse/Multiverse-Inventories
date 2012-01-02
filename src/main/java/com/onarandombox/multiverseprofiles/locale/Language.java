package com.onarandombox.multiverseprofiles.locale;

public enum Language {

    ERROR("messages.generic.error"),
    SUCCESS("messages.generic.success"),
    INFO("messages.generic.info"),;

    private String path;

    Language(String path) {
        this.path = path;
    }

    /**
     * Retrieves the path for a config option
     *
     * @return The path for a config option
     */
    protected String getPath() {
        return path;
    }
}
