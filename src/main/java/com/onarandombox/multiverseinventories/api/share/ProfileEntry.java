package com.onarandombox.multiverseinventories.api.share;

import java.util.HashMap;
import java.util.Map;

/**
 * Indicates how a Sharable should be stored in the profile file.  Serves as a lookup for finding a sharable based on
 * it's file tag.
 */
public final class ProfileEntry {

    private static final Map<String, Sharable> STATS_MAP = new HashMap<String, Sharable>();
    private static final Map<String, Sharable> OTHERS_MAP = new HashMap<String, Sharable>();

    private boolean isStat;
    private String fileTag;

    public ProfileEntry(boolean isStat, String fileTag) {
        this.isStat = isStat;
        this.fileTag = fileTag;
    }

    /**
     * @return True if this indicates a {@link Sharable} whose data will be stored in the stats string of the player
     *         file.
     */
    public boolean isStat() {
        return this.isStat;
    }

    /**
     * @return The String that represents where this file is stored in the player profile.
     */
    public String getFileTag() {
        return this.fileTag;
    }

    /**
     * Registers a {@link Sharable} with it's respective ProfileEntry.
     *
     * @param sharable The sharable to register.
     */
    static void register(Sharable sharable) {
        ProfileEntry entry = sharable.getProfileEntry();
        if (entry == null) {
            // This would mean the sharable is not intended for saving in profile files.
            return;
        }
        if (entry.isStat()) {
            STATS_MAP.put(entry.getFileTag(), sharable);
        } else {
            OTHERS_MAP.put(entry.getFileTag(), sharable);
        }
    }

    /**
     * Used to look up a {@link Sharable} by it's file tag.
     *
     * @param stat    True means this sharable is in the stats section of the player file.
     * @param fileTag The string representing where the sharable is stored in the player file.
     * @return A sharable if one has been registered with it's ProfileEntry otherwise null.
     */
    public static Sharable lookup(boolean stat, String fileTag) {
        if (stat) {
            return STATS_MAP.get(fileTag);
        } else {
            return OTHERS_MAP.get(fileTag);
        }
    }
}
