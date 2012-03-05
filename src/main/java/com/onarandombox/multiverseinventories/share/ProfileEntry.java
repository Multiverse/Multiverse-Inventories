package com.onarandombox.multiverseinventories.share;

public class ProfileEntry {

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
}
