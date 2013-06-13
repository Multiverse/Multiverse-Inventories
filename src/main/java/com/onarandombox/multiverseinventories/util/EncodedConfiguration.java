package com.onarandombox.multiverseinventories.util;

import com.feildmaster.lib.configuration.EnhancedConfiguration;
import com.google.common.io.Files;
import org.apache.commons.lang.Validate;
import org.bukkit.plugin.Plugin;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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

    @Override
    public void load(InputStream stream) throws IOException, InvalidConfigurationException {
        Validate.notNull(stream, "Stream cannot be null");
        InputStreamReader reader = new InputStreamReader(stream, charset);
        StringBuilder builder = new StringBuilder();
        BufferedReader input = new BufferedReader(reader);
        try {
            String line;
            while ((line = input.readLine()) != null) {
                builder.append(line).append('\n');
            }
        } finally {
            input.close();
        }
        loadFromString(builder.toString());
    }

    @Override
    public void save(File file) throws IOException {
        Validate.notNull(file, "File cannot be null");

        Files.createParentDirs(file);

        String data = saveToString();

        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), charset));

        try {
            writer.write(data);
        } finally {
            writer.close();
        }
    }
}
