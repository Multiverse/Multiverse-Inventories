package com.onarandombox.multiverseinventories.migration;

/**
 * Exception thrown when migration doesn't go well.
 */
public class MigrationException extends Exception {

    private Exception causeException = null;

    public MigrationException(String message) {
        super(message);
    }

    /**
     * Sets what the causing exception was, if any.
     *
     * @param exception The cause exception.
     * @return This exception for easy chainability.
     */
    public MigrationException setCauseException(Exception exception) {
        this.causeException = exception;
        return this;
    }

    /**
     * @return The causing exception or null if none.
     */
    public Exception getCauseException() {
        return this.causeException;
    }
}

