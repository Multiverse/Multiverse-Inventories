package com.onarandombox.multiverseinventories.util;

import com.google.common.io.Files;
import org.apache.commons.lang.Validate;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;

public class EncodedJsonConfiguration extends JsonConfiguration {

    private final Charset charset;

    public EncodedJsonConfiguration(File file, String charset) throws UnsupportedEncodingException, IllegalCharsetNameException {
        super(file);
        this.charset = Charset.forName(charset);
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
