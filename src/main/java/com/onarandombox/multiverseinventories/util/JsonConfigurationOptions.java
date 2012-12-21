package com.onarandombox.multiverseinventories.util;

import org.bukkit.configuration.file.FileConfigurationOptions;

public class JsonConfigurationOptions extends FileConfigurationOptions {

    protected JsonConfigurationOptions(JsonConfiguration configuration) {
        super(configuration);
    }

    @Override
    public JsonConfiguration configuration() {
        return (JsonConfiguration) super.configuration();
    }

    @Override
    public JsonConfigurationOptions copyDefaults(boolean value) {
        super.copyDefaults(value);
        return this;
    }

    @Override
    public JsonConfigurationOptions pathSeparator(char value) {
        super.pathSeparator(value);
        return this;
    }

    @Override
    public JsonConfigurationOptions header(String value) {
        super.header(value);
        return this;
    }

    @Override
    public JsonConfigurationOptions copyHeader(boolean value) {
        super.copyHeader(value);
        return this;
    }
}
