/******************************************************************************
 * Multiverse 2 Copyright (c) the Multiverse Team 2020.                       *
 * Multiverse 2 is licensed under the BSD License.                            *
 * For more information please check the README.md file included              *
 * with this project.                                                         *
 ******************************************************************************/

package com.onarandombox.multiverseinventories.util;

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;

public class PersistentDataAdapterContextImpl implements PersistentDataAdapterContext {

    @Override
    public @NotNull PersistentDataContainer newPersistentDataContainer() {
        return new PersistentDataContainerImpl();
    }
}
