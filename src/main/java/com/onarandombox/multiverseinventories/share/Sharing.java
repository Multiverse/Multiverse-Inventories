package com.onarandombox.multiverseinventories.share;

/**
 * A enum which shows if sharing is set to true/false or simply not set.
 */
public enum Sharing {

    // BEGIN CHECKSTYLE-SUPPRESSION: Javadoc
    TRUE,
    FALSE,
    NOT_SET;
    // END CHECKSTYLE-SUPPRESSION: Javadoc

    /**
     * @return True if equals {@link #TRUE}
     */
    public boolean isTrue() {
        return this.equals(TRUE);
    }

    /**
     * @return True if equals {@link #NOT_SET}
     */
    public boolean isNotSet() {
        return this.equals(NOT_SET);
    }
}
