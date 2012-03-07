package com.onarandombox.multiverseinventories.share;

import java.util.HashMap;
import java.util.Map;

public class ProfileEntry {
    
    private static final Map<String, Sharable> statsMap = new HashMap<String, Sharable>();
    private static final Map<String, Sharable> othersMap = new HashMap<String, Sharable>();

    private boolean isStat;
    private String fileTag;

    public ProfileEntry(boolean isStat, String fileTag) {
        this.isStat = isStat;
        this.fileTag = fileTag;
    }

    public boolean isStat() {
        return this.isStat;
    }

    public String getFileTag() {
        return this.fileTag;
    }
    
    static void register(Sharable sharable) {
        ProfileEntry entry = sharable.getProfileEntry();
        if (entry.isStat()) {
            statsMap.put(entry.getFileTag(), sharable);
        } else {
            othersMap.put(entry.getFileTag(), sharable);
        }
    }
    
    public static Sharable lookup(boolean stat, String fileTag) {
        if (stat) {
            return statsMap.get(fileTag);
        } else {
            return othersMap.get(fileTag);
        }
    }
}
