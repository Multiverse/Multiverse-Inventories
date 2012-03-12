package com.onarandombox.multiverseinventories.share;

import java.util.HashMap;
import java.util.Map;

public class ProfileEntry {

    private static final Map<String, SerializableSharable> statsMap = new HashMap<String, SerializableSharable>();
    private static final Map<String, SerializableSharable> othersMap = new HashMap<String, SerializableSharable>();

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

    static void register(SerializableSharable sharable) {
        ProfileEntry entry = sharable.getProfileEntry();
        if (entry == null) {
            // This would mean the sharable is not intended for saving in profile files.
            return;
        }
        if (entry.isStat()) {
            statsMap.put(entry.getFileTag(), sharable);
        } else {
            othersMap.put(entry.getFileTag(), sharable);
        }
    }

    public static SerializableSharable lookup(boolean stat, String fileTag) {
        if (stat) {
            return statsMap.get(fileTag);
        } else {
            return othersMap.get(fileTag);
        }
    }
}
