package org.mvplugins.multiverse.inventories.util;

/**
 * Exception thrown when something goes wrong while deserializing this plugin's objects.
 */
public class DeserializationException extends Exception {

    public DeserializationException(String message) {
        super(message);
    }
}

