/******************************************************************************
 * Multiverse 2 Copyright (c) the Multiverse Team 2020.                       *
 * Multiverse 2 is licensed under the BSD License.                            *
 * For more information please check the README.md file included              *
 * with this project.                                                         *
 ******************************************************************************/

package com.onarandombox.multiverseinventories.util;

import org.apache.commons.lang.Validate;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class PersistentDataContainerImpl implements PersistentDataContainer {

    private final Map<String, Object> data = new HashMap<>();
    private final PersistentDataAdapterContext adapterContext = new PersistentDataAdapterContextImpl();

    @Override
    public <T, Z> void set(@NotNull NamespacedKey key, @NotNull PersistentDataType<T, Z> type, @NotNull Z value) {
        Validate.notNull(key, "The provided key for the custom value was null");
        Validate.notNull(value, "The provided value for the custom value was null");

        data.put(key.toString(), value);
    }

    @Override
    public <T, Z> boolean has(@NotNull NamespacedKey key, @NotNull PersistentDataType<T, Z> type) {
        return data.containsKey(key.toString());
    }

    @Override
    public <T, Z> @Nullable Z get(@NotNull NamespacedKey key, @NotNull PersistentDataType<T, Z> type) {
        return (Z) data.get(key.toString());
    }

    @Override
    public <T, Z> @NotNull Z getOrDefault(@NotNull NamespacedKey key, @NotNull PersistentDataType<T, Z> type, @NotNull Z defaultValue) {
        return (Z) data.getOrDefault(key.toString(), defaultValue);
    }

    @Override
    public void remove(@NotNull NamespacedKey key) {
        data.remove(key.toString());
    }

    @Override
    public boolean isEmpty() {
        return data.isEmpty();
    }

    @Override
    public @NotNull PersistentDataAdapterContext getAdapterContext() {
        return adapterContext;
    }
}
