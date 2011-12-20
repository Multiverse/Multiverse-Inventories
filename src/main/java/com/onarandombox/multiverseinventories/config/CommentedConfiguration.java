package com.onarandombox.multiverseinventories.config;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.util.HashMap;

/**
 * @author dumptruckman
 */
public class CommentedConfiguration extends YamlConfiguration {

    private HashMap<String, String> comments;
    private File file;

    public CommentedConfiguration(File file) {
        super();
        comments = new HashMap<String, String>();
        this.file = file;
    }

    public void load() throws Exception {
        this.load(file);
    }

    public boolean save() {

        boolean saved = true;

        // Save the config just like normal
        try {
            super.save(file);
        } catch (Exception e) {
            saved = false;
        }

        // if there's comments to add and it saved fine, we need to add comments
        if (!comments.isEmpty() && saved) {
            // String array of each line in the config file
            String[] yamlContents =
                    this.convertFileToString(file).split("[" + System.getProperty("line.separator") + "]");

            // This will hold the newly formatted line
            String newContents = "";
            // This holds the current path the lines are at in the config
            String currentPath = "";
            // This tells if the specified path has already been commented
            boolean commentedPath = false;
            // This flags if the line is a node or unknown text.
            boolean node = false;
            // The depth of the path. (number of words separated by periods - 1)
            int depth = 0;

            // Loop through the config lines
            for (String line : yamlContents) {
                // If the line is a node (and not something like a list value)
                if (line.contains(": ") || (line.length() > 1 && line.charAt(line.length() - 1) == ':')) {
                    // This is a new node so we need to mark it for commenting (if there are comments)
                    commentedPath = false;
                    // This is a node so flag it as one
                    node = true;

                    // Grab the index of the end of the node name
                    int index = 0;
                    index = line.indexOf(": ");
                    if (index < 0) {
                        index = line.length() - 1;
                    }
                    // If currentPath is empty, store the node name as the currentPath. (this is only on the first iteration, i think)
                    if (currentPath.isEmpty()) {
                        currentPath = line.substring(0, index);
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
                        // Find out if the current depth (whitespace * 2) is greater/lesser/equal to the previous depth
                        if (whiteSpace / 2 > depth) {
                            // Path is deeper.  Add a . and the node name
                            currentPath += "." + line.substring(whiteSpace, index);
                            depth++;
                        } else if (whiteSpace / 2 < depth) {
                            // Path is shallower, calculate current depth from whitespace (whitespace / 2) and subtract that many levels from the currentPath
                            int newDepth = whiteSpace / 2;
                            for (int i = 0; i < depth - newDepth; i++) {
                                currentPath = currentPath.replace(currentPath.substring(currentPath.lastIndexOf(".")), "");
                            }
                            // Grab the index of the final period
                            int lastIndex = currentPath.lastIndexOf(".");
                            if (lastIndex < 0) {
                                // if there isn't a final period, set the current path to nothing because we're at root
                                currentPath = "";
                            } else {
                                // If there is a final period, replace everything after it with nothing
                                currentPath = currentPath.replace(currentPath.substring(currentPath.lastIndexOf(".")), "");
                                currentPath += ".";
                            }
                            // Add the new node name to the path
                            currentPath += line.substring(whiteSpace, index);
                            // Reset the depth
                            depth = newDepth;
                        } else {
                            // Path is same depth, replace the last path node name to the current node name
                            int lastIndex = currentPath.lastIndexOf(".");
                            if (lastIndex < 0) {
                                // if there isn't a final period, set the current path to nothing because we're at root
                                currentPath = "";
                            } else {
                                // If there is a final period, replace everything after it with nothing
                                currentPath = currentPath.replace(currentPath.substring(currentPath.lastIndexOf(".")), "");
                                currentPath += ".";
                            }
                            //currentPath = currentPath.replace(currentPath.substring(currentPath.lastIndexOf(".")), "");
                            currentPath += line.substring(whiteSpace, index);
                        }
                    }
                } else {
                    node = false;
                }

                if (node) {
                    String comment = null;
                    if (!commentedPath) {
                        // If there's a comment for the current path, retrieve it and flag that path as already commented
                        comment = comments.get(currentPath);
                    }
                    if (comment != null) {
                        // Add the comment to the beginning of the current line
                        line = comment + System.getProperty("line.separator") + line + System.getProperty("line.separator");
                        comment = null;
                        commentedPath = true;
                    } else {
                        // Add a new line as it is a node, but has no comment
                        line += System.getProperty("line.separator");
                    }
                }
                // Add the (modified) line to the total config String
                newContents += line + ((!node) ? System.getProperty("line.separator"):"");
            }
            /*
             * Due to a bukkit bug we need to strip any extra new lines from the
             * beginning of this file, else they will multiply.
             */
            while (newContents.startsWith(System.getProperty("line.separator"))) {
                newContents = newContents.replaceFirst(System.getProperty("line.separator"), "");
            }

            try {
                // Write the string to the config file
                this.stringToFile(newContents, file);
            } catch (IOException e) {
                saved = false;
            }
        }
        return saved;
    }

    /**
     * Adds a comment just before the specified path.  The comment can be multiple lines.  An empty string will indicate a blank line.
     * @param path Configuration path to add comment.
     * @param commentLines Comments to add.  One String per line.
     */
    public void addComment(String path, String...commentLines) {
        StringBuilder commentstring = new StringBuilder();
        String leadingSpaces = "";
        for (int n = 0; n < path.length(); n++) {
            if (path.charAt(n) == '.') {
                leadingSpaces += "  ";
            }
        }
        for (String line : commentLines) {
            if (!line.isEmpty()) {
                line = leadingSpaces + line;
            } else {
                line = " ";
            }
            if (commentstring.length() > 0) {
                commentstring.append("\r\n");
            }
            commentstring.append(line);
        }
        comments.put(path, commentstring.toString());
    }

    /**
     * Pass a file and it will return it's contents as a string.
     * @param file File to read.
     * @return Contents of file.  String will be empty in case of any errors.
     */
    private String convertFileToString(File file) {
        if (file != null && file.exists() && file.canRead() && !file.isDirectory()) {
            Writer writer = new StringWriter();
            InputStream is = null;

            char[] buffer = new char[1024];
            try {
                is = new FileInputStream(file);
                Reader reader = new BufferedReader(
                        new InputStreamReader(is, "UTF-8"));
                int n;
                while ((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }
            } catch (IOException e) {
                System.out.println("Exception ");
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException ignore) {}
                }
            }
            return writer.toString();
        } else {
            return "";
        }
    }

    /**
     * Writes the contents of a string to a file.
     * @param source String to write.
     * @param file File to write to.
     * @return True on success.
     * @throws IOException
     */
    private boolean stringToFile(String source, File file) throws IOException {
        try {
            OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(file),"UTF-8");

            source.replaceAll("\n", System.getProperty("line.separator"));

            out.write(source);
            out.close();
            return true;
        } catch (IOException e) {
            System.out.println("Exception ");
            return false;
        }
    }
}