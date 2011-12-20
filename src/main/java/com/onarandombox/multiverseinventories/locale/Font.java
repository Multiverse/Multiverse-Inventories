package com.onarandombox.multiverseinventories.locale;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Class for font functions
 * @author TAT
 * @modifiedby dumptruckman
 */
public class Font {

    private final static int LINESIZE = 318;

    static HashMap<String, Integer> fontWidth = new HashMap<String, Integer>();

    public Font() {
        /*
         * Widths is in pixels
         * Got them from fontWidths.txt uploaded to the Bukkit forum by Edward Hand
         * http://forums.bukkit.org/threads/formatting-module-output-text-into-columns.8481/
         */
        fontWidth.clear();
        fontWidth.put(" ",4);
        fontWidth.put("!",2);
        fontWidth.put("\"",5);
        fontWidth.put("#",6);
        fontWidth.put("$",6);
        fontWidth.put("%",6);
        fontWidth.put("&",6);
        fontWidth.put("'",3);
        fontWidth.put("(",5);
        fontWidth.put(")",5);
        fontWidth.put("*",5);
        fontWidth.put("+",6);
        fontWidth.put(",",2);
        fontWidth.put("-",6);
        fontWidth.put(".",2);
        fontWidth.put("/",6);
        fontWidth.put("0",6);
        fontWidth.put("1",6);
        fontWidth.put("2",6);
        fontWidth.put("3",6);
        fontWidth.put("4",6);
        fontWidth.put("5",6);
        fontWidth.put("6",6);
        fontWidth.put("7",6);
        fontWidth.put("8",6);
        fontWidth.put("9",6);
        fontWidth.put(":",2);
        fontWidth.put(";",2);
        fontWidth.put("<",5);
        fontWidth.put("=",6);
        fontWidth.put(">",5);
        fontWidth.put("?",6);
        fontWidth.put("@",7);
        fontWidth.put("A",6);
        fontWidth.put("B",6);
        fontWidth.put("C",6);
        fontWidth.put("D",6);
        fontWidth.put("E",6);
        fontWidth.put("F",6);
        fontWidth.put("G",6);
        fontWidth.put("H",6);
        fontWidth.put("I",4);
        fontWidth.put("J",6);
        fontWidth.put("K",6);
        fontWidth.put("L",6);
        fontWidth.put("M",6);
        fontWidth.put("N",6);
        fontWidth.put("O",6);
        fontWidth.put("P",6);
        fontWidth.put("Q",6);
        fontWidth.put("R",6);
        fontWidth.put("S",6);
        fontWidth.put("T",6);
        fontWidth.put("U",6);
        fontWidth.put("V",6);
        fontWidth.put("W",6);
        fontWidth.put("X",6);
        fontWidth.put("Y",6);
        fontWidth.put("Z",6);
        fontWidth.put("_",6);
        fontWidth.put("'",3);
        fontWidth.put("a",6);
        fontWidth.put("b",6);
        fontWidth.put("c",6);
        fontWidth.put("d",6);
        fontWidth.put("e",6);
        fontWidth.put("f",5);
        fontWidth.put("g",6);
        fontWidth.put("h",6);
        fontWidth.put("i",2);
        fontWidth.put("j",6);
        fontWidth.put("k",5);
        fontWidth.put("l",3);
        fontWidth.put("m",6);
        fontWidth.put("n",6);
        fontWidth.put("o",6);
        fontWidth.put("p",6);
        fontWidth.put("q",6);
        fontWidth.put("r",6);
        fontWidth.put("s",6);
        fontWidth.put("t",4);
        fontWidth.put("u",6);
        fontWidth.put("v",6);
        fontWidth.put("w",6);
        fontWidth.put("x",6);
        fontWidth.put("y",6);
        fontWidth.put("z",6);
    }

    /**
     * Get width of string in pixels
     * @param text String
     * @return Length of string in pixels
     */
    public static int stringWidth(String text) {
        if (fontWidth.isEmpty()) {
            return 0;
        }
        char[] chars = text.toCharArray();
        int width = 0;
        int spacepos = 0;
        for (int i = 0; i < chars.length; i++) {
            if (fontWidth.containsKey(String.valueOf(chars[i]))) {
                width += fontWidth.get(String.valueOf(chars[i]));
            } else if (chars[i] == (char)167) {
                i++;
            }
        }
        return width;
    }

    /**
     * Get a List of Strings where none exceed the maximum line length
     * @param text String
     * @return List of Strings
     */
    public static List<String> splitString(String text) {
        List<String> split = new ArrayList<String>();
        if (fontWidth.isEmpty()) {
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
            if (fontWidth.containsKey(String.valueOf(chars[i]))) {
                width += fontWidth.get(String.valueOf(chars[i]));
            } else if (chars[i] == (char)167) {
                i++;
                lastcolor = Character.toString(chars[i]);
                colorfoundthisline = true;
            }
            if ((width > LINESIZE) && (lastspaceindex != 0)) {
                if (lastcolor != null && !colorfoundthisline) {
                    split.add(Character.toString((char)167) + lastcolor
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
                split.add(Character.toString((char)167) + lastcolor
                        + text.substring(lastlineindex));
            } else {
                split.add(text.substring(lastlineindex));
            }

        return split;
    }
}