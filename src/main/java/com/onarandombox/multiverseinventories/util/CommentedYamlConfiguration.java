package com.onarandombox.multiverseinventories.util;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A Configuration wrapper class that allows for comments to be applied to the config paths.
 */
public final class CommentedYamlConfiguration {

    private final File file;
    private final FileConfiguration config;
    private final boolean doComments;
    private final HashMap<String, String> comments;

    private static final Pattern NEW_LINE_PATTERN = Pattern.compile("\r?\n");

    public CommentedYamlConfiguration(File file, boolean doComments) {
        this.file = file;
        this.doComments = doComments;
        this.comments = new HashMap<String, String>();
        this.config = new YamlConfiguration();

        try {
            this.config.load(file);
        } catch (Exception ignored) {}
    }

    /**
     * @return The underlying configuration object.
     */
    public FileConfiguration getConfig() {
        return this.config;
    }

    /**
     * Saves the file as per normal for YamlConfiguration and then parses the file and inserts
     * comments where necessary.
     *
     * @return True if successful.
     */
    public boolean save() {
        // try to save the config file, return false on failure
        try {
            config.save(file);
        } catch (Exception e) {
            return false;
        }

        // if we're not supposed to add comments, or there aren't any to add, we're done
        if (!doComments || comments.isEmpty()) {
            return true;
        }

        // convert config file to String
        String stringConfig = this.convertFileToString(file);

        // figure out where the header ends
        int indexAfterHeader = 0;
        Matcher newline = NEW_LINE_PATTERN.matcher(stringConfig);

        while (newline.find() && stringConfig.charAt(newline.end()) == '#') {
            indexAfterHeader = newline.end();
        }

        // convert stringConfig to array, ignoring the header
        String[] arrayConfig = stringConfig.substring(indexAfterHeader).split(newline.group());

        // begin building the new config, starting with the header
        StringBuilder newContents = new StringBuilder();
        newContents.append(stringConfig, 0, indexAfterHeader);

        // This holds the current path the lines are at in the config
        StringBuilder currentPath = new StringBuilder();

        // This flags if the line is a node or unknown text.
        boolean node;
        // The depth of the path. (number of words separated by periods - 1)
        int depth = 0;

        // Loop through the config lines
        for (final String line : arrayConfig) {
            // If the line is a node (and not something like a list value)
            if (line.contains(": ") || (line.length() > 1 && line.charAt(line.length() - 1) == ':')) {
                // This is a node so flag it as one
                node = true;

                // Grab the index of the end of the node name
                int index;
                index = line.indexOf(": ");
                if (index < 0) {
                    index = line.length() - 1;
                }
                // If currentPath is empty, store the node name as the currentPath. (this is only on the first iteration, i think)
                if (currentPath.toString().isEmpty()) {
                    currentPath = new StringBuilder(line.substring(0, index));
                } else {
                    // Calculate the whitespace preceding the node name
                    int whiteSpace = 0;
                    for (int n = 0; n < line.length(); n++) {
                        if (line.charAt(n) == ' ') {
                            whiteSpace++;
                        } else {
                            break;
                        }
                    }

                    int whiteSpaceDividedByTwo = whiteSpace / 2;
                    // Find out if the current depth (whitespace * 2) is greater/lesser/equal to the previous depth
                    if (whiteSpaceDividedByTwo > depth) {
                        // Path is deeper. Add a dot and the node name
                        currentPath.append(".").append(line, whiteSpace, index);
                        depth++;
                    } else if (whiteSpaceDividedByTwo < depth) {
                        // Path is shallower, calculate current depth from whitespace (whitespace / 2) and subtract that many levels from the currentPath
                        for (int i = 0; i < depth - whiteSpaceDividedByTwo; i++) {
                            currentPath.replace(currentPath.lastIndexOf("."), currentPath.length(), "");
                        }
                        // Grab the index of the final period
                        int lastIndex = currentPath.lastIndexOf(".");
                        if (lastIndex < 0) {
                            // if there isn't a final period, set the current path to nothing because we're at root
                            currentPath = new StringBuilder();
                        } else {
                            // If there is a final period, replace everything after it with nothing
                            currentPath.replace(currentPath.lastIndexOf("."), currentPath.length(), "").append(".");
                        }
                        // Add the new node name to the path
                        currentPath.append(line, whiteSpace, index);
                        // Reset the depth
                        depth = whiteSpaceDividedByTwo;
                    } else {
                        // Path is same depth, replace the last path node name to the current node name
                        int lastIndex = currentPath.lastIndexOf(".");
                        if (lastIndex < 0) {
                            // if there isn't a final period, set the current path to nothing because we're at root
                            currentPath = new StringBuilder();
                        } else {
                            // If there is a final period, replace everything after it with nothing
                            currentPath.replace(currentPath.lastIndexOf("."), currentPath.length(), "").append(".");
                        }

                        currentPath.append(line, whiteSpace, index);
                    }
                }
            } else {
                node = false;
            }

            StringBuilder newLine = new StringBuilder();

            if (node) {
                // get the comment for the current node
                String comment = comments.get(currentPath.toString());
                if (comment != null && !comment.isEmpty()) {
                    // if the previous line doesn't end in a colon
                    // and there's not already a newline character,
                    // add a newline before we add the comment
                    char previousChar = newContents.charAt(newContents.length() - 2);
                    if (previousChar != ':' && previousChar != '\n') {
                        newLine.append("\n");
                    }

                    // add the comment
                    newLine.append(comment).append("\n");
                }
            }

            // add the config line
            newLine.append(line).append("\n");

            // Add the (modified) line to the total config String
            newContents.append(newLine);
        }

        // try to save the config file, returning whether it saved successfully
        return this.stringToFile(newContents.toString(), file);
    }

    /**
     * Adds a comment just before the specified path. The comment can be multiple lines. An empty string will indicate
     * a blank line.
     *
     * @param path         Configuration path to add comment.
     * @param commentLines Comments to add. One String per line.
     */
    public void addComment(String path, List<String> commentLines) {
        StringBuilder commentString = new StringBuilder();
        StringBuilder leadingSpaces = new StringBuilder();
        for (int n = 0; n < path.length(); n++) {
            if (path.charAt(n) == '.') {
                leadingSpaces.append("  ");
            }
        }
        for (String line : commentLines) {
            if (commentString.length() > 0) {
                commentString.append("\n");
            }
            if (!line.isEmpty()) {
                commentString.append(leadingSpaces).append(line);
            }
        }
        comments.put(path, commentString.toString());
    }

    /**
     * Pass a file and it will return it's contents as a string.
     *
     * @param file File to read.
     * @return Contents of file. String will be empty in case of any errors.
     */
    private String convertFileToString(File file) {
        final int bufferSize = 1024;
        if (file != null && file.exists() && file.canRead() && !file.isDirectory()) {
            Writer writer = new StringWriter();
            char[] buffer = new char[bufferSize];

            try (InputStream is = new FileInputStream(file)) {
                int n;
                Reader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                while ((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return writer.toString();
        } else {
            return "";
        }
    }

    /**
     * Writes the contents of a string to a file.
     *
     * @param source String to write.
     * @param file   File to write to.
     * @return True on success.
     */
    private boolean stringToFile(String source, File file) {
        OutputStreamWriter out = null;
        try {
            out = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
            out.write(source);
            out.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

