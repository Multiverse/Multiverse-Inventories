package org.mvplugins.multiverse.inventories.dataimport;

import org.mvplugins.multiverse.core.exceptions.MultiverseException;
import org.mvplugins.multiverse.core.locale.message.Message;
import org.mvplugins.multiverse.external.jetbrains.annotations.Nullable;

/**
 * Exception thrown when migration doesn't go well.
 */
public class DataImportException extends MultiverseException {

    private Exception causeException = null;

    public DataImportException(@Nullable String message) {
        super(message);
    }

    public DataImportException(@Nullable String message, Exception causeException) {
        super(message);
        this.causeException = causeException;
    }

    public DataImportException(@Nullable Message message, Exception causeException) {
        super(message);
        this.causeException = causeException;
    }

    public DataImportException(@Nullable String message, @Nullable Throwable cause, Exception causeException) {
        super(message, cause);
        this.causeException = causeException;
    }

    public DataImportException(@Nullable Message message, @Nullable Throwable cause, Exception causeException) {
        super(message, cause);
        this.causeException = causeException;
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

