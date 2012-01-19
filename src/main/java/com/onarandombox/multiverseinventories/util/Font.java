package com.onarandombox.multiverseinventories.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Class for font functions including font width specifications.
 */
public class Font {

    /**
     * Provides the char for a Section symbol.
     */
    public static final char SECTION_SYMBOL = (char) 167;

    private static final int LINE_LENGTH = 318;
    private static final HashMap<String, Integer> FONT_WIDTH;

    static {
        FONT_WIDTH = new HashMap<String, Integer>();
        /*
         * Widths is in pixels
         * Got them from fontWidths.txt uploaded to the Bukkit forum by Edward Hand
         * http://forums.bukkit.org/threads/formatting-module-output-text-into-columns.8481/
         */
        // BEGIN CHECKSTYLE-SUPPRESSION: MagicNumberCheck
        FONT_WIDTH.put(" ", 4);
        FONT_WIDTH.put("!", 2);
        FONT_WIDTH.put("\"", 5);
        FONT_WIDTH.put("#", 6);
        FONT_WIDTH.put("$", 6);
        FONT_WIDTH.put("%", 6);
        FONT_WIDTH.put("&", 6);
        FONT_WIDTH.put("'", 3);
        FONT_WIDTH.put("(", 5);
        FONT_WIDTH.put(")", 5);
        FONT_WIDTH.put("*", 5);
        FONT_WIDTH.put("+", 6);
        FONT_WIDTH.put(",", 2);
        FONT_WIDTH.put("-", 6);
        FONT_WIDTH.put(".", 2);
        FONT_WIDTH.put("/", 6);
        FONT_WIDTH.put("0", 6);
        FONT_WIDTH.put("1", 6);
        FONT_WIDTH.put("2", 6);
        FONT_WIDTH.put("3", 6);
        FONT_WIDTH.put("4", 6);
        FONT_WIDTH.put("5", 6);
        FONT_WIDTH.put("6", 6);
        FONT_WIDTH.put("7", 6);
        FONT_WIDTH.put("8", 6);
        FONT_WIDTH.put("9", 6);
        FONT_WIDTH.put(":", 2);
        FONT_WIDTH.put(";", 2);
        FONT_WIDTH.put("<", 5);
        FONT_WIDTH.put("=", 6);
        FONT_WIDTH.put(">", 5);
        FONT_WIDTH.put("?", 6);
        FONT_WIDTH.put("@", 7);
        FONT_WIDTH.put("A", 6);
        FONT_WIDTH.put("B", 6);
        FONT_WIDTH.put("C", 6);
        FONT_WIDTH.put("D", 6);
        FONT_WIDTH.put("E", 6);
        FONT_WIDTH.put("F", 6);
        FONT_WIDTH.put("G", 6);
        FONT_WIDTH.put("H", 6);
        FONT_WIDTH.put("I", 4);
        FONT_WIDTH.put("J", 6);
        FONT_WIDTH.put("K", 6);
        FONT_WIDTH.put("L", 6);
        FONT_WIDTH.put("M", 6);
        FONT_WIDTH.put("N", 6);
        FONT_WIDTH.put("O", 6);
        FONT_WIDTH.put("P", 6);
        FONT_WIDTH.put("Q", 6);
        FONT_WIDTH.put("R", 6);
        FONT_WIDTH.put("S", 6);
        FONT_WIDTH.put("T", 6);
        FONT_WIDTH.put("U", 6);
        FONT_WIDTH.put("V", 6);
        FONT_WIDTH.put("W", 6);
        FONT_WIDTH.put("X", 6);
        FONT_WIDTH.put("Y", 6);
        FONT_WIDTH.put("Z", 6);
        FONT_WIDTH.put("_", 6);
        FONT_WIDTH.put("'", 3);
        FONT_WIDTH.put("a", 6);
        FONT_WIDTH.put("b", 6);
        FONT_WIDTH.put("c", 6);
        FONT_WIDTH.put("d", 6);
        FONT_WIDTH.put("e", 6);
        FONT_WIDTH.put("f", 5);
        FONT_WIDTH.put("g", 6);
        FONT_WIDTH.put("h", 6);
        FONT_WIDTH.put("i", 2);
        FONT_WIDTH.put("j", 6);
        FONT_WIDTH.put("k", 5);
        FONT_WIDTH.put("l", 3);
        FONT_WIDTH.put("m", 6);
        FONT_WIDTH.put("n", 6);
        FONT_WIDTH.put("o", 6);
        FONT_WIDTH.put("p", 6);
        FONT_WIDTH.put("q", 6);
        FONT_WIDTH.put("r", 6);
        FONT_WIDTH.put("s", 6);
        FONT_WIDTH.put("t", 4);
        FONT_WIDTH.put("u", 6);
        FONT_WIDTH.put("v", 6);
        FONT_WIDTH.put("w", 6);
        FONT_WIDTH.put("x", 6);
        FONT_WIDTH.put("y", 6);
        FONT_WIDTH.put("z", 6);
        // END CHECKSTYLE-SUPPRESSION: MagicNumberCheck
    }

    private Font() { }

    /**
     * Get width of string in pixels.
     *
     * @param text String.
     * @return Length of string in pixels.
     */
    public static int stringWidth(String text) {
        if (FONT_WIDTH.isEmpty()) {
            return 0;
        }
        char[] chars = text.toCharArray();
        int width = 0;
        int spacepos = 0;
        for (int i = 0; i < chars.length; i++) {
            if (FONT_WIDTH.containsKey(String.valueOf(chars[i]))) {
                width += FONT_WIDTH.get(String.valueOf(chars[i]));
            } else if (chars[i] == SECTION_SYMBOL) {
                i++;
            }
        }
        return width;
    }

    /**
     * Get a List of Strings where none exceed the maximum line length.
     *
     * @param text String
     * @return List of Strings
     */
    public static List<String> splitString(String text) {
        List<String> split = new ArrayList<String>();
        if (FONT_WIDTH.isEmpty()) {
            split.add(text);
            return split;
        }
        char[] chars = text.toCharArray();
        int width = 0;
        int lastspaceindex = 0;
        int lastlineindex = 0;
        String lastcolor = null;
        boolean colorfoundthisline = false;
        for (int i = 0; i < chars.length; i++) {
            if (FONT_WIDTH.containsKey(String.valueOf(chars[i]))) {
                width += FONT_WIDTH.get(String.valueOf(chars[i]));
            } else if (chars[i] == SECTION_SYMBOL) {
                i++;
                lastcolor = Character.toString(chars[i]);
                colorfoundthisline = true;
            }
            if ((width > LINE_LENGTH) && (lastspaceindex != 0)) {
                if (lastcolor != null && !colorfoundthisline) {
                    split.add(Character.toString(SECTION_SYMBOL) + lastcolor
                            + text.substring(lastlineindex, lastspaceindex));
                } else {
                    split.add(text.substring(lastlineindex, lastspaceindex));
                }
                colorfoundthisline = false;
                lastlineindex = lastspaceindex;
                i = lastspaceindex;
                width = 0;
            }
            if (String.valueOf(chars[i]).equals(" ")) {
                lastspaceindex = i;
            }
        }
        if (!text.substring(lastlineindex).isEmpty())
            if (lastcolor != null && !colorfoundthisline) {
                split.add(Character.toString(SECTION_SYMBOL) + lastcolor
                        + text.substring(lastlineindex));
            } else {
                split.add(text.substring(lastlineindex));
            }

        return split;
    }
}

