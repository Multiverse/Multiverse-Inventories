package com.onarandombox.multiverseinventories.migration;

/**
 * Exception thrown when migration doesn't go well.
 */
public class MigrationException extends Exception {

    public MigrationException(String message) {
        super(message);
    }
}
