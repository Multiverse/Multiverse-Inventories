package com.onarandombox.multiverseinventories.dataimport;

/**
 * Exception thrown when migration doesn't go well.
 */
public class DataImportException extends Exception {

    private Exception causeException = null;

    public DataImportException(String message) {
        super(message);
    }

    /**
     * Sets what the causing exception was, if any.
     *
     * @param exception The cause exception.
     * @return This exception for easy chainability.
     */
    public DataImportException setCauseException(Exception exception) {
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

