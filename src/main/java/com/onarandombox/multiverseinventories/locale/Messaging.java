package com.onarandombox.multiverseinventories.locale;

/**
 * This interface is implemented by classes that use a {@link Messager}.
 */
public interface Messaging {

    /**
     * @return The {@link Messager} used by the Plugin.
     */
    Messager getMessager();

    /**
     * Sets the {@link Messager} used by the Plugin.
     *
     * @param messager The new {@link Messager}. Must not be null!
     */
    void setMessager(Messager messager);
}

