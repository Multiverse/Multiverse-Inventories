package com.onarandombox.multiverseinventories.util;

import com.feildmaster.lib.configuration.EnhancedConfiguration;
import com.google.common.io.Files;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.plugin.Plugin;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;

public class EncodedConfiguration extends EnhancedConfiguration {

    private final Charset charset;

    public EncodedConfiguration(Plugin plugin, String charset) throws UnsupportedEncodingException, IllegalCharsetNameException {
        super(plugin);
        this.charset = Charset.forName(charset);
    }

    public EncodedConfiguration(String file, Plugin plugin, String charset) throws UnsupportedEncodingException, IllegalCharsetNameException {
        super(file, plugin);
        this.charset = Charset.forName(charset);
    }

    public EncodedConfiguration(File file, String charset) throws UnsupportedEncodingException, IllegalCharsetNameException {
        super(file);
        this.charset = Charset.forName(charset);
    }

    public EncodedConfiguration(File file, Plugin plugin, String charset) throws UnsupportedEncodingException, IllegalCharsetNameException {
        super(file, plugin);
        this.charset = Charset.forName(charset);
    }
}
