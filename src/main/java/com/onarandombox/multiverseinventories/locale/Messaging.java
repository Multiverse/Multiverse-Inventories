package com.onarandombox.multiverseinventories.locale;

public interface Messaging {

    /**
     * @return The {@link Messager} used by the Plugin.
     */
    public Messager getMessager();

    /**
     * Sets the {@link Messager} used by the Plugin.
     *
     * @param messager The new {@link Messager}. Must not be null!
     */
    public void setMessager(Messager messager);
}
